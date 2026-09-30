package ui.canvas.drawable;

import model.*;
import service.EditorService;

import java.util.Map;

public class DrawableFactory {

    /**
     * 根据 PPTObject 生成对应 Drawable
     */
    public static Drawable createDrawable(PPTObject obj) {
        if (obj == null) return null;

        switch (obj.getType()) {

            case "text": {
                PPTText t = (PPTText) obj;
                TextDrawable td = new TextDrawable(t.getText(), t.getX(), t.getY(), t.getFontSize());
                td.setFontFamily(t.getFontFamily());
                td.setFontColor(t.getFontColor());
                return td;
            }

            case "rect": {
                PPTRect r = (PPTRect) obj;
                RectDrawable d = new RectDrawable(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                return d;
            }

            case "circle": {
                PPTCircle c = (PPTCircle) obj;
                CircleDrawable d = new CircleDrawable(c.getX(), c.getY(), c.getWidth(), c.getHeight());
                return d;
            }

            case "line": {
                PPTLine l = (PPTLine) obj;
                LineDrawable d = new LineDrawable(l.getX(),  l.getY(), l.getX2(), l.getY2());
                return d;
            }

            case "image": {
                PPTImage img = (PPTImage) obj;
                // resourcesMap 来自 EditorService
                Map<String, byte[]> res = EditorService.getInstance().getResourceMap();

                String src = img.getSrc();
                if (src != null) {
                    byte[] data = res.get(src);
                    if (data != null)
                        return new ImageDrawable(data, img.getX(), img.getY(), img.getWidth(), img.getHeight());
                }


// fallback：如果没有资源字节，就按原路径加载
                return new ImageDrawable(img.getImagePath(), img.getX(), img.getY(), img.getWidth(), img.getHeight());

            }
        }
        return null;
    }
}
