package ui.canvas.drawable;

import lombok.Data;

import java.awt.*;
@Data
public class CircleDrawable implements Drawable {

    private double x, y, width, height;// 圆心坐标, 宽高
    private Color fillColor = new Color(220, 220, 220);// 填充色
    private Color strokeColor = Color.BLACK;// 描边色

    private boolean fillEnabled = false;// 是否填充
    public CircleDrawable(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setStroke(new BasicStroke(2.0f));

        // === 填充 ===
        if (fillEnabled && fillColor != null) {
            g.setColor(fillColor);
            g.fillOval((int)x, (int)y, (int)width, (int)height);
        }

        g.setColor(strokeColor);
        g.drawOval((int)x, (int)y, (int)width, (int)height);
    }

    @Override
    public boolean hitTest(double px, double py) {
        double cx = x + width / 2;
        double cy = y + height / 2;
        double rx = width / 2;
        double ry = height / 2;

        double dx = px - cx;
        double dy = py - cy;

        double v = (dx*dx) / (rx*rx) + (dy*dy) / (ry*ry);
        return v <= 1.0;
    }

    public double getX(){ return x; }
    public double getY(){ return y; }
    public double getWidth(){ return width; }
    public double getHeight(){ return height; }
}
