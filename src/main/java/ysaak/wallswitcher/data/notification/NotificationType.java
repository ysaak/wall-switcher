package ysaak.wallswitcher.data.notification;

public enum NotificationType {
    INFORMATION("img/tray_info.png", "#2C54AB"),
    NOTICE("img/tray_notice.png", "#8D9695"),
    SUCCESS("img/tray_success.png", "#009961"),
    WARNING("img/tray_warning.png", "#E23E0A"),
    ERROR("img/tray_error.png", "#CC0033");

    private final String urlResource;
    private final String paintHex;

    NotificationType(String urlResource, String paintHex) {
        this.urlResource = urlResource;
        this.paintHex = paintHex;
    }

    public String getURLResource() {
        return urlResource;
    }

    public String getPaintHex() {
        return paintHex;
    }
}
