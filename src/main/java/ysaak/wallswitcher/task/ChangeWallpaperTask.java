package ysaak.wallswitcher.task;

import javafx.concurrent.Task;
import ysaak.wallswitcher.data.Profile;
import ysaak.wallswitcher.services.Services;

public class ChangeWallpaperTask extends Task<Void> {
    private final String profileId;

    public ChangeWallpaperTask(String profileId) {

        this.profileId = profileId;
    }

    @Override
    protected Void call() throws Exception {
        final String newProfileId;

        if (profileId != null) {
            newProfileId = profileId;
        }
        else {
            final Profile profile = Services.getProfileService().getCurrentProfile();
            newProfileId = profile.getId();
        }

        Services.getWallpaperService().showWallpaperForProfile(newProfileId);
        return null;
    }
}
