package ysaak.wallswitcher.platform;

import java.nio.file.Path;

import ysaak.wallswitcher.exception.SetWallpaperException;

/**
 * Interface of a platform
 *
 * @author ROTHDA
 */
public interface IPlatform {

	/**
	 * Returns the directory in which the application store data
	 *
	 * @return the directory in which the application store data
	 */
	Path getApplicationDirectory();

	/**
	 * Sets the wallpaper given in parameter with no change of the display style
	 *
	 * @param wallpaper
	 *            Wallpaper path
	 * @throws SetWallpaperException
	 *             Thrown if an error occurs during the process
	 */
	void setWallpaper(final Path wallpaper) throws SetWallpaperException;

	/**
	 * Sets the wallpaper given in parameter with a specific display style
	 *
	 * @param wallpaper
	 *            Wallpaper path
	 * @param style
	 *            Display style
	 * @throws SetWallpaperException
	 *             Thrown if an error occurs during the process
	 */
	void setWallpaper(final Path wallpaper, final WallpaperDisplayStyle style) throws SetWallpaperException;
}
