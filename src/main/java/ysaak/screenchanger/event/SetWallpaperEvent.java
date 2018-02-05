package ysaak.screenchanger.event;

import java.io.File;

/**
 * Set wallpaperFile event
 */
public class SetWallpaperEvent implements ActionEvent {
    private final String profileId;
    private final File wallpaperFile;

    public SetWallpaperEvent(String profileId, File wallpaper) {
        this.profileId = profileId;
        this.wallpaperFile = wallpaper;
    }

    public String getProfileId() {
        return profileId;
    }

    public File getWallpaperFile() {
        return wallpaperFile;
    }
}
