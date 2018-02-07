package ysaak.wallswitcher.ui.notification;

import javafx.animation.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;
import ysaak.wallswitcher.App;
import ysaak.wallswitcher.data.notification.TrayNotification;

import java.util.ArrayDeque;
import java.util.Queue;

public class NotificationEngine {
    private static final Duration FADE_IN_DURATION = Duration.millis(300);
    private static final Duration FADE_OUT_DURATION = Duration.millis(300);
    private static final Duration DEFAULT_PAUSE_DURATION = Duration.seconds(5);

    private final Queue<TrayNotification> queue;
    private TrayNotification current = null;
    private SequentialTransition transition = null;

    public NotificationEngine() {
        queue = new ArrayDeque<>();
    }

    public synchronized void show(TrayNotification notification) {
        queue.offer(notification);

        if (current == null) {
            pickNotification();
        }
    }

    private synchronized void pickNotification() {
        current = queue.poll();


        if (current != null) {
            System.err.println("aaa > " + current);
            final Popup popup = createAndShowStage(current);

            FadeTransition fadeInTransition = new FadeTransition(FADE_IN_DURATION);
            fadeInTransition.setFromValue(0.0);
            fadeInTransition.setToValue(1.0);

            PauseTransition pauseTransition = new PauseTransition(current.getDuration() != null ? current.getDuration() : DEFAULT_PAUSE_DURATION);

            transition = new SequentialTransition(popup.getScene().getRoot(), fadeInTransition, pauseTransition);
            transition.setOnFinished(evt -> closeNotification(popup));
            transition.play();
        }
    }

    private void closeNotification(Popup popup) {
        System.err.println("bbb > " + current);

        if (transition != null) {
            transition.stop();
            transition = null;
        }

        FadeTransition fadeOutTransition = new FadeTransition(FADE_OUT_DURATION, popup.getScene().getRoot());
        fadeOutTransition.setFromValue(1.0);
        fadeOutTransition.setToValue(0.0);
        fadeOutTransition.setOnFinished(evt -> pickNotification());
    }

    private Popup createAndShowStage(final TrayNotification notification) {

        Rectangle rectangleColor = new Rectangle(32, 85, Paint.valueOf(notification.getType().getPaintHex()));
        rectangleColor.setArcWidth(5);
        rectangleColor.setArcHeight(5);
        AnchorPane.setTopAnchor(rectangleColor, 1.);
        AnchorPane.setLeftAnchor(rectangleColor, 1.);

        ImageView imageIcon = new ImageView();
        imageIcon.setImage(new Image(getClass().getClassLoader().getResource(notification.getType().getURLResource()).toString()));
        imageIcon.setFitWidth(67.);
        imageIcon.setFitHeight(67.0);
        imageIcon.setPickOnBounds(true);
        AnchorPane.setTopAnchor(imageIcon, 9.);
        AnchorPane.setLeftAnchor(imageIcon, 47.);

        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        AnchorPane.setTopAnchor(titleLabel, 9.);
        AnchorPane.setLeftAnchor(titleLabel, 126.);

        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px;");
        AnchorPane.setTopAnchor(messageLabel, 52.);
        AnchorPane.setLeftAnchor(messageLabel, 126.);

        Label closeLabel = new Label("x");
        closeLabel.setStyle("-fx-font-size: 20px;");
        closeLabel.setCursor(Cursor.HAND);
        AnchorPane.setTopAnchor(closeLabel, 9.);
        AnchorPane.setLeftAnchor(closeLabel, 441.);

        AnchorPane rootNode = new AnchorPane(rectangleColor, imageIcon, titleLabel, messageLabel, closeLabel);
        rootNode.setPrefSize(461, 87);
        rootNode.setStyle("-fx-border-color: gray; -fx-border-width: 1;");

        final Popup stage = new Popup();
        stage.getScene().setRoot(rootNode);

        stage.setWidth(rootNode.getPrefWidth());
        stage.setHeight(rootNode.getPrefHeight());

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double x = screenBounds.getMinX() + screenBounds.getWidth() - rootNode.getPrefWidth() - 2;
        double y = screenBounds.getMinY() + screenBounds.getHeight() - rootNode.getPrefHeight() - 2;

        stage.setX(x);
        stage.setY(y);

        closeLabel.setOnMouseReleased(evt -> closeNotification(stage));

        stage.show(App.getMainStage());

        return stage;
    }
}
