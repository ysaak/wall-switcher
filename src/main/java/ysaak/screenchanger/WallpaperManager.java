package ysaak.screenchanger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ysaak.screenchanger.exception.NoDataFoundException;
import ysaak.screenchanger.exception.SetWallpaperException;
import ysaak.screenchanger.platform.IPlatform;
import ysaak.screenchanger.platform.WallpaperDisplayStyle;
import ysaak.screenchanger.platform.WindowsPlatform;
import ysaak.screenchanger.utils.ProfileUtils;
import ysaak.screenchanger.utils.ScreenUtils;

public class WallpaperManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(WallpaperManager.class);

	public static final String DEFAULT_PROFILE = ProfileUtils.DEFAULT_PROFILE;

	private BooleanProperty defaultProfileAvailable = new SimpleBooleanProperty(false);
	private final ObservableList<String> availableProfiles;

	private final IPlatform platform;
	private final Path wallpaperFolder;

	public WallpaperManager() throws Exception {

		platform = new WindowsPlatform();
		wallpaperFolder = platform.getApplicationDirectory().resolve("walls");

		// Create store folder is required
		if (Files.notExists(wallpaperFolder)) {
			Files.createDirectories(wallpaperFolder);
		}

		availableProfiles = FXCollections.observableArrayList();

		// Parse folder
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(wallpaperFolder)) {
			for (final Path path : directoryStream) {

				final String profile = path.getFileName().toString().replaceFirst("[.][^.]+$", "");

				if (DEFAULT_PROFILE.equals(profile)) {
					defaultProfileAvailable.set(true);
					;
				} else {
					availableProfiles.add(profile);
				}
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
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
		} else if (!DEFAULT_PROFILE.equals(profile)) {
			LOGGER.debug("No wallpaper found, try with default");
			try {
				showWallpaperForProfile(DEFAULT_PROFILE);
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
	 * @param wallpaper
	 *            Wallpaper to copy
	 * @param id
	 *            Profile ID
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
}
