package ysaak.wallswitcher.data;

import javafx.scene.paint.Color;
import ysaak.wallswitcher.ui.creator.WallpaperField;

import java.io.File;

/**
 * Created by ROTHDA on 30/05/2017.
 */
public class WallpaperPartAttribute {
    private final ScreenDto screen;
    private final File image;
    private final Color backgroundColor;

    public WallpaperPartAttribute(ScreenDto screen, WallpaperField field) {
        this.screen = screen;
        this.image = field.getSelectedFile();
        this.backgroundColor = field.getBackgroundColor();
    }

    public ScreenDto getScreen() {
        return screen;
    }

    public File getImage() {
        return image;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }
}
