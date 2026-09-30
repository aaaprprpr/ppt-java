package ui.canvas;

import lombok.Data;
import lombok.EqualsAndHashCode;
import model.*;
import ui.AppFrame;
import ui.canvas.drawable.*;
import ui.canvas.tool.ToolCreationHandler;
import ui.canvas.tool.ObjectTransformHandler;
import ui.menu.ToolBarPanel;
import ui.slide.SlideListPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
@Data
@EqualsAndHashCode(callSuper = false)
public class CanvasPanel extends JPanel implements ToolBarPanel.ToolSelectListener {
    private Page currentPage;    // ★新增：当前页面


    public static final int PPT_WIDTH = 1280;
    public static final int PPT_HEIGHT = 720;
    // 是否处于“自动铺满窗口”模式
    private boolean autoFit = true;
    private Drawable selected = null;          // 当前选中对象
    private PPTObject selectedModel = null;    // 当前选中模型

    // 缩放/旋转控制点
    private enum DragMode { NONE, MOVE, SCALE }
    private DragMode dragMode = DragMode.NONE;

    private int activeHandle = -1; // 控制点编号（0-7 是缩放点，8 是旋转点）
    private double lastMouseX, lastMouseY;

    private double zoom = 1.0;
    private List<Drawable> drawables = new ArrayList<>();
    private TextDrawable editingDrawable = null;   // 正在编辑的文本
    private PPTText editingModel = null;           // 正在编辑的模型对象
    private JTextPane textEditor;
    private JScrollPane textScroll;
    private boolean isTextEditing = false;

    // 工具模式
    enum ToolMode {
        NONE,
        CREATE_RECT,
        CREATE_CIRCLE,
        CREATE_LINE,
        CREATE_TEXT,
        CREATE_IMAGE
    }

    enum CreationState {
        IDLE,
        WAITING_POINT1,
        WAITING_POINT2
    }
    // 预览相关变量
    private boolean isPreviewing = false;
    private double previewX1, previewY1;
    private double previewX2, previewY2;
    private ToolMode previewMode = ToolMode.NONE;

    private ToolMode toolMode = ToolMode.NONE;
    private CreationState creationState = CreationState.IDLE;

    // 第一点击点
    private double p1x, p1y;

    public CanvasPanel() {
        setBackground(Color.WHITE);

        /* ===================================================
         * 鼠标滚轮：缩放 / 滚动
         * =================================================== */
        addMouseWheelListener(e -> handleScrollZoom(e));


        /* ===================================================
         * 鼠标按下（包括：双击文本进入编辑、绘图创建、选中对象等）
         * =================================================== */
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                // 双击文本 → 进入编辑模式
                if (e.getClickCount() == 2 && selected instanceof TextDrawable) {

                    editingDrawable = (TextDrawable) selected;
                    editingModel = (PPTText) selectedModel;
                    isTextEditing = true;

                    // 1. 设置文本内容到编辑器
                    textEditor.setText(editingModel.getText());
                    textEditor.setFont(new Font("SansSerif", Font.PLAIN, editingModel.getFontSize()));

                    Point p = new Point((int)(editingDrawable.getX() * zoom), (int)(editingDrawable.getY() * zoom));

                    SwingUtilities.convertPointToScreen(p, CanvasPanel.this);
                    SwingUtilities.convertPointFromScreen(p, AppFrame.getInstance().getLayeredPaneWrapper());
                    textScroll.setBounds(p.x, p.y, 300, 150);
                    textScroll.setVisible(true);
                    textEditor.requestFocus();
                    return;
                }
                handleMousePressed(e);
            }
        });


        /* ===================================================
         * 鼠标拖动（移动、缩放）
         * =================================================== */
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selected == null) return;

                double cx = toCanvasX(e.getX());
                double cy = toCanvasY(e.getY());

                double dx = cx - lastMouseX;
                double dy = cy - lastMouseY;

                // 移动
                if (dragMode == DragMode.MOVE) {
                    ObjectTransformHandler.applyMove(selectedModel, selected, dx, dy);
                    DrawableModelMapper.sync(selected, selectedModel);
                }

                // 缩放
                if (dragMode == DragMode.SCALE) {
                    ObjectTransformHandler.applyScale(selectedModel, activeHandle, dx, dy);
                    DrawableModelMapper.sync(selected, selectedModel);
                }

                lastMouseX = cx;
                lastMouseY = cy;
                repaint();

                // 缩略图刷新
                if (currentPage != null) {
                    int idx = currentPage.getId();
                    SlideListPanel list = AppFrame.getInstance().getSlideListPanel();
                    list.refreshThumbnail(idx);
                }
            }
        });


        /* ===================================================
         * 鼠标移动（更新光标形状、预览线框）
         * =================================================== */
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }
        });


        /* ===================================================
         * 组件大小变化（自动适应窗口）
         * =================================================== */
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (autoFit) {
                    SwingUtilities.invokeLater(() -> autoFitToWindow());
                }
                if (!autoFit && isCanvasExactlyFit()) {
                    autoFit = true;
                }
            }
        });


        /* ===================================================
         * 键盘事件（文本编辑）
         * =================================================== */
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // ================= 删除键 =================
                if (!isTextEditing && e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelectedObject();
                }
            }
        });

        /* ===================================================
         * 初始化缩放与初始布局
         * =================================================== */
        updatePreferredSize();
        SwingUtilities.invokeLater(() -> autoFitToWindow());
