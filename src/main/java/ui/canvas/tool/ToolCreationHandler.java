package ui.canvas.tool;

import model.*;
import service.EditorService;
import ui.canvas.drawable.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ToolCreationHandler {

    // 创建矩形
    public static void createRect(double p1x, double p1y, double p2x, double p2y, java.util.List<Drawable> drawables, Page page) {
        double x = Math.min(p1x, p2x);
        double y = Math.min(p1y, p2y);
        double w = Math.abs(p2x - p1x);
        double h = Math.abs(p2y - p1y);
        RectDrawable d = new RectDrawable(x, y, w, h);
        drawables.add(d);
        PPTRect obj = new PPTRect();
        obj.setX(x);
        obj.setY(y);
        obj.setWidth(w);
        obj.setHeight(h);
        page.getObjects().add(obj);
    }

    // 创建圆形
    public static void createCircle(double p1x, double p1y, double p2x, double p2y, java.util.List<Drawable> drawables, Page page) {
        double x = Math.min(p1x, p2x);
        double y = Math.min(p1y, p2y);
        double w = Math.abs(p2x - p1x);
        double h = Math.abs(p2y - p1y);
        CircleDrawable d = new CircleDrawable(x, y, w, h);
        drawables.add(d);
        PPTCircle obj = new PPTCircle();
        obj.setX(x);
        obj.setY(y);
        obj.setWidth(w);
        obj.setHeight(h);
        page.getObjects().add(obj);
    }

    // 创建直线
    public static void createLine(double p1x, double p1y, double p2x, double p2y, java.util.List<Drawable> drawables, Page page) {
        LineDrawable d = new LineDrawable(p1x, p1y, p2x, p2y);
        drawables.add(d);
        PPTLine obj = new PPTLine(p1x, p1y, p2x, p2y);
        page.getObjects().add(obj);
    }

    // 创建文本
    public static TextDrawable createText(double x, double y, java.util.List<Drawable> drawables, Page page) {
        TextDrawable td = new TextDrawable("", x, y, 18);
        drawables.add(td);
        PPTText obj = new PPTText();
        obj.setX(x);
        obj.setY(y);
        obj.setFontSize(18);
        obj.setText("");
        page.getObjects().add(obj);
        return td;
    }

    // 创建图片
    public static void createImage(String path,
                                   List<Drawable> drawables, Page page) {

        // 1. 读取字节
        byte[] data;
        try {
            data = Files.readAllBytes(Paths.get(path));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 2. 自动生成唯一资源名（防重名覆盖）
        int index = EditorService.getInstance().getResourceMap().size();
        String ext = "";
        int i = path.lastIndexOf('.');
        if (i >= 0) ext = path.substring(i);

        String fname = String.format("img_%04d%s", index, ext);
        String resPath = "resources/" + fname;

        // 3. 存入资源 Map
        EditorService.getInstance().getResourceMap().put(resPath, data);

        // 4. 创建 Drawable（用字节）
        ImageDrawable d = new ImageDrawable(data, 10, 10, 200, 200);
        drawables.add(d);

        // 5. 写入 PPTImage 模型
        PPTImage obj = new PPTImage();
        obj.setSrc(resPath);     // 显示时使用的路径（始终指向 ZIP 内资源）
        obj.setImagePath(path);  // 用户原始路径（用于 fallback，不写入 ZIP）

        obj.setX(10);
        obj.setY(10);
        obj.setWidth(200);
        obj.setHeight(200);

        page.getObjects().add(obj);
    }


}
