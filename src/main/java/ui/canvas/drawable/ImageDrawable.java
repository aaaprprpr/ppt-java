package ui.canvas.drawable;

import lombok.Data;

import java.awt.*;
import javax.swing.*;
@Data
public class ImageDrawable implements Drawable {

    private Image image;// 图片
    private double x, y, width, height;// 位置和尺寸

    public ImageDrawable(String path, double x, double y, double w, double h) {
        this.image = new ImageIcon(path).getImage();
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    public ImageDrawable(String path, double x, double y) {
        this.image = new ImageIcon(path).getImage();

        int ow = image.getWidth(null);
        int oh = image.getHeight(null);

        this.x = x;
        this.y = y;

        //  默认缩放为原图的一半
        this.width = ow / 2.0;
        this.height = oh / 2.0;
    }
    public ImageDrawable(byte[] data, double x, double y, double w, double h) {
        this.image = new ImageIcon(data).getImage(); // ★ 强制加载完成
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }


    @Override
    public void draw(Graphics2D g) {
        g.drawImage(image, (int)x, (int)y, (int)width, (int)height, null);
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
