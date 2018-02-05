package ysaak.wallswitcher.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.stage.Screen;
import ysaak.wallswitcher.data.ScreenDto;

public class ScreenUtils {
	private static final Pattern SCREEN_PATTERN = Pattern.compile("^([0-9])+-");
	
	public static List<ScreenDto> toScreenDto(List<Screen> fxScreens) {
		List<ScreenDto> screens = new ArrayList<>();

		int id = 1;

		for (Screen s : fxScreens)
			screens.add(new ScreenDto(id++, s));
		return screens;
	}
	
	public static String calculateProfileId(List<ScreenDto> screens) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(screens.size());
		
		for (ScreenDto screen : screens) {
			builder.append('-')
				.append(screen.getX()).append('x')
				.append(screen.getY()).append('-')
				.append(screen.getWidth()).append('x')
				.append(screen.getHeight());
		}
		
		return builder.toString();
	}
	
	public static int getNbScreens(String profile) {
		final Matcher matcher = SCREEN_PATTERN.matcher(profile);
		if (matcher.find()) {
			final String nbStr = matcher.group(1);
			return Integer.parseInt(nbStr); 
		}
		
		return 1;
	}
}
