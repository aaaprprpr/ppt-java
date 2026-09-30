package myppt.render;

import myppt.editor.ObjectGeometry;
import myppt.model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public final class SlideRenderer {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private final Map<String, byte[]> resources;
    private final Map<String, Optional<BufferedImage>> images = new HashMap<>();

    public SlideRenderer(Map<String, byte[]> resources) {
        this.resources = resources;
    }

    public void clearCache() { images.clear(); }

    public void paintPage(Graphics2D graphics, Page page, PPTObject hidden) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.clip(new Rectangle(0, 0, WIDTH, HEIGHT));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setColor(Colors.parse(page.getBackgroundColor(), Color.WHITE));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            for (PPTObject object : page.getObjects()) {
                if (object != hidden) paintObject(g, object);
            }
        } finally {
            g.dispose();
        }
    }

    private void paintObject(Graphics2D g, PPTObject object) {
        g.setStroke(new BasicStroke(2));
        if (object instanceof PPTShape shape) {
            Rectangle2D bounds = ObjectGeometry.bounds(shape);
            Shape outline = shape instanceof PPTCircle
                    ? new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()) : bounds;
            if (shape.isFillEnabled()) {
                g.setColor(Colors.parse(shape.getFillColor(), Color.LIGHT_GRAY));
                g.fill(outline);
            }
            g.setColor(Colors.parse(shape.getStrokeColor(), Color.BLACK));
            g.draw(outline);
        } else if (object instanceof PPTLine line) {
            g.setColor(Colors.parse(line.getStrokeColor(), Color.BLACK));
            g.draw(new Line2D.Double(line.getX(), line.getY(), line.getX2(), line.getY2()));
        } else if (object instanceof PPTText text) {
            g.setColor(Colors.parse(text.getFontColor(), Color.BLACK));
            g.setFont(ObjectGeometry.font(text));
            float y = (float) text.getY() + ObjectGeometry.ascent(text);
            for (String line : ObjectGeometry.lines(text)) {
                g.drawString(line, (float) text.getX(), y);
                y += ObjectGeometry.lineHeight(text);
            }
        } else if (object instanceof PPTImage image) {
            BufferedImage bitmap = image(image);
            if (bitmap != null) {
                g.drawImage(bitmap, (int) image.getX(), (int) image.getY(),
                        (int) image.getWidth(), (int) image.getHeight(), null);
            } else {
                Rectangle2D bounds = ObjectGeometry.bounds(image);
                g.setColor(Color.LIGHT_GRAY);
                g.fill(bounds);
                g.setColor(Color.GRAY);
                g.draw(bounds);
                g.draw(new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
            }
        }
    }

    private BufferedImage image(PPTImage image) {
        String key = image.getSrc() != null ? image.getSrc() : image.getImagePath();
        if (key == null) return null;
        return images.computeIfAbsent(key, ignored -> {
            try {
                byte[] data = image.getSrc() == null ? null : resources.get(image.getSrc());
                if (data != null) return Optional.ofNullable(ImageIO.read(new ByteArrayInputStream(data)));
                if (image.getImagePath() != null) return Optional.ofNullable(ImageIO.read(new File(image.getImagePath())));
            } catch (IOException e) {
                // A missing legacy external image is rendered as a placeholder.
            }
            return Optional.empty();
        }).orElse(null);
    }

    public BufferedImage thumbnail(Page page, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Colors.parse(page.getBackgroundColor(), Color.WHITE));
            g.fillRect(0, 0, width, height);
            double scale = Math.min(width / (double) WIDTH, height / (double) HEIGHT);
            g.translate((width - WIDTH * scale) / 2, (height - HEIGHT * scale) / 2);
            g.scale(scale, scale);
            paintPage(g, page, null);
        } finally {
            g.dispose();
        }
        return image;
    }
}
