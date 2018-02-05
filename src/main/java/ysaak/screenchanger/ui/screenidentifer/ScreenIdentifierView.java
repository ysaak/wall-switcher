package ysaak.screenchanger.ui.screenidentifer;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import ysaak.screenchanger.App;
import ysaak.screenchanger.data.ScreenDto;

import java.util.ArrayList;
import java.util.List;

public final class ScreenIdentifierView {
    private static final Duration VISIBILITY_DURATION = Duration.seconds(5);
    private static final int FADE_OUT_DURATION = 300;
    private static final int BASE_PADDING = 20;

    private static final double CENTER_ON_SCREEN_X_FRACTION = 1.0f / 2;
    private static final double CENTER_ON_SCREEN_Y_FRACTION = 1.0f / 3;

    private static final BooleanProperty SCREEN_IDENTIFIERS_VISIBLE = new SimpleBooleanProperty(false);

    public static void showScreenIdentifier(List<ScreenDto> screens) {
        SCREEN_IDENTIFIERS_VISIBLE.setValue(true);

        final List<Window> windowList = new ArrayList<>();
        screens.forEach(screen -> windowList.add(createAndShowStage(screen)));

        final PauseTransition delay = new PauseTransition(VISIBILITY_DURATION);
        delay.setOnFinished( event -> {
            windowList.forEach(ScreenIdentifierView::closeWindow);
            SCREEN_IDENTIFIERS_VISIBLE.setValue(false);
        });
        delay.play();
    }

    public static ReadOnlyBooleanProperty visibleProperty() {
        return SCREEN_IDENTIFIERS_VISIBLE;
    }

    private static Popup createAndShowStage(ScreenDto screen) {
        // Screen ID
        Text text = new Text("" + screen.getId());
        text.setBoundsType(TextBoundsType.VISUAL);
        text.setStyle("-fx-font-size: 200pt; -fx-font-family: \"Courier New\";");

        // Background circle
        Circle circle = new Circle();
        circle.setFill(Color.web("#b8daff"));
        circle.setRadius(getNodeMaxDimension(text) / 2 + BASE_PADDING);

        StackPane layout = new StackPane();
        layout.getChildren().addAll(circle, text);
        layout.setPadding(new Insets(BASE_PADDING));
        layout.setStyle("-fx-background-color: rgba(255, 255, 255, 0.0);");


        final Popup stage = new Popup();
        stage.getScene().setRoot(layout);

        double size = (circle.getRadius() * 2) + 40;
        stage.setWidth(size);
        stage.setHeight(size);
        stage.setX(screen.getX() + (screen.getWidth() - size) * CENTER_ON_SCREEN_X_FRACTION);
        stage.setY(screen.getY() + (screen.getHeight() - size) * CENTER_ON_SCREEN_Y_FRACTION);

        stage.show(App.getMainStage());

        return stage;
    }

    private static double getNodeMaxDimension(Node node) {
        new Scene(new Group(node));
        node.applyCss();

        Bounds bounds = node.getLayoutBounds();

        return Math.max(bounds.getWidth(), bounds.getHeight());
    }

    private static void closeWindow(Window window) {
        final FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_OUT_DURATION));
        fadeOut.setNode(window.getScene().getRoot());
        fadeOut.setFromValue(1.d);
        fadeOut.setToValue(0.d);
        fadeOut.setCycleCount(1);
        fadeOut.setAutoReverse(false);
        fadeOut.setOnFinished(event -> window.hide());
        fadeOut.playFromStart();
    }
}
