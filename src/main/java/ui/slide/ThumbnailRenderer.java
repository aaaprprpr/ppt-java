package ui.slide;

import model.*;
import ui.canvas.drawable.Drawable;
import ui.canvas.drawable.DrawableFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ThumbnailRenderer {

    public static BufferedImage render(Page page, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double scaleX = w / 1280.0;
        double scaleY = h / 720.0;
        double scale = Math.min(scaleX, scaleY);

        g.scale(scale, scale);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1280, 720);

        for (PPTObject obj : page.getObjects()) {
            Drawable d = DrawableFactory.createDrawable(obj);
            if (d != null) {
                ui.canvas.drawable.DrawableModelMapper.sync(d, obj);
                d.draw(g);
            }
        }

        g.dispose();
        return img;
    }
}
