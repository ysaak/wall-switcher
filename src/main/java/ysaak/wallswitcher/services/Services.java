package ysaak.wallswitcher.services;

import ysaak.wallswitcher.services.profile.ProfileService;
import ysaak.wallswitcher.services.wallpaper.WallpaperService;

public class Services {
    private static WallpaperService wallpaperService = null;
    private static ProfileService profileService = null;

    public static WallpaperService getWallpaperService() {
        if (wallpaperService == null) {
            wallpaperService = new WallpaperService();
        }
        return wallpaperService;
    }

    public static ProfileService getProfileService() {
        if (profileService == null) {
            profileService = new ProfileService();
        }
        return profileService;
    }
}
