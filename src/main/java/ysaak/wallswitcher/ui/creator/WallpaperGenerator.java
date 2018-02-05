package ysaak.wallswitcher.ui.creator;

import ysaak.wallswitcher.data.WallpaperPartAttribute;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * Wallpaper generator class
 */
public final class WallpaperGenerator {

    public static BufferedImage generate(List<WallpaperPartAttribute> attributeList) throws IOException {
        // compute final image size
        int minX = 0;
        int minY = 0;
        int maxWidth = -1;
        int maxHeight = -1;

        for (WallpaperPartAttribute attribute : attributeList) {
            // Compute maximum width/height
            maxWidth = Math.max(maxWidth, ((int) (attribute.getScreen().getX() + attribute.getScreen().getWidth())));
            maxHeight = Math.max(maxHeight, ((int) (attribute.getScreen().getY() + attribute.getScreen().getHeight())));

            // Compute minimum X / Y position
            minX = Math.min(minX, (int) (attribute.getScreen().getX()));
            minY = Math.min(minY, (int) (attribute.getScreen().getY()));
        }

        if (minX < 0) {
            // Screen placed in negative X position
            maxWidth += -1 * minX;
        }

        if (minY < 0) {
            // Screen placed in negative Y position
            maxHeight += -1 * minY;
        }

        final BufferedImage image = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = image.createGraphics();
        // Set default background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, maxWidth, maxHeight);

        for (WallpaperPartAttribute attribute : attributeList) {
            writeImagePart(g2, attribute, -1 * minX, -1 * minY);
        }

        return image;
    }

    private static void writeImagePart(Graphics2D g2, WallpaperPartAttribute attribute, int xOffset, int yOffset) throws IOException {
        // Draw background
        Color color = new Color(
                (float) attribute.getBackgroundColor().getRed(),
                (float) attribute.getBackgroundColor().getGreen(),
                (float) attribute.getBackgroundColor().getBlue(),
                (float) attribute.getBackgroundColor().getOpacity()
        );
        g2.setColor(color);

        int partX = ((int) attribute.getScreen().getX()) + xOffset;
        int partY = ((int) attribute.getScreen().getY()) + yOffset;
        int partWidth = (int) attribute.getScreen().getWidth();
        int partHeight = (int) attribute.getScreen().getHeight();

        g2.fillRect(partX, partY, partWidth, partHeight);

        if (attribute.getImage() != null) {
            BufferedImage image = ImageIO.read(attribute.getImage());
            BufferedImage resizeImage = Scalr.resize(image, partWidth, partHeight);
            image.flush();


            // Center image on part space
            int x = partX;
            int y = partY;

            if (resizeImage.getWidth() < partWidth) {
                x += (partWidth - resizeImage.getWidth()) / 2;
            }
            if (resizeImage.getHeight() < partHeight) {
                y += (partHeight - resizeImage.getHeight()) / 2;
            }


            g2.drawImage(resizeImage, x, y, resizeImage.getWidth(), resizeImage.getHeight(), null);
        }
    }
}
