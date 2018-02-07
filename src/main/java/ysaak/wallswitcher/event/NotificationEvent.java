package ysaak.wallswitcher.event;

public class NotificationEvent {
    public enum NotificationType {
        ERROR
    }

    private final NotificationType type;
    private final String errorCode;

    public NotificationEvent(NotificationType type, String errorCode) {
        this.type = type;
        this.errorCode = errorCode;
    }

    public NotificationType getType() {
        return type;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
