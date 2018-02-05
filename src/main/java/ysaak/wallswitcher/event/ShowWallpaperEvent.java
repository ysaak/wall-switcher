package ysaak.wallswitcher.event;

/**
 * Show current profile id event
 */
public class ShowWallpaperEvent implements ActionEvent {
    private final String profileId;

    public ShowWallpaperEvent(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileId() {
        return profileId;
    }
}
