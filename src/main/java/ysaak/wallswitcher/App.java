package ysaak.wallswitcher;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ysaak.wallswitcher.data.notification.NotificationType;
import ysaak.wallswitcher.data.notification.TrayNotification;
import ysaak.wallswitcher.event.*;
import ysaak.wallswitcher.exception.ServiceInitException;
import ysaak.wallswitcher.services.Services;
import ysaak.wallswitcher.services.eventbus.EventBus;
import ysaak.wallswitcher.services.eventbus.Subscribe;
import ysaak.wallswitcher.task.ChangeWallpaperTask;
import ysaak.wallswitcher.task.SetWallpaperTask;
import ysaak.wallswitcher.task.ShowWallpaperTask;
import ysaak.wallswitcher.ui.AppMenu;
import ysaak.wallswitcher.ui.Dialogs;
import ysaak.wallswitcher.ui.creator.CreatorScene;
import ysaak.wallswitcher.ui.notification.NotificationEngine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends javafx.application.Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final String ICON_IMAGE = "/img/picture.png";

    private static Stage mainStage;


    private SystemTray tray = null;
    private TrayIcon trayIcon = null;

    public static Path getAppDirectory() {
        return Paths.get(System.getProperty("user.home"), ".ysk-profile");
    }

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        if (Files.notExists(getAppDirectory())) {
            try {
                Files.createDirectories(getAppDirectory());
            }
            catch (final IOException e) {
                LOGGER.error("Cannot create application directory", e);
            }
        }
    }

    @Override
    public void start(final Stage primaryStage) {
        mainStage = primaryStage;
        mainStage.setTitle("Wall-switcher");

        Dialogs.setMainStage(mainStage);

        // Instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);

        try {
            Services.getWallpaperService().init();
            Services.getProfileService().init();
        }
        catch (ServiceInitException e) {
            Dialogs.error("Error while starting application", e);
            exit(-1);
        }
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!SystemTray.isSupported()) {
                LOGGER.error("No system tray support, application exiting.");
                exit(-1);
            }

            // set up a system tray icon.
            tray = SystemTray.getSystemTray();
            final Image image = ImageIO.read(getClass().getResource(ICON_IMAGE));
            trayIcon = new TrayIcon(image);

            // Initialize menu
            final AppMenu menu = new AppMenu();

            final Frame frame = new Frame("");
            frame.setUndecorated(true);
            frame.setType(Window.Type.UTILITY);


            // if the user double-clicks on the tray icon, show the main app
            // stage.
            //trayIcon.addActionListener(event -> Platform.runLater(this::showConfigurationId));
            trayIcon.addActionListener(event -> Platform.runLater(this::showCreator));
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        frame.add(menu.getPopup());
                        menu.getPopup().show(frame, e.getXOnScreen(), e.getYOnScreen());
                    }
                }
            });

            menu.defaultProfileAvailableProperty().bind(Services.getWallpaperService().defaultProfileAvailableProperty());
            Bindings.bindContent(menu.getAvailableProfiles(), Services.getWallpaperService().getAvailableProfiles());

            trayIcon.setPopupMenu(menu.getPopup());
            // add the application tray icon to the system tray.
            tray.add(trayIcon);
            frame.setResizable(false);
            frame.setVisible(true);
        }
        catch (java.awt.AWTException | IOException e) {
            LOGGER.error("Unable to init system tray", e);
            exit(-1);
        }
    }

    private void showCreator() {
        CreatorScene editor = new CreatorScene();
        editor.setScreensList(Services.getProfileService().getCurrentProfile().getScreenList());
        Scene scene = new Scene(editor.getPane());

        scene.getStylesheets().add(App.class.getResource("/theme.css").toExternalForm());

        mainStage.setScene(scene);
        mainStage.show();
    }

    private void exit(final int code) {
        Platform.exit();
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
        }
        System.exit(code);
    }

    @Subscribe
    private void handleBasicEvent(final BasicEvent event) {
        switch (event.getAction()) {
            case EXIT:
                exit(0);
                break;

            case REFRESH:
                executeTask(new ChangeWallpaperTask(null));
                break;

            case OPEN_CREATOR:
                Platform.runLater(this::showCreator);
                break;

            default:
                LOGGER.warn("No action defined for code: " + event.getAction());
                break;
        }
    }

    /**
     * Displays the wallpaper defined for a specific profile
     *
     * @param event Show wallpaper event
     */
    @Subscribe
    private void handleShowWallpaperEvent(ShowWallpaperEvent event) {
        executeTask(new ShowWallpaperTask(event.getProfileId()));
    }

    /**
     * Sets the wallpaper for a specific profile and update it
     *
     * @param event Set wallpaper event
     */
    @Subscribe
    private void handleSetWallpaperEvent(SetWallpaperEvent event) {
        executeTask(new SetWallpaperTask(event.getProfileId(), event.getWallpaperFile()));
    }

    @Subscribe
    private void handleNewProfileDetected(NewProfileDetectedEvent event) {
        if (event.getProfile() != null) {
            LOGGER.info("New profile detected {}", event.getProfile().getId());
            executeTask(new ChangeWallpaperTask(event.getProfile().getId()));
        }
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    private <V> void executeTask(final Task<V> task) {
        final Service<V> service = new Service<V>() {
            @Override
            protected Task<V> createTask() {
                return task;
            }
        };

        service.setOnRunning(evt -> LOGGER.debug("Task {} starting", task.getTitle()));
        service.setOnSucceeded(evt -> LOGGER.debug("Task {} completed", task.getTitle()));

        service.setOnFailed(evt -> {
            LOGGER.error("Task {} has failed", service.getException());
            //handleNotificationEvent(new NotificationEvent(NotificationEvent.NotificationType.ERROR, "CODE TO CHANGE"));
        });

        service.start();
    }
}
