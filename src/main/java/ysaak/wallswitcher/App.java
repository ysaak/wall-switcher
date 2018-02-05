package ysaak.wallswitcher;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ysaak.wallswitcher.services.WallpaperManager;
import ysaak.wallswitcher.ui.AppMenu;
import ysaak.wallswitcher.ui.Dialogs;
import ysaak.wallswitcher.ui.creator.CreatorScene;
import ysaak.wallswitcher.data.ScreenDto;
import ysaak.wallswitcher.event.BasicEvent;
import ysaak.wallswitcher.event.SetWallpaperEvent;
import ysaak.wallswitcher.event.ShowWallpaperEvent;
import ysaak.wallswitcher.exception.NoDataFoundException;
import ysaak.wallswitcher.exception.SetWallpaperException;
import ysaak.wallswitcher.services.ProfileUtils;
import ysaak.wallswitcher.services.i18n.I18n;
import ysaak.wallswitcher.services.ScreenUtils;
import ysaak.wallswitcher.services.eventbus.EventBus;
import ysaak.wallswitcher.services.eventbus.Subscribe;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class App extends javafx.application.Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final String ICON_IMAGE = "/img/picture.png";
    private static final int REFRESH_DELAY = 2000;

    private WallpaperManager wallpaperManager;

    private static Stage mainStage;

    private SystemTray tray = null;
    private TrayIcon trayIcon = null;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final transient Object lock = new Object();
    private ScheduledFuture<?> future;
    private transient String currentConfig = "";

    public static Path getAppDirectory() {
        return Paths.get(System.getProperty("user.home"), ".ysk-screen");
    }

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        if (Files.notExists(getAppDirectory())) {
            try {
                Files.createDirectories(getAppDirectory());
            } catch (final IOException e) {
                LOGGER.error("Cannot create application directory", e);
            }
        }

        EventBus.register(this);

        wallpaperManager = new WallpaperManager();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        mainStage.setTitle("ScreenChanger");

        Dialogs.setMainStage(mainStage);
        // updateDisplay(true);
        updateTaskStart();

        Screen.getScreens().addListener((final Change<? extends Screen> c) -> updateTaskStart());

        // Instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);
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

            menu.defaultProfileAvailableProperty().bind(wallpaperManager.defaultProfileAvailableProperty());
            Bindings.bindContent(menu.getAvailableProfiles(), wallpaperManager.getAvailableProfiles());

            trayIcon.setPopupMenu(menu.getPopup());
            // add the application tray icon to the system tray.
            tray.add(trayIcon);
            frame.setResizable(false);
            frame.setVisible(true);
        } catch (java.awt.AWTException | IOException e) {
            LOGGER.error("Unable to init system tray", e);
            exit(-1);
        }
    }

    private void showCreator() {
        CreatorScene editor = new CreatorScene();
        editor.setScreensList(ScreenUtils.toScreenDto(Screen.getScreens()));
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
                updateDisplay(true);
                break;

            case SHOW_CURRENT_PROFILE:
                Platform.runLater(this::showConfigurationId);
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
        final Optional<Path> file = wallpaperManager.getWallpaperPath(event.getProfileId());

        file.ifPresent((data) -> {
            final Desktop dt = Desktop.getDesktop();
            try {
                dt.open(data.toFile());
            } catch (final Exception e) {
                LOGGER.error("Error while opening image " + data, e);
                Dialogs.error(I18n.get("error.wallpaperOpen"), e);
            }
        });
    }

    /**
     * Sets the wallpaper for a specific profile and update it
     *
     * @param event Set wallpaper event
     */
    @Subscribe
    private void handleSetWallpaperEvent(SetWallpaperEvent event) {

        final String profileId = ProfileUtils.CURRENT_PROFILE.equals(event.getProfileId())
                ? currentConfig : event.getProfileId();

        try {
            wallpaperManager.storeWallpaper(event.getWallpaperFile().toPath(), profileId);
            updateDisplay(true);
        }
        catch (final Exception e) {
            Dialogs.error("Error while storing default wallpaper", e);
        }
    }

    /**
     * Shows the current configuration ID
     */
    private void showConfigurationId() {
        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(I18n.get("profileDialog.title"));
        alert.setHeaderText(I18n.get("profileDialog.header"));

        final TextField idField = new TextField();
        idField.setText(currentConfig);
        idField.setEditable(false);
        idField.getStyleClass().add("copyable-label");

        alert.getDialogPane().setContent(idField);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/copyable-text.css").toExternalForm());
        alert.showAndWait();
    }

    /**
     * Update the wallpaper according to calculated profile. The update is canceled if the profile hasn't changed.
     *
     * @param force Force the update of the wallpaper
     */
    private void updateDisplay(final boolean force) {
        final ObservableList<Screen> fxScreens = Screen.getScreens();
        final List<ScreenDto> screens = ScreenUtils.toScreenDto(fxScreens);
        final String configId = ScreenUtils.calculateProfileId(screens);

        if (currentConfig.equals(configId) && !force) {
            // Same config, stop
            return;
        }

        currentConfig = configId;

        try {
            wallpaperManager.showWallpaperForProfile(configId);
        } catch (final NoDataFoundException | SetWallpaperException e) {
            LOGGER.warn(e.getMessage(), e);

            if (trayIcon != null) {
                trayIcon.displayMessage("Error", e.getMessage(), TrayIcon.MessageType.ERROR);
            }
        }
    }

    /**
     * Starts the timer of the wallpaper update.
     *
     * The update is delayed since the list is updated multiple times
     */
    private void updateTaskStart() {
        synchronized (lock) {
            if (future == null || future.isDone()) {
                future = scheduler.schedule(() -> updateDisplay(false), REFRESH_DELAY, TimeUnit.MILLISECONDS);
            }
        }
    }

    public static Stage getMainStage() {
        return mainStage;
    }
}
