package ysaak.wallswitcher.ui.creator;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ysaak.wallswitcher.ui.Dialogs;
import ysaak.wallswitcher.data.ScreenDto;
import ysaak.wallswitcher.data.WallpaperPartAttribute;
import ysaak.wallswitcher.event.SetWallpaperEvent;
import ysaak.wallswitcher.ui.screenidentifer.ScreenIdentifierView;
import ysaak.wallswitcher.services.ProfileUtils;
import ysaak.wallswitcher.services.eventbus.EventBus;
import ysaak.wallswitcher.services.i18n.I18n;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Wallpaper creator scene
 */
public class CreatorScene {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreatorScene.class);

    private final BorderPane pane;
    private final BorderPane topPane;

    private final ToggleGroup toggleGroup;
    private final HBox screenPane;

    private ImageView imageView;

    private final Map<ScreenDto, WallpaperField> wallpaperFields = new HashMap<>();

    private ChangeListener<Object> changeListener = ((observable, oldValue, newValue) -> startGenerate(GenerateMode.PREVIEW));

    private final BooleanProperty previewNotGenerated = new SimpleBooleanProperty(true);

    private final List<ScreenDto> screenList = new ArrayList<>();

    public CreatorScene() {
        toggleGroup = new ToggleGroup();
        screenPane = new HBox(10);
        screenPane.setPadding(new Insets(10));

        Button showIdButton = new Button("Identify screens");
        showIdButton.disableProperty().bind(ScreenIdentifierView.visibleProperty());
        showIdButton.setOnAction(event -> ScreenIdentifierView.showScreenIdentifier(screenList));

        topPane = new BorderPane();
        topPane.setLeft(screenPane);
        topPane.setRight(showIdButton);

        imageView = new ImageView();
        Pane imagePane = new BorderPane(imageView);
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(imagePane.widthProperty());
        imageView.fitHeightProperty().bind(imagePane.heightProperty());

        // Button pane
        Button saveButton = new Button(I18n.get("creator.actions.save"));
        saveButton.disableProperty().bind(previewNotGenerated);
        saveButton.setOnAction(event -> startGenerate(GenerateMode.SAVE));

        Button saveAndSetButton = new Button(I18n.get("creator.actions.saveAndSet"));
        saveAndSetButton.disableProperty().bind(previewNotGenerated);
        saveAndSetButton.setOnAction(event -> startGenerate(GenerateMode.SAVE_AND_SET));

        HBox actionPane = new HBox(10, saveButton, saveAndSetButton);
        actionPane.setAlignment(Pos.CENTER_RIGHT);
        actionPane.setPadding(new Insets(10));

        pane = new BorderPane();
        pane.setTop(topPane);
        pane.setCenter(imagePane);
        pane.setBottom(actionPane);

        pane.setMinSize(600, 400);
        pane.setPadding(new Insets(10));
    }

    public void setScreensList(List<ScreenDto> screenList) {
        this.screenList.clear();
        this.screenList.addAll(screenList);

        toggleGroup.getToggles().clear();
        screenPane.getChildren().clear();
        wallpaperFields.forEach((screen, field) -> {
            field.backgroundColorProperty().removeListener(changeListener);
            field.selectedFileProperty().removeListener(changeListener);
        });
        wallpaperFields.clear();

        int index = 0;

        RadioButton firstButton = null;

        for (final ScreenDto screen : this.screenList) {
            RadioButton btn = new RadioButton("Screen " + ++index);
            btn.setUserData(screen);
            btn.setOnAction(event -> {
                WallpaperField field = wallpaperFields.get(screen);
                topPane.setBottom(field.getPane());
            });

            toggleGroup.getToggles().add(btn);

            WallpaperField field = new WallpaperField();
            field.selectedFileProperty().addListener(changeListener);
            field.backgroundColorProperty().addListener(changeListener);

            wallpaperFields.put(screen, field);
            screenPane.getChildren().add(btn);

            if (firstButton == null) {
                firstButton = btn;
            }
        }

        if (firstButton != null) {
            firstButton.fire();
        }
    }

    private void startGenerate(final GenerateMode mode) {
        final File outputImageFile;

        if (mode == GenerateMode.SAVE || mode == GenerateMode.SAVE_AND_SET) {
            // Ask for file path in save mode
            Optional<File> outputFile = Dialogs.saveFileChooser();
            if (outputFile.isPresent()) {
                // Check filename ends with the good extension
                if (!outputFile.get().getAbsolutePath().endsWith(".png")) {
                    outputImageFile = new File(outputFile.get().getAbsolutePath() + ".png");
                }
                else {
                    outputImageFile = outputFile.get();
                }

                if (outputImageFile.exists()) {
                    // Ask for confirmation if file exists
                    boolean overwrite = Dialogs.confirm(I18n.get("creator.save.confirmOverwriteFile"));
                    if (!overwrite) {
                        // Stop action : user don't want to overwrite file
                        return;
                    }
                }
            }
            else {
                // Stop action : no file selected
                return;
            }
        }
        else {
            outputImageFile = null;
        }

        new Thread(() -> generateImage(mode, outputImageFile)).start();
    }

    private void generateImage(final GenerateMode mode, final File outputFile) {
        LOGGER.debug("Start image generation with mode {}", mode);

        // Build the different parts of wallpaper
        final List<WallpaperPartAttribute> parts = wallpaperFields.entrySet().stream()
                .map(entry -> new WallpaperPartAttribute(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        try {
            // Generate full image
            BufferedImage image = WallpaperGenerator.generate(parts);

            if (mode == GenerateMode.PREVIEW) {
                // Preview mode : resize image to match view pane
                BufferedImage resize = Scalr.resize(image, ((int) imageView.getFitWidth()), ((int) imageView.getFitHeight()));
                image.flush();

                // Set image
                Platform.runLater(() -> {
                    imageView.setImage(SwingFXUtils.toFXImage(resize, null));
                    previewNotGenerated.set(false);
                });
            }
            else {
                // For both SAVE and SAVE_AND_SET mode, the image is stored
                ImageIO.write(image, "png", outputFile);

                if (mode == GenerateMode.SAVE_AND_SET) {
                    EventBus.post(new SetWallpaperEvent(ProfileUtils.CURRENT_PROFILE, outputFile));
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("Error while generating image", e);
            Platform.runLater(() -> {
                Dialogs.error(I18n.get("creator.error.generation"), e);

                if (mode == GenerateMode.PREVIEW) {
                    Platform.runLater(() -> previewNotGenerated.set(true));
                }
            });
        }

        LOGGER.debug("End of image generation with mode {}", mode);
    }

    public BorderPane getPane() {
        return pane;
    }


    private enum GenerateMode {
        SAVE,
        SAVE_AND_SET,
        PREVIEW
    }
}
