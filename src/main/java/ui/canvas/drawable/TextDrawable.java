package ui.canvas.drawable;

import lombok.Data;

import java.awt.*;
@Data
public class TextDrawable implements Drawable {

    private String text;
    private double x, y, width, height;
    private int fontSize;
    private String fontFamily = "SansSerif";
    private String fontColor = "#000000";

    public TextDrawable(String text, double x, double y, int fontSize) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.fontSize = fontSize;

        updateSize();
    }

    @Override
    public boolean hitTest(double px, double py) {

        double textTop = y;
        double textBottom = y + height;

        return px >= x && px <= x + width &&
                py >= textTop && py <= textBottom;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.decode(fontColor));
        g.setFont(new Font(fontFamily, Font.PLAIN, fontSize));
        g.drawString(text, (int)x, (int)(y + fontSize));
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void updateSize() {
        Font font = new Font(fontFamily, Font.PLAIN, fontSize);
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);

        this.width = Math.max(10, fm.stringWidth(text));   // 避免 0 宽度
        this.height = Math.max(10, fm.getHeight());        // 避免 0 高度
    }

}
