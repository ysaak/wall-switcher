package ysaak.screenchanger.platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ysaak.screenchanger.App;
import ysaak.screenchanger.exception.NoDataFoundException;
import ysaak.screenchanger.exception.SetWallpaperException;
import ysaak.screenchanger.exception.SetWallpaperException.ErrorCode;

public final class WindowsPlatform implements IPlatform {

	private static class StreamGobbler extends Thread {
		InputStream is;
		String type;

		StreamGobbler(final InputStream is, final String type) {
			this.is = is;
			this.type = type;
		}

		@Override
		public void run() {
			try {
				final InputStreamReader isr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					LOGGER.info(type + ">" + line);
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(WindowsPlatform.class);

	@Override
	public Path getApplicationDirectory() {
		return Paths.get(System.getProperty("user.home"), ".ysk-screen");
	}

	@Override
	public void setWallpaper(final Path wallpaper) throws SetWallpaperException {
		setWallpaper(wallpaper, WallpaperDisplayStyle.NOCHANGE);
	}

	@Override
	public void setWallpaper(final Path wallpaper, final WallpaperDisplayStyle style) throws SetWallpaperException {
		LOGGER.debug("START setWallpaper([" + wallpaper.getFileName() + ", " + style + "])");

		final Path tempWallpaper = App.getAppDirectory().resolve("_wallpaper.png");

		// Copy wallpaper to temp location

		Path realFile = wallpaper;

		if (wallpaper.toString().endsWith(".lnk")) {
			// Windows link
			try {
				final WindowsShortcut link = new WindowsShortcut(wallpaper.toFile());
				realFile = Paths.get(link.getRealFilename());
			} catch (IOException | ParseException e) {
				e.printStackTrace();
				realFile = null;
				return;
			}
		}

		try {
			Files.delete(tempWallpaper);
			Files.copy(realFile, tempWallpaper);
		} catch (final IOException e1) {
			e1.printStackTrace();
			return;
		}

		// Generate ouput file
		final Path outfile = App.getAppDirectory().resolve("wallsetter.ps1");

		try {
			exportResource("/wallsetter.ps1", outfile.toFile());
		} catch (final Exception e) {
			throw new SetWallpaperException(ErrorCode.CREATE_SCRIPT, "Error while coping script", e);
		}

		// Exec file
		Process setterProcess = null;
		try {
			setterProcess = Runtime.getRuntime().exec(
					new String[] { "powershell.exe", "-file", outfile.toString(), tempWallpaper.toString(), style.getPsCode() });

			final StreamGobbler errorGobbler = new StreamGobbler(setterProcess.getErrorStream(), "ERROR");
			final StreamGobbler outputGobbler = new StreamGobbler(setterProcess.getInputStream(), "OUTPUT");

			outputGobbler.start();
			errorGobbler.start();

			setterProcess.waitFor();
		} catch (final IOException | InterruptedException e) {
			throw new SetWallpaperException(ErrorCode.CREATE_SCRIPT, "Error while executing script", e);
		}
	}

	private void exportResource(final String resourceName, final File outputFile) throws NoDataFoundException, IOException {
		InputStream stream = null;
		OutputStream resStreamOut = null;

		try {
			stream = WindowsPlatform.class.getResourceAsStream(resourceName);
			if (stream == null) {
				throw new NoDataFoundException("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			final byte[] buffer = new byte[4096];

			resStreamOut = new FileOutputStream(outputFile);
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} finally {
			stream.close();
			resStreamOut.close();
		}
	}
}
