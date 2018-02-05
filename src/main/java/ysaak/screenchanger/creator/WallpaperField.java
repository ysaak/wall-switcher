package ysaak.screenchanger.creator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import ysaak.screenchanger.Dialogs;

import java.io.File;
import java.util.Optional;

/**
 * Wallpaper field
 */
public class WallpaperField {

    private final HBox rootPane;

    private final TextField fileField;

    private final ColorPicker backgroundField;

    private ObjectProperty<File> selectedFile = new SimpleObjectProperty<>(null);

    WallpaperField() {
        fileField = new TextField();
        fileField.setEditable(false);
        selectedFile.addListener((observable, oldValue, newValue) -> fileField.setText(newValue.getAbsolutePath()));

        Button selectFileButton = new Button("Choose file");
        selectFileButton.setOnAction(event -> selectFile());

        backgroundField = new ColorPicker(Color.BLACK);

        rootPane = new HBox(10);
        rootPane.setAlignment(Pos.CENTER_LEFT);
        rootPane.getChildren().addAll(
                new Label("Image:"), fileField, selectFileButton,

                new Label("Background color:"), backgroundField
        );
    }

    private void selectFile() {
        Optional<File> file = Dialogs.openFileChooser();
        file.ifPresent(selectedFile::set);
    }

    Node getPane() {
        return rootPane;
    }

    public File getSelectedFile() {
        return selectedFile.get();
    }

    public ReadOnlyObjectProperty<File> selectedFileProperty() {
        return selectedFile;
    }

    public Color getBackgroundColor() {
        return backgroundField.getValue();
    }

    public ReadOnlyObjectProperty<Color> backgroundColorProperty() {
        return backgroundField.valueProperty();
    }
}
