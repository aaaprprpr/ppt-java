package ui.canvas.drawable;

import lombok.Data;

import java.awt.*;
@Data
public class RectDrawable implements Drawable {

    private double x, y, width, height;
    private Color fillColor = new Color(220, 220, 220);
    private Color strokeColor = Color.BLACK;

    private boolean fillEnabled = false;
    public RectDrawable(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setStroke(new BasicStroke(2.0f));

        if (fillEnabled && fillColor != null) {
            g.setColor(fillColor);
            g.fillRect((int)x, (int)y, (int)width, (int)height);
        }
        g.setColor(strokeColor);
        g.drawRect((int)x, (int)y, (int)width, (int)height);
    }
    @Override
    public boolean hitTest(double px, double py) {
        return px >= x && px <= x + width &&
                py >= y && py <= y + height;
    }


    public double getX(){ return x; }
    public double getY(){ return y; }
    public double getWidth(){ return width; }
    public double getHeight(){ return height; }
}