// 文本编辑器初始化
        textEditor = new JTextPane();
        textEditor.setFont(new Font("SansSerif", Font.PLAIN, 20));
        textEditor.setOpaque(true);
        textEditor.setBorder(null);

        textScroll = new JScrollPane(textEditor);
        textScroll.setVisible(false);

        setLayout(null);
        AppFrame.getInstance().getLayeredPaneWrapper().add(textScroll, JLayeredPane.PALETTE_LAYER);

        textEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finishTextEdit();
            }
        });
    }
    private void finishTextEdit() {
        if (!isTextEditing) return;

        //  更新文本
        editingModel.setText(textEditor.getText());
        editingDrawable.setText(editingModel.getText());
        editingDrawable.setFontFamily(editingModel.getFontFamily());
        editingDrawable.setFontColor(editingModel.getFontColor());

        // 更新可视对象尺寸
        editingDrawable.updateSize();

        // 关闭编辑器
        textScroll.setVisible(false);
        isTextEditing = false;

        repaint();

        // 刷新缩略图
        if (currentPage != null) {
            int idx = currentPage.getId();
            AppFrame.getInstance().getSlideListPanel().refreshThumbnail(idx);
        }
        AppFrame.getInstance().getPropertyPanel().showProperties(editingModel);
    }


    @Override
    public void onToolSelected(String tool) {
        if (tool.equals("delete")) {
            deleteSelectedObject();               // 执行删除
            return;                               // 不进入工具模式
        }

        // 切换工具 = 取消当前选中
        clearSelection();

        switch (tool) {
            case "rect":   toolMode = ToolMode.CREATE_RECT; break;
            case "circle": toolMode = ToolMode.CREATE_CIRCLE; break;
            case "line":   toolMode = ToolMode.CREATE_LINE; break;
            case "text":   toolMode = ToolMode.CREATE_TEXT; break;
            case "image":  toolMode = ToolMode.CREATE_IMAGE; break;
            default:       toolMode = ToolMode.NONE;
        }
        /* 设置鼠标十字光标（图片除外） */
        if (toolMode == ToolMode.CREATE_RECT || toolMode == ToolMode.CREATE_CIRCLE || toolMode == ToolMode.CREATE_LINE || toolMode == ToolMode.CREATE_TEXT) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
        creationState = CreationState.IDLE;
    }

    private void handleMousePressed(MouseEvent e) {

        Point p = SwingUtilities.convertPoint(CanvasPanel.this, e.getPoint(), AppFrame.getInstance().getLayeredPaneWrapper());
        if (isTextEditing && !textScroll.getBounds().contains(p)) {
            finishTextEdit();
        }

        // ======== 创建模式：禁止选中已有对象 ========
        if (toolMode != ToolMode.NONE) {

            // 文本模式（一次点击立即创建）
            if (toolMode == ToolMode.CREATE_TEXT) {
                createTextInputAt(e.getX(), e.getY());
                toolMode = ToolMode.NONE;
                return;
            }

            // 二点绘制模式（rect / circle / line）
            if (creationState == CreationState.IDLE) {
                // 第一个点
                p1x = toCanvasX(e.getX());
                p1y = toCanvasY(e.getY());
                previewX1 = p1x;
                previewY1 = p1y;
                isPreviewing = true;
                previewMode = toolMode;
                creationState = CreationState.WAITING_POINT2;
                return;
            }

            if (creationState == CreationState.WAITING_POINT2) {
                double p2x = toCanvasX(e.getX());
                double p2y = toCanvasY(e.getY());

                // 这里复制你下面已有的创建代码（矩形/圆/线）
                if (toolMode == ToolMode.CREATE_RECT) {
                    ToolCreationHandler.createRect(p1x, p1y, p2x, p2y, drawables, currentPage);
                }

                if (toolMode == ToolMode.CREATE_CIRCLE) {
                    ToolCreationHandler.createCircle(p1x, p1y, p2x, p2y, drawables, currentPage);
                }

                if (toolMode == ToolMode.CREATE_LINE) {
                    ToolCreationHandler.createLine(p1x, p1y, p2x, p2y, drawables, currentPage);
                }

                creationState = CreationState.IDLE;
                toolMode = ToolMode.NONE;
                isPreviewing = false;

                repaint();
                return;
            }
        }
        // ========点击选中已有对象 ======== //
        double cx = toCanvasX(e.getX());
        double cy = toCanvasY(e.getY());
        // 控制点区域保护：只要点到控制点，就绝对不能切换选中对象
        if (selected != null) {
            int h = hitTestHandle(cx, cy, selected);
            if (h >= 0) {
                // 命中控制点 → 直接进入缩放/旋转，不允许其它对象抢占
                activeHandle = h;
                dragMode =  DragMode.SCALE;

                lastMouseX = cx;
                lastMouseY = cy;
                return;
            }
        }
        // 先从上往下找，ZIndex 或列表顺序决定前后
        for (int i = drawables.size()-1; i >= 0; i--) {
            Drawable d = drawables.get(i);
            if (d.hitTest(cx, cy)) {

                selected = d;
                selectedModel = currentPage.getObjects().get(i);
                AppFrame.getInstance().getToolBarPanel().setDeleteEnabled(true);
                // 右侧属性面板更新
                AppFrame.getInstance().getPropertyPanel().showProperties(selectedModel);

                // 判断是否命中控制点
                activeHandle = hitTestHandle(cx, cy, d);
                if (activeHandle >= 0) {
                    dragMode = DragMode.SCALE;
                } else {
                    dragMode = DragMode.MOVE;
                }

                lastMouseX = cx;
                lastMouseY = cy;

                repaint();
                return;
            }
        }
        // 如果命中了控制点，绝不退出选中
        if (selected != null) {
            int h = hitTestHandle(cx, cy, selected);
            if (h >= 0) {
                activeHandle = h;
                dragMode = DragMode.SCALE;
                lastMouseX = cx;
                lastMouseY = cy;
                return;
            }
        }
        // 点击空白
        clearSelection();

        // 文本模式：点击一次弹出输入框
        if (toolMode == ToolMode.CREATE_TEXT) {
            createTextInputAt(e.getX(), e.getY());
            toolMode = ToolMode.NONE;
        }
    }

    private void createTextInputAt(int x, int y) {
        double cx = toCanvasX(x);
        double cy = toCanvasY(y);

        TextDrawable td = ToolCreationHandler.createText(cx, cy, drawables, currentPage);
        editingDrawable = td;
        editingModel = (PPTText) currentPage.getObjects().get(currentPage.getObjects().size()-1);

        // 自动进入编辑模式
        isTextEditing = true;

        // 设置编辑器内容
        textEditor.setText(editingModel.getText());
        textEditor.setFont(new Font("SansSerif", Font.PLAIN, editingModel.getFontSize()));

        Point p = new Point((int)(editingDrawable.getX() * zoom), (int)(editingDrawable.getY() * zoom));

        SwingUtilities.convertPointToScreen(p, CanvasPanel.this);
        SwingUtilities.convertPointFromScreen(p, AppFrame.getInstance().getLayeredPaneWrapper());

        textScroll.setBounds(p.x, p.y, 300, 150);
        textScroll.setVisible(true);
        repaint();
    }

    // 滚轮缩放与滚动
    private void handleScrollZoom(MouseWheelEvent e) {
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, CanvasPanel.this);

        if (scrollPane == null) return;

        if (e.isControlDown()) {
            autoFit = false;

            if (e.getWheelRotation() < 0)
                setZoom(zoom * 1.1);
            else
                setZoom(zoom / 1.1);
            return;
        }

        if (e.isAltDown()) {
            JScrollBar hBar = scrollPane.getHorizontalScrollBar();
            int move = e.getWheelRotation() * hBar.getUnitIncrement()*20;
            hBar.setValue(hBar.getValue() + move);
            return;
        }

        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        int move = e.getWheelRotation() * vBar.getUnitIncrement()*20;
        vBar.setValue(vBar.getValue() + move);
    }

    public void setZoom(double z) {
        zoom = Math.max(0.1, Math.min(z, 10.0));
        updatePreferredSize();
        repaint();
    }

    private void updatePreferredSize() {
        int w = (int) (PPT_WIDTH * zoom);
        int h = (int) (PPT_HEIGHT * zoom);
        setPreferredSize(new Dimension(w, h));
        revalidate();
    }

    public void addDrawable(Drawable d) {
        drawables.add(d);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        int canvasX = (int)((getWidth()  - PPT_WIDTH  * zoom) / 2);
        int canvasY = (int)((getHeight() - PPT_HEIGHT * zoom) / 2);

        g2.translate(canvasX, canvasY);
        g2.scale(zoom, zoom);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, PPT_WIDTH, PPT_HEIGHT);

        g2.setColor(Color.GRAY);
        g2.drawRect(0, 0, PPT_WIDTH, PPT_HEIGHT);

        // 现在 Drawable 的 draw() 坐标也都是画布内部坐标
        for (Drawable d : drawables) {
            if (isTextEditing && d == editingDrawable)
                continue;  // 编辑状态隐藏 TextDrawable
            d.draw(g2);
        }

        /* ======= 绘制预览 ======= */
        if (isPreviewing) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1.5f));

            double x1 = previewX1;
            double y1 = previewY1;
            double x2 = previewX2;
            double y2 = previewY2;

            switch (previewMode) {
                case CREATE_RECT:
                    g2.drawRect((int)Math.min(x1, x2), (int)Math.min(y1, y2), (int)Math.abs(x2 - x1), (int)Math.abs(y2 - y1));
                    break;
                case CREATE_CIRCLE:
                    g2.drawOval((int)Math.min(x1, x2), (int)Math.min(y1, y2), (int)Math.abs(x2 - x1), (int)Math.abs(y2 - y1));
                    break;
                case CREATE_LINE:
                    g2.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
                    break;
            }
        }
        if (selected != null) {
            Drawable d = selected;

            double x = d.getX(), y = d.getY();
            double w = d.getWidth(), h = d.getHeight();

            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(1));
            g2.drawRect((int)x, (int)y, (int)w, (int)h);

            // 8 个控制点
            double[][] handles = {
                    {x, y}, {x+w/2, y}, {x+w, y},
                    {x, y+h/2}, {x+w, y+h/2},
                    {x, y+h}, {x+w/2, y+h}, {x+w, y+h}
            };

            for (double[] p : handles) {
                g2.fillRect((int)p[0]-4, (int)p[1]-4, 8, 8);
            }
        }
        g2.translate(-canvasX, -canvasY);
    }

    private void handleMouseMove(MouseEvent e) {
        if (isPreviewing && creationState == CreationState.WAITING_POINT2) {
            previewX2 = toCanvasX(e.getX());
            previewY2 = toCanvasY(e.getY());
            repaint();
        }
        double cx = toCanvasX(e.getX());
        double cy = toCanvasY(e.getY());

        if (selected != null) {
            int h = hitTestHandle(cx, cy, selected);
            switch (h) {
                case 0: case 7:
                    setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                    return;
                case 1: case 6:
                    setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                    return;
                case 2: case 5:
                    setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    return;
                case 3: case 4:
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    return;
            }
            // 如果没碰到控制点，但选中对象 → 普通移动光标
            if (selected.hitTest(cx, cy)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                return;
            }
        }
        setCursor(Cursor.getDefaultCursor());// 默认光标
    }

    public void setPage(Page page) {
        // === 页面切换时清除选中状态 ===
        clearSelection();

        this.currentPage = page;          // ★新增：记住当前页
        drawables.clear();                // 清空画布对象

        if (page != null && page.getObjects() != null) {
            for (var obj : page.getObjects()) {
                Drawable d = DrawableFactory.createDrawable(obj);   // ★新增：模型转可绘制对象
                if (d != null) {
                    drawables.add(d);
                    DrawableModelMapper.sync(d, obj);
                }
            }
        }
        repaint();
    }

    @Override
    public void addImage(String path) {
        setCursor(Cursor.getDefaultCursor());
        ToolCreationHandler.createImage(path, drawables, currentPage);
        repaint();
    }

    // 屏幕坐标 → PPT 内部坐标（考虑居中 + 缩放）
    private double toCanvasX(int screenX) {
        int canvasX = (int)((getWidth() - PPT_WIDTH * zoom) / 2);
        return (screenX - canvasX) / zoom;
    }

    private double toCanvasY(int screenY) {
        int canvasY = (int)((getHeight() - PPT_HEIGHT * zoom) / 2);
        return (screenY - canvasY) / zoom;
    }

    private void autoFitToWindow() {
        // 可视区域大小（在 ScrollPane 内容下计算）
        int viewW = getParent().getWidth();
        int viewH = getParent().getHeight();

        if (viewW <= 0 || viewH <= 0) return;

        // PPT 内部逻辑大小
        double scaleW = viewW * 1.0 / PPT_WIDTH;
        double scaleH = viewH * 1.0 / PPT_HEIGHT;

        // 最终缩放比例取最小值（保证能完全显示）
        double newZoom = Math.min(scaleW, scaleH);

        // 设置 zoom（但不触发退出 autoFit）
        this.zoom = newZoom;
        updatePreferredSize();
        repaint();
    }
    private boolean isCanvasExactlyFit() {
        int viewW = getParent().getWidth();
        int viewH = getParent().getHeight();
        double scaleW = viewW / (double)PPT_WIDTH;
        double scaleH = viewH / (double)PPT_HEIGHT;
        double target = Math.min(scaleW, scaleH);

        return Math.abs(target - zoom) < 0.7;
    }
    private int hitTestHandle(double px, double py, Drawable d) {
        double x = d.getX(), y = d.getY();
        double w = d.getWidth(), h = d.getHeight();

        double[][] handles = {
                {x, y}, {x+w/2, y}, {x+w, y},
                {x, y+h/2},           {x+w, y+h/2},
                {x, y+h}, {x+w/2, y+h}, {x+w, y+h}
        };

        for (int i = 0; i < handles.length; i++) {
            double hx = handles[i][0];
            double hy = handles[i][1];
            if (Math.abs(px - hx) <= 8 && Math.abs(py - hy) <= 8)
                return i;
        }

        return -1;
    }

    private void clearSelection() {
        selected = null;
        selectedModel = null;
        activeHandle = -1;
        dragMode = DragMode.NONE;
        AppFrame.getInstance().getToolBarPanel().setDeleteEnabled(false);
        AppFrame.getInstance().getPropertyPanel().showEmpty();

        repaint();
    }
    private void deleteSelectedObject() {
        if (selected == null || selectedModel == null || currentPage == null) return;

        // 找到 Drawable 在 drawables 中的下标（这是最准确的）
        int index = drawables.indexOf(selected);
        if (index >= 0) {
            drawables.remove(index);
            currentPage.getObjects().remove(index);
        }

        clearSelection();
        repaint();

        // 通知工具栏：没有选中了 → 删除按钮禁用
        AppFrame.getInstance().getToolBarPanel().setDeleteEnabled(false);

        // 刷新缩略图
        int pid = currentPage.getId();
        AppFrame.getInstance().getSlideListPanel().refreshThumbnail(pid);
    }
    public void onPropertyChanged(PPTObject obj) {
        if (selectedModel != obj) return;
        if (selected == null) return;

        // 模型 → Drawable 同步
        DrawableModelMapper.sync(selected, obj);

        // 特例：文字需要额外更新尺寸
        if (selected instanceof TextDrawable td) {
            td.updateSize();
        }

        repaint();

        // 缩略图刷新
        if (currentPage != null) {
            int idx = currentPage.getId();
            AppFrame.getInstance().getSlideListPanel().refreshThumbnail(idx);
        }
    }
}
