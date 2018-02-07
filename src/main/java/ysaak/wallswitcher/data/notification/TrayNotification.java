package ysaak.wallswitcher.data.notification;

import javafx.util.Duration;

public class TrayNotification {
    private NotificationType type;
    private String title;
    private String message;

    private Duration duration;

    public NotificationType getType() {
        return type;
    }

    public void setType(final NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(final Duration duration) {
        this.duration = duration;
    }
}
