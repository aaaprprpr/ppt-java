package ui.canvas.drawable;

import lombok.Data;

import java.awt.*;
@Data
public class LineDrawable implements Drawable {

    private double x1, y1, x2, y2;
    private Color strokeColor = Color.BLACK;

    public LineDrawable(double x1, double y1, double x2, double y2) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setStroke(new BasicStroke(2.0f));

        g.setColor(strokeColor);
        g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
    }
    @Override
    public boolean hitTest(double px, double py) {

        double dx = x2 - x1;
        double dy = y2 - y1;

        if (dx == 0 && dy == 0)
            return false;

        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));

        double projX = x1 + t * dx;
        double projY = y1 + t * dy;

        double dist2 = (projX - px) * (projX - px) + (projY - py) * (projY - py);
        return dist2 <= 81;   // 5px 容忍
    }
    public void setLine(double x1, double y1, double x2, double y2){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public double getX() { return Math.min(x1, x2); }
    @Override
    public double getY() { return Math.min(y1, y2); }
    @Override
    public double getWidth() { return Math.abs(x2 - x1); }
    @Override
    public double getHeight() { return Math.abs(y2 - y1); }
}
