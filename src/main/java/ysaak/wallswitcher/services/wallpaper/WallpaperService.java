package ysaak.wallswitcher.services.wallpaper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ysaak.wallswitcher.App;
import ysaak.wallswitcher.event.NewProfileDetectedEvent;
import ysaak.wallswitcher.event.NotificationEvent;
import ysaak.wallswitcher.exception.NoDataFoundException;
import ysaak.wallswitcher.exception.ServiceInitException;
import ysaak.wallswitcher.exception.SetWallpaperException;
import ysaak.wallswitcher.platform.IPlatform;
import ysaak.wallswitcher.platform.WallpaperDisplayStyle;
import ysaak.wallswitcher.platform.WindowsPlatform;
import ysaak.wallswitcher.services.ProfileUtils;
import ysaak.wallswitcher.services.ScreenUtils;
import ysaak.wallswitcher.services.eventbus.EventBus;
import ysaak.wallswitcher.services.eventbus.Subscribe;

public class WallpaperService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WallpaperService.class);

    private final BooleanProperty defaultProfileAvailable = new SimpleBooleanProperty(false);
    private final ObservableList<String> availableProfiles;

    private final IPlatform platform;
    private final Path wallpaperFolder;

    public WallpaperService() {
        platform = new WindowsPlatform();
        wallpaperFolder = App.getAppDirectory().resolve("walls");
        availableProfiles = FXCollections.observableArrayList();
    }

    public void init() throws ServiceInitException {
        // Create store folder is required
        if (Files.notExists(wallpaperFolder)) {
            try {
                Files.createDirectories(wallpaperFolder);
            }
            catch (IOException e) {
                LOGGER.error("Error while creating store directory", e);
                throw new ServiceInitException("Error while creating store directory", e);
            }
        }

        // Parse folder
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(wallpaperFolder)) {
            for (final Path path : directoryStream) {

                final String profile = path.getFileName().toString().replaceFirst("[.][^.]+$", "");

                if (ProfileUtils.DEFAULT_PROFILE.equals(profile)) {
                    defaultProfileAvailable.set(true);
                } else {
                    availableProfiles.add(profile);
                }
            }
        }
        catch (final IOException e) {
            LOGGER.error("Error while reading store directory", e);
            throw new ServiceInitException("Error while reading store directory", e);
        }
    }

    public BooleanProperty defaultProfileAvailableProperty() {
        return defaultProfileAvailable;
    }

    public ObservableList<String> getAvailableProfiles() {
        return availableProfiles;
    }

    public Optional<Path> getWallpaperPath(final String profileId) {
        Path wallpaper = wallpaperFolder.resolve(profileId + ".png");

        if (!Files.exists(wallpaper) || !Files.isReadable(wallpaper)) {

            wallpaper = wallpaperFolder.resolve(profileId + ".lnk");

            if (!Files.exists(wallpaper) || !Files.isReadable(wallpaper)) {
                return Optional.empty();
            }
        }

        return Optional.of(wallpaper);
    }

    public void showWallpaperForProfile(final String profile) throws NoDataFoundException, SetWallpaperException {
        final Optional<Path> wallpaper = getWallpaperPath(profile);

        if (wallpaper.isPresent()) {
            // Set wallpaper
            LOGGER.debug("Set wallpaper " + wallpaper.get().toString());

            // Find style for the wallpaper
            final WallpaperDisplayStyle style = (ScreenUtils.getNbScreens(profile) > 1) ? WallpaperDisplayStyle.TILE
                    : WallpaperDisplayStyle.STRETCH;

            platform.setWallpaper(wallpaper.get(), style);
        } else if (!ProfileUtils.DEFAULT_PROFILE.equals(profile)) {
            LOGGER.debug("No wallpaper found, try with default");
            try {
                showWallpaperForProfile(ProfileUtils.DEFAULT_PROFILE);
            } catch (final NoDataFoundException e) {
                LOGGER.debug("No default wallpaper set");
                throw new NoDataFoundException("No wallpaper found for id " + profile, e);
            }
        } else {
            throw new NoDataFoundException("No wallpaper found for id " + profile);
        }
    }

    /**
     * Copy selected wallpaper in application directory
     *
     * @param wallpaper Wallpaper to copy
     * @param id        Profile ID
     * @throws IOException
     */
    public void storeWallpaper(final Path wallpaper, final String id) throws IOException {

        if (Files.notExists(wallpaperFolder)) {
            Files.createDirectories(wallpaperFolder);
        }

        final Path newLocation = wallpaperFolder.resolve(id + ".png");

        if (Files.exists(newLocation)) {
            Files.delete(newLocation);
        }

        Files.copy(wallpaper, newLocation);
    }

    /**
     * Indicates if a wallpaper is stored for the given profile ID
     * @param profileId Profile ID
     * @return TRUE if a wallpaper is stored - FALSE otherwise
     */
    public boolean existsWallpaperForProfileId(String profileId) {
        return availableProfiles.contains(profileId);
    }
}
