package ysaak.wallswitcher.ui.tray;

import javafx.application.Platform;
import ysaak.wallswitcher.data.notification.NotificationType;
import ysaak.wallswitcher.data.notification.TrayNotification;
import ysaak.wallswitcher.event.NotificationEvent;
import ysaak.wallswitcher.services.eventbus.EventBus;
import ysaak.wallswitcher.services.eventbus.Subscribe;
import ysaak.wallswitcher.ui.notification.NotificationEngine;

public class TrayController {

    private final NotificationEngine notificationEngine;
    private final TrayView trayView;

    public TrayController() {

        notificationEngine = new NotificationEngine();

        EventBus.register(this);
    }


    @Subscribe
    private void handleNotificationEvent(final NotificationEvent event) {
        final TrayNotification notification = new TrayNotification();

        if (event.getType() == NotificationEvent.NotificationType.ERROR) {
            notification.setType(NotificationType.ERROR);
            notification.setTitle("Error"); // TODO translation
        }
        else {
            notification.setType(NotificationType.NOTICE);
            notification.setTitle("Unknown"); // TODO translation
        }

        notification.setMessage(event.getErrorCode()); // TODO translation

        Platform.runLater(() -> notificationEngine.show(notification));
    }

}
