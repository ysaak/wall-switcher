package ysaak.wallswitcher.task;

import javafx.concurrent.Task;
import ysaak.wallswitcher.data.Profile;
import ysaak.wallswitcher.services.Services;
import ysaak.wallswitcher.services.profile.ProfileService;
import ysaak.wallswitcher.services.wallpaper.WallpaperService;

import java.io.File;

public class SetWallpaperTask extends Task<Void> {

    private final WallpaperService wallpaperService;
    private final ProfileService profileService;

    private final String profileId;
    private final File wallpaperFile;

    public SetWallpaperTask(String profileId, File wallpaperFile) {
        this.wallpaperService = Services.getWallpaperService();
        this.profileService = Services.getProfileService();

        this.profileId = profileId;
        this.wallpaperFile = wallpaperFile;

        updateTitle("SetWallpaperTask");
    }

    @Override
    protected Void call() throws Exception {
        wallpaperService.storeWallpaper(wallpaperFile.toPath(), profileId);

        boolean changeWallpaper = true;

        if (Profile.DEFAULT_PROFILE_ID.equals(profileId)) {
            final String currentProfileId = profileService.getCurrentProfileId();
            changeWallpaper = !wallpaperService.existsWallpaperForProfileId(currentProfileId);
        }

        if (changeWallpaper) {
            wallpaperService.showWallpaperForProfile(profileId);
        }
        return null;
    }
}
