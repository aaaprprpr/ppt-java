package myppt.editor;

import myppt.model.*;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;

public final class ObjectGeometry {
    private static final FontRenderContext FONT_CONTEXT = new FontRenderContext(null, true, true);

    private ObjectGeometry() {}

    public static Font font(PPTText text) {
        return new Font(text.getFontFamily(), Font.PLAIN, Math.max(1, text.getFontSize()));
    }

    public static String[] lines(PPTText text) {
        return (text.getText() == null ? "" : text.getText()).split("\\R", -1);
    }

    public static float lineHeight(PPTText text) {
        return font(text).getLineMetrics("Ag", FONT_CONTEXT).getHeight();
    }

    public static float ascent(PPTText text) {
        return font(text).getLineMetrics("Ag", FONT_CONTEXT).getAscent();
    }

    public static Rectangle2D bounds(PPTObject object) {
        if (object instanceof PPTLine line) {
            return new Rectangle2D.Double(Math.min(line.getX(), line.getX2()), Math.min(line.getY(), line.getY2()),
                    Math.abs(line.getX2() - line.getX()), Math.abs(line.getY2() - line.getY()));
        }
        if (object instanceof PPTText text) {
            double width = 10;
            Font font = font(text);
            String[] lines = lines(text);
            for (String line : lines) width = Math.max(width, font.getStringBounds(line, FONT_CONTEXT).getWidth());
            return new Rectangle2D.Double(text.getX(), text.getY(), width, lineHeight(text) * lines.length);
        }
        return new Rectangle2D.Double(object.getX(), object.getY(), object.getWidth(), object.getHeight());
    }

    public static boolean hitTest(PPTObject object, double x, double y, double tolerance) {
        if (object instanceof PPTLine line) {
            return Line2D.ptSegDist(line.getX(), line.getY(), line.getX2(), line.getY2(), x, y) <= tolerance;
        }
        Rectangle2D bounds = bounds(object);
        if (object instanceof PPTCircle) {
            return new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()).contains(x, y);
        }
        return bounds.contains(x, y);
    }

    public static Point2D[] handles(PPTObject object) {
        if (object instanceof PPTLine line) {
            return new Point2D[] {new Point2D.Double(line.getX(), line.getY()), new Point2D.Double(line.getX2(), line.getY2())};
        }
        Rectangle2D b = bounds(object);
        double x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();
        return new Point2D[] {
            new Point2D.Double(x, y), new Point2D.Double(x+w/2, y), new Point2D.Double(x+w, y),
            new Point2D.Double(x, y+h/2), new Point2D.Double(x+w, y+h/2),
            new Point2D.Double(x, y+h), new Point2D.Double(x+w/2, y+h), new Point2D.Double(x+w, y+h)
        };
    }

    public static void move(PPTObject object, double dx, double dy) {
        object.setX(object.getX() + dx);
        object.setY(object.getY() + dy);
        if (object instanceof PPTLine line) {
            line.setX2(line.getX2() + dx);
            line.setY2(line.getY2() + dy);
        }
    }

    public static void resize(PPTObject object, int handle, double dx, double dy) {
        if (object instanceof PPTLine line) {
            if (handle == 0) {
                line.setX(line.getX() + dx);
                line.setY(line.getY() + dy);
            } else if (handle == 1) {
                line.setX2(line.getX2() + dx);
                line.setY2(line.getY2() + dy);
            }
            return;
        }
        if (handle < 0 || handle > 7) return;
        Rectangle2D original = bounds(object);
        double left = original.getMinX(), top = original.getMinY();
        double right = original.getMaxX(), bottom = original.getMaxY();
        boolean movesLeft = handle == 0 || handle == 3 || handle == 5;
        boolean movesRight = handle == 2 || handle == 4 || handle == 7;
        boolean movesTop = handle <= 2;
        boolean movesBottom = handle >= 5;
        if (movesLeft) left = Math.min(left + dx, right - 5);
        if (movesRight) right = Math.max(right + dx, left + 5);
        if (movesTop) top = Math.min(top + dy, bottom - 5);
        if (movesBottom) bottom = Math.max(bottom + dy, top + 5);
        if (object instanceof PPTText text) {
            double scaleX = (right - left) / original.getWidth();
            double scaleY = (bottom - top) / original.getHeight();
            double scale = Math.abs(scaleX - 1) > Math.abs(scaleY - 1) ? scaleX : scaleY;
            text.setFontSize(Math.max(1, Math.min(512, (int) Math.round(text.getFontSize() * scale))));
            Rectangle2D resized = bounds(text);
            if (movesLeft) left = original.getMaxX() - resized.getWidth();
            if (movesTop) top = original.getMaxY() - resized.getHeight();
        } else {
            object.setWidth(right - left);
            object.setHeight(bottom - top);
        }
        object.setX(left);
        object.setY(top);
    }
}
