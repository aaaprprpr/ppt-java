package myppt.ui.canvas;

import myppt.editor.ObjectGeometry;
import myppt.model.*;
import myppt.render.SlideRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.function.Consumer;

public final class CanvasPanel extends JPanel implements Scrollable {
    public enum Tool { SELECT, TEXT, RECT, CIRCLE, LINE }

    private final SlideRenderer renderer;
    private final Consumer<PPTObject> onSelection;
    private final Runnable onChange;
    private final TextEditor textEditor;
    private Page page;
    private PPTObject selected;
    private Tool tool = Tool.SELECT;
    private Point2D firstPoint;
    private Point2D previewPoint;
    private Point2D lastPoint;
    private Point2D dragOrigin;
    private TextDrag textDrag;
    private int activeHandle = -1;
    private boolean dragging;
    private double zoom = 1;
    private boolean autoFit = true;

    private record TextDrag(double x, double y, int fontSize) {}

    public CanvasPanel(SlideRenderer renderer, Consumer<PPTObject> onSelection, Runnable onChange) {
        this.renderer = renderer;
        this.onSelection = onSelection;
        this.onChange = onChange;
        setBackground(new Color(225, 225, 225));
        setLayout(null);
        setFocusable(true);
        textEditor = new TextEditor(edited -> {
            changed();
            if (selected == edited) onSelection.accept(edited);
        });
        add(textEditor);
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { press(e); }
            @Override public void mouseDragged(MouseEvent e) { drag(e); }
            @Override public void mouseMoved(MouseEvent e) { hover(e); }
            @Override public void mouseReleased(MouseEvent e) {
                if (dragging && selected != null) onSelection.accept(selected);
                dragging = false;
                lastPoint = null;
                activeHandle = -1;
            }
            @Override public void mouseWheelMoved(MouseWheelEvent e) { scroll(e); }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (autoFit) fitToWindow();
                positionEditor();
            }
        });
        bind("DELETE", this::deleteSelected);
        bind("ESCAPE", () -> selectTool(Tool.SELECT));
        bind("control 0", () -> { autoFit = true; fitToWindow(); });
        updatePreferredSize();
    }

    private void bind(String key, Runnable action) {
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    public Page getPage() { return page; }
    public PPTObject getSelectedObject() { return selected; }

    public void setPage(Page next) {
        finishTextEdit();
        page = next;
        selectTool(Tool.SELECT);
    }

    public void finishTextEdit() { textEditor.finish(); }

    public void selectTool(Tool next) {
        finishTextEdit();
        tool = page == null ? Tool.SELECT : next;
        firstPoint = previewPoint = lastPoint = null;
        dragging = false;
        activeHandle = -1;
        select(null);
        setCursor(Cursor.getPredefinedCursor(tool == Tool.SELECT ? Cursor.DEFAULT_CURSOR : Cursor.CROSSHAIR_CURSOR));
        requestFocusInWindow();
    }

    private void select(PPTObject object) {
        selected = object;
        onSelection.accept(object);
        repaint();
    }

    public void addObject(PPTObject object) {
        if (page == null) return;
        selectTool(Tool.SELECT);
        page.getObjects().add(object);
        select(object);
        changed();
    }

    public void deleteSelected() {
        finishTextEdit();
        if (page == null || selected == null) return;
        page.getObjects().removeIf(object -> object == selected);
        select(null);
        changed();
    }

    public void propertyChanged(PPTObject object) {
        finishTextEdit();
        if (page != null && page.getObjects().contains(object)) changed();
    }

    private void changed() {
        repaint();
        onChange.run();
    }

    private void press(MouseEvent e) {
        if (page == null || !SwingUtilities.isLeftMouseButton(e)) return;
        finishTextEdit();
        requestFocusInWindow();
        Point2D point = toSlide(e.getPoint());
        if (tool != Tool.SELECT) {
            if (tool == Tool.TEXT) {
                PPTText text = new PPTText();
                text.setX(point.getX());
                text.setY(point.getY());
                addObject(text);
                edit(text);
            } else if (firstPoint == null) {
                firstPoint = previewPoint = point;
                repaint();
            } else {
                addObject(createShape(firstPoint, point));
            }
            return;
        }
        activeHandle = handleAt(point);
        if (activeHandle < 0) {
            PPTObject hit = null;
            for (int i = page.getObjects().size() - 1; i >= 0; i--) {
                PPTObject candidate = page.getObjects().get(i);
                if (ObjectGeometry.hitTest(candidate, point.getX(), point.getY(), 6 / zoom)) {
                    hit = candidate;
                    break;
                }
            }
            select(hit);
        }
        if (e.getClickCount() == 2 && selected instanceof PPTText text) {
            edit(text);
            return;
        }
        lastPoint = point;
        dragOrigin = point;
        textDrag = selected instanceof PPTText text
                ? new TextDrag(text.getX(), text.getY(), text.getFontSize()) : null;
        dragging = selected != null;
    }

    private void drag(MouseEvent e) {
        if (!dragging || selected == null || lastPoint == null) return;
        Point2D point = toSlide(e.getPoint());
        double dx = point.getX() - lastPoint.getX(), dy = point.getY() - lastPoint.getY();
        if (activeHandle >= 0) {
            if (selected instanceof PPTText text && textDrag != null) {
                // Use the whole gesture so small movements are not lost to integer font sizes.
                text.setX(textDrag.x());
                text.setY(textDrag.y());
                text.setFontSize(textDrag.fontSize());
                ObjectGeometry.resize(text, activeHandle,
                        point.getX() - dragOrigin.getX(), point.getY() - dragOrigin.getY());
            } else ObjectGeometry.resize(selected, activeHandle, dx, dy);
        } else ObjectGeometry.move(selected, dx, dy);
        lastPoint = point;
        if (dx != 0 || dy != 0) changed();
    }

    private void hover(MouseEvent e) {
        Point2D point = toSlide(e.getPoint());
        if (tool != Tool.SELECT) {
            previewPoint = point;
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            repaint();
            return;
        }
        int handle = handleAt(point);
        int cursor = Cursor.DEFAULT_CURSOR;
        if (handle >= 0) {
            int[] cursors = {Cursor.NW_RESIZE_CURSOR, Cursor.N_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR,
                    Cursor.W_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR,
                    Cursor.S_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR};
            cursor = selected instanceof PPTLine ? Cursor.CROSSHAIR_CURSOR : cursors[handle];
        } else if (selected != null && ObjectGeometry.hitTest(selected, point.getX(), point.getY(), 6 / zoom)) {
            cursor = Cursor.MOVE_CURSOR;
        }
        setCursor(Cursor.getPredefinedCursor(cursor));
    }

    private int handleAt(Point2D point) {
        if (selected == null) return -1;
        Point2D[] handles = ObjectGeometry.handles(selected);
        for (int i = 0; i < handles.length; i++) {
            if (Math.abs(handles[i].getX() - point.getX()) <= 6 / zoom
                    && Math.abs(handles[i].getY() - point.getY()) <= 6 / zoom) return i;
        }
        return -1;
    }

    private PPTObject createShape(Point2D start, Point2D end) {
        PPTObject object = switch (tool) {
            case RECT -> new PPTRect();
            case CIRCLE -> new PPTCircle();
            case LINE -> new PPTLine();
            default -> throw new IllegalStateException("Not a shape tool: " + tool);
        };
        if (object instanceof PPTLine line) {
            line.setX(start.getX());
            line.setY(start.getY());
            line.setX2(end.getX());
            line.setY2(end.getY());
        } else {
            object.setX(Math.min(start.getX(), end.getX()));
            object.setY(Math.min(start.getY(), end.getY()));
            object.setWidth(Math.max(5, Math.abs(end.getX() - start.getX())));
            object.setHeight(Math.max(5, Math.abs(end.getY() - start.getY())));
        }
        return object;
    }

    private void edit(PPTText text) {
        dragging = false;
        textEditor.begin(text);
        positionEditor();
        repaint();
    }

    private void positionEditor() {
        PPTText text = textEditor.getModel();
        if (text != null) textEditor.position((int) (offsetX() + text.getX() * zoom),
                (int) (offsetY() + text.getY() * zoom), zoom);
    }

    private double offsetX() { return (getWidth() - SlideRenderer.WIDTH * zoom) / 2; }
    private double offsetY() { return (getHeight() - SlideRenderer.HEIGHT * zoom) / 2; }

    public Point2D toSlide(Point point) {
        return new Point2D.Double((point.x - offsetX()) / zoom, (point.y - offsetY()) / zoom);
    }

    public void setZoom(double value) {
        autoFit = false;
        zoom = Math.max(0.1, Math.min(10, value));
        updatePreferredSize();
        positionEditor();
        repaint();
    }

    private void fitToWindow() {
        if (getParent() == null || getParent().getWidth() <= 0 || getParent().getHeight() <= 0) return;
        zoom = Math.max(0.01, Math.min((getParent().getWidth() - 20.0) / SlideRenderer.WIDTH,
                (getParent().getHeight() - 20.0) / SlideRenderer.HEIGHT));
        updatePreferredSize();
        repaint();
    }

    private void updatePreferredSize() {
        setPreferredSize(new Dimension((int) Math.ceil(SlideRenderer.WIDTH * zoom) + 20,
                (int) Math.ceil(SlideRenderer.HEIGHT * zoom) + 20));
        revalidate();
    }

    private void scroll(MouseWheelEvent e) {
        if (e.isControlDown()) {
            setZoom(zoom * Math.pow(1.1, -e.getWheelRotation()));
        } else {
            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (scroll == null) return;
            JScrollBar bar = e.isAltDown() ? scroll.getHorizontalScrollBar() : scroll.getVerticalScrollBar();
            bar.setValue(bar.getValue() + e.getWheelRotation() * 40);
        }
        e.consume();
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.translate(offsetX(), offsetY());
            g.scale(zoom, zoom);
            if (page != null) renderer.paintPage(g, page, textEditor.getModel());
            else {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, SlideRenderer.WIDTH, SlideRenderer.HEIGHT);
            }
            g.setColor(Color.GRAY);
            g.setStroke(new BasicStroke((float) (1 / zoom)));
            g.drawRect(0, 0, SlideRenderer.WIDTH, SlideRenderer.HEIGHT);
            if (firstPoint != null && previewPoint != null) {
                PPTObject preview = createShape(firstPoint, previewPoint);
                g.setColor(Color.RED);
                if (preview instanceof PPTLine line) {
                    g.draw(new Line2D.Double(line.getX(), line.getY(), line.getX2(), line.getY2()));
                } else if (preview instanceof PPTCircle) {
                    var b = ObjectGeometry.bounds(preview);
                    g.draw(new Ellipse2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight()));
                } else g.draw(ObjectGeometry.bounds(preview));
            }
            if (selected != null && textEditor.getModel() == null) {
                g.setColor(new Color(0, 120, 215));
                g.draw(ObjectGeometry.bounds(selected));
                for (Point2D point : ObjectGeometry.handles(selected)) {
                    g.fill(new Rectangle2D.Double(point.getX() - 4 / zoom, point.getY() - 4 / zoom, 8 / zoom, 8 / zoom));
                }
            }
        } finally {
            g.dispose();
        }
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int axis, int direction) { return 20; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int axis, int direction) {
        return axis == SwingConstants.HORIZONTAL ? r.width : r.height;
    }
    @Override public boolean getScrollableTracksViewportWidth() {
        return autoFit || getParent() != null && getPreferredSize().width < getParent().getWidth();
    }
    @Override public boolean getScrollableTracksViewportHeight() {
        return autoFit || getParent() != null && getPreferredSize().height < getParent().getHeight();
    }
}
