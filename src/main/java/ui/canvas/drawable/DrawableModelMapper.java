package ui.canvas.drawable;

import model.*;

import java.awt.*;

public class DrawableModelMapper {

    /**
     * 模型 → 可绘制对象 的同步逻辑
     * （完全照搬 CanvasPanel.syncDrawableFromModel）
     */
    public static void sync(Drawable d, PPTObject m) {

        // ==== 位置和尺寸（所有类型共同部分）====
        if (d instanceof RectDrawable rd && m instanceof PPTRect) {
            rd.setX(m.getX());
            rd.setY(m.getY());
            rd.setWidth(m.getWidth());
            rd.setHeight(m.getHeight());
            PPTRect r = (PPTRect) m;
            rd.setFillEnabled(r.isFillEnabled());

            rd.setStrokeColor(Color.decode(((PPTRect)m).getStrokeColor()));
            rd.setFillColor(Color.decode(((PPTRect)m).getFillColor()));
            return;
        }

        if (d instanceof CircleDrawable cd && m instanceof PPTCircle) {
            cd.setX(m.getX());
            cd.setY(m.getY());
            cd.setWidth(m.getWidth());
            cd.setHeight(m.getHeight());
            PPTCircle c = (PPTCircle) m;
            cd.setFillEnabled(c.isFillEnabled());

            cd.setStrokeColor(Color.decode(((PPTCircle)m).getStrokeColor()));
            cd.setFillColor(Color.decode(((PPTCircle)m).getFillColor()));
            return;
        }

        if (d instanceof LineDrawable ld && m instanceof PPTLine l) {
            ld.setLine(l.getX(), l.getY(), l.getX2(), l.getY2());
            ld.setStrokeColor(Color.decode(l.getStrokeColor()));
            return;
        }

        if (d instanceof ImageDrawable id && m instanceof PPTImage) {
            id.setX(m.getX());
            id.setY(m.getY());
            id.setWidth(m.getWidth());
            id.setHeight(m.getHeight());
            return;
        }

        if (d instanceof TextDrawable td && m instanceof PPTText t) {
            td.setX(m.getX());
            td.setY(m.getY());

            td.setFontColor(t.getFontColor());
            td.setFontFamily(t.getFontFamily());
            td.setFontSize(t.getFontSize());
            td.updateSize();
        }
    }

}
