package ysaak.wallswitcher.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.stage.Screen;
import ysaak.wallswitcher.data.ScreenDto;

public class ScreenUtils {
	private static final Pattern SCREEN_PATTERN = Pattern.compile("^([0-9])+-");

	

	
	public static int getNbScreens(String profile) {
		final Matcher matcher = SCREEN_PATTERN.matcher(profile);
		if (matcher.find()) {
			final String nbStr = matcher.group(1);
			return Integer.parseInt(nbStr); 
		}
		
		return 1;
	}
}
