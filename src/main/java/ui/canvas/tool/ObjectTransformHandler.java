package ui.canvas.tool;

import model.*;
import ui.canvas.drawable.*;

public class ObjectTransformHandler {

    /**
     * 移动对象（Rect / Circle / Image / Text）
     * 完全照搬 CanvasPanel.mouseDragged 的移动逻辑
     */
    public static void applyMove(PPTObject m, Drawable d, double dx, double dy) {

        // ==== 直线独立处理 ====
        if (m instanceof PPTLine && d instanceof LineDrawable) {
            PPTLine line = (PPTLine) m;

            line.setX(line.getX() + dx);
            line.setY(line.getY() + dy);
            line.setX2(line.getX2() + dx);
            line.setY2(line.getY2() + dy);

            LineDrawable ld = (LineDrawable) d;
            ld.setLine(line.getX(), line.getY(), line.getX2(), line.getY2());
            return;
        }

        // ==== 普通矩形类对象 ====
        m.setX(m.getX() + dx);
        m.setY(m.getY() + dy);
    }


    /**
     * 缩放对象（完全照搬 CanvasPanel.applyScale 的全部逻辑）
     */
    public static void applyScale(PPTObject m, int handle, double dx, double dy) {

        // ★★★ 直线专用缩放逻辑 ★★★
        if (m instanceof PPTLine) {
            PPTLine line = (PPTLine) m;

            switch (handle) {
                case 0: case 1: case 3: case 5:
                    line.setX(line.getX() + dx);
                    line.setY(line.getY() + dy);
                    break;

                case 2: case 4: case 6: case 7:
                    line.setX2(line.getX2() + dx);
                    line.setY2(line.getY2() + dy);
                    break;
            }
            return;
        }

        // ==== 矩形/圆/图片的缩放 ====
        double x = m.getX();
        double y = m.getY();
        double w = m.getWidth();
        double h = m.getHeight();

        switch (handle) {
            case 0: x += dx; y += dy; w -= dx; h -= dy; break;
            case 1: y += dy; h -= dy; break;
            case 2: y += dy; w += dx; h -= dy; break;
            case 3: x += dx; w -= dx; break;
            case 4: w += dx; break;
            case 5: x += dx; w -= dx; h += dy; break;
            case 6: h += dy; break;
            case 7: w += dx; h += dy; break;
        }

        if (w < 5) w = 5;
        if (h < 5) h = 5;

        m.setX(x);
        m.setY(y);
        m.setWidth(w);
        m.setHeight(h);
    }
}
