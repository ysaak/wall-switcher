package ysaak.wallswitcher.task;

import javafx.concurrent.Task;
import ysaak.wallswitcher.data.Profile;
import ysaak.wallswitcher.services.Services;
import ysaak.wallswitcher.ui.Dialogs;
import ysaak.wallswitcher.ui.i18n.I18n;

import java.awt.Desktop;
import java.nio.file.Path;
import java.util.Optional;

public class ShowWallpaperTask extends Task<Void> {
    private final String profileId;

    public ShowWallpaperTask(String profileId) {
        this.profileId = profileId;
    }

    @Override
    protected Void call() throws Exception {
        final Optional<Path> file = Services.getWallpaperService().getWallpaperPath(profileId);

        if (file.isPresent()) {
            final Desktop dt = Desktop.getDesktop();
            dt.open(file.get().toFile());
        }
        return null;
    }
}
