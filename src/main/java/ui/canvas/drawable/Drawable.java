package ui.canvas.drawable;

import java.awt.*;

/**
 * 所有可绘制对象必须实现这个接口
 * 统一绘图协议： draw(Graphics2D g)
 */
public interface Drawable {

    /**
     * 在画布上绘制对象
     */
    void draw(Graphics2D g);

    /**
     * 位置与大小，用于选中判定
     */
    double getX();
    double getY();
    double getWidth();
    double getHeight();

    /**
     * 点击检测
     */
    boolean hitTest(double px, double py);
}
