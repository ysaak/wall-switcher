package ysaak.wallswitcher.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import ysaak.wallswitcher.services.config.Config;
import ysaak.wallswitcher.ui.i18n.I18n;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public final class Dialogs {
    private static Stage mainStage = null;

    public static void error(final String contentText) {
        error(null, contentText);
    }

    public static void error(final String headerText, final String contentText) {
        openBasicAlert(AlertType.ERROR, I18n.get("common.dialogs.error"), headerText, contentText, null);
    }

    public static void error(final String headerText, final String contentText, final Throwable error) {
        // Create expandable Exception.
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        final String exceptionText = sw.toString();

        final Label label = new Label(I18n.get("common.dialogs.error.stacktrace"));
        label.setPadding(new Insets(0, 0, 10, 0));

        final TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        final GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        openBasicAlert(AlertType.ERROR, I18n.get("common.dialogs.error"), headerText, contentText, expContent);
    }

    public static boolean confirm(final String contentText) {
        return confirm(null, contentText);
    }

    public static boolean confirm(final String headerText, final String contentText) {

        final ButtonType yesBtn = new ButtonType(I18n.get("common.dialogs.confirm.yes"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType noBtn = new ButtonType(I18n.get("common.dialogs.confirm.no"), ButtonBar.ButtonData.CANCEL_CLOSE);

        Optional<ButtonType> result = openBasicAlert(AlertType.CONFIRMATION, I18n.get("common.dialogs.confirm"), headerText, contentText, null, yesBtn, noBtn);

        return (result.isPresent() && result.get() == yesBtn);
    }

    public static void error(final String contentText, final Throwable error) {
        error(null, contentText, error);
    }

    public static Optional<File> openFileChooser() {
        final String storedDirectory = Config.get("chooser.open.location");

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.get("common.dialogs.chooser.openFile"));

        File initialDirectory = new File(storedDirectory != null ? storedDirectory : System.getProperty("user.home"));
        if (initialDirectory.exists() && initialDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter(I18n.get("common.dialogs.chooser.filter.images"), "*.png", "*.jpg"));
        Optional<File> file = Optional.ofNullable(fileChooser.showOpenDialog(mainStage));

        file.ifPresent(f -> {
            if (storedDirectory == null || !storedDirectory.equals(f.getParentFile().getAbsolutePath())) {
                Config.set("chooser.open.location", f.getParentFile().getAbsolutePath());
            }
        });

        return file;
    }

    public static Optional<File> saveFileChooser() {
        final String storedDirectory = Config.get("chooser.save.location");

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.get("common.dialogs.chooser.saveFile"));

        File initialDirectory = new File(storedDirectory != null ? storedDirectory : System.getProperty("user.home"));
        if (initialDirectory.exists() && initialDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        fileChooser.getExtensionFilters().addAll(new ExtensionFilter(I18n.get("common.dialogs.chooser.filter.images"), "*.png"));
        Optional<File> file = Optional.ofNullable(fileChooser.showSaveDialog(mainStage));

        file.ifPresent(f -> {
            if (storedDirectory == null || !storedDirectory.equals(f.getParentFile().getAbsolutePath())) {
                Config.set("chooser.save.location", f.getParentFile().getAbsolutePath());
            }
        });

        return file;
    }

    public static void setMainStage(final Stage mainStage) {
        Dialogs.mainStage = mainStage;
    }

    private static Optional<ButtonType> openBasicAlert(final AlertType type, final String title, final String headerText,
            final String contentText, final Node expandableContent, ButtonType...buttonTypes) {
        final Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        if (expandableContent != null) {
            alert.getDialogPane().setExpandableContent(expandableContent);
        }
        if (buttonTypes != null && buttonTypes.length > 0) {
            alert.getButtonTypes().setAll(buttonTypes);
        }

        return alert.showAndWait();
    }

}
