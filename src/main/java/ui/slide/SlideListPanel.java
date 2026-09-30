package ui.slide;

import lombok.Data;
import lombok.EqualsAndHashCode;
import model.Document;
import model.Page;
import ui.AppFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
@Data
@EqualsAndHashCode(callSuper = false)
public class SlideListPanel extends JPanel {
    // === 拖动相关变量 ===
    private boolean dragging = false;
    private int dragIndex = -1;           // 原始位置
    private int hoverIndex = -1;          // 当前经过的位置
    private ThumbnailPanel dragPanel = null;
    private Point dragOffset = null;      // 鼠标相对面板左上角偏移
    private int pressIndex = -1;
    private Point pressPoint = null;
    private boolean draggingStarted = false;  // 解决：按下不等于开始拖动

    private Document document;
    private int selectedIndex = 0;   // 当前选中页

    private static final int THUMB_W = 120;
    private static final int THUMB_H = (int)(120 * (720.0 / 1280.0)); // 等比缩放

    public SlideListPanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentY(TOP_ALIGNMENT);

        setBackground(new Color(230,230,230));

        // 空白区域右键菜单
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showBlankAreaMenu(SlideListPanel.this, e.getX(), e.getY());
                }
            }
        });
    }
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        // 拖拽图层永远画在最上层
        if (dragging && dragPanel != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));

            Point mouse = getMousePosition();
            if (mouse != null && dragOffset != null) {
                int x = mouse.x - dragOffset.x;
                int y = mouse.y - dragOffset.y;

                g2.translate(x, y);
                dragPanel.printAll(g2);
            }
            g2.dispose();
        }

    }

    /** 外部调用：刷新缩略图栏 */
    public void setDocument(Document doc) {
        this.document = doc;
        removeAll();

        for (int i = 0; i < doc.getPages().size(); i++) {

            if (dragging && i == hoverIndex && dragIndex != hoverIndex) {
                // 视觉上插入空白位置
                add(Box.createVerticalStrut(THUMB_H + 20));
            }

            if (i != dragIndex) {
                add(new ThumbnailPanel(i));
            }
        }

        revalidate();
        repaint();
    }


    /** 缩略图组件：一个页面对应一个缩略图 */
    private class ThumbnailPanel extends JPanel {
        private BufferedImage thumbnail;

        private int pageIndex;   // 对应 Page.id（真实 index）

        public ThumbnailPanel(int index) {
            this.pageIndex = index;

            setPreferredSize(new Dimension(150, THUMB_H + 20));
            setMaximumSize(new Dimension(150, THUMB_H + 20));
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {

                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressIndex = pageIndex;
                        pressPoint = SwingUtilities.convertPoint(ThumbnailPanel.this, e.getPoint(), SlideListPanel.this);

                        draggingStarted = false;   // 尚未进入拖动模式
                    }

                    if (SwingUtilities.isRightMouseButton(e)) {
                        showThumbnailMenu(ThumbnailPanel.this, pageIndex, e.getX(), e.getY());
                    }
                }

            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (pressIndex < 0) return;

                    Point now = SwingUtilities.convertPoint(ThumbnailPanel.this, e.getPoint(), SlideListPanel.this);

                    // 移动距离是否超过阈值（3 像素）
                    if (!draggingStarted) {
                        if (pressPoint.distance(now) < 3) return;

                        // === 真正进入拖动模式 ===
                        draggingStarted = true;
                        dragging = true;
                        dragIndex = pressIndex;
                        dragPanel = ThumbnailPanel.this;

                        // 计算缩略图左上角在 SlideListPanel 坐标系中的位置
                        Point panelOrigin = SwingUtilities.convertPoint(
                                ThumbnailPanel.this,
                                new Point(0,0),
                                SlideListPanel.this
                        );

                        // 鼠标相对缩略图左上角的偏移
                        Point mouseInList = SwingUtilities.convertPoint(
                                ThumbnailPanel.this,
                                e.getPoint(),
                                SlideListPanel.this
                        );

                        dragOffset = new Point(mouseInList.x - panelOrigin.x, mouseInList.y - panelOrigin.y);

                        revalidate();
                        repaint();

                    }

                    // === 拖动过程中更新 hoverIndex ===
                    int newIndex = now.y / (THUMB_H + 20);
                    newIndex = Math.max(0, Math.min(newIndex, document.getPages().size() - 1));
                    hoverIndex = newIndex;

                    revalidate();
                    SlideListPanel.this.repaint(0, 0, SlideListPanel.this.getWidth(), SlideListPanel.this.getHeight());

                }

            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {

                    // 如果完全没进入拖动模式：这是一个单击选中操作
                    if (!draggingStarted) {

                        selectedIndex = pageIndex;
                        AppFrame.getInstance().getCanvasPanel().setPage(document.getPages().get(pageIndex));
                        refreshUI();

                        pressIndex = -1;
                        pressPoint = null;
                        return;
                    }

                    // === 拖动模式结束 ===
                    dragging = false;

                    if (hoverIndex != dragIndex && hoverIndex >= 0) {
                        Page moved = document.getPages().remove(dragIndex);
                        document.getPages().add(hoverIndex, moved);
                        normalizePageIds();
                        selectedIndex = hoverIndex;
                    }

                    dragIndex = -1;
                    hoverIndex = -1;
                    dragPanel = null;
                    pressIndex = -1;
                    pressPoint = null;

                    refreshUI();
                }

            });

            // === 新增：生成缩略图 ===
            Page p = document.getPages().get(pageIndex);
            thumbnail = ThumbnailRenderer.render(p, THUMB_W, THUMB_H);

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(new Color(200, 200, 200));
            g.fillRect(10, 10, THUMB_W, THUMB_H);

            // === 新增：绘制缩略图 ===
            if (thumbnail != null) {
                g.drawImage(thumbnail, 10, 10, THUMB_W, THUMB_H, null);
            }
            // === 新位置页码 ===
            String text = String.valueOf(pageIndex + 1);

            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);
            int th = fm.getAscent();

            int x = 10 + THUMB_W - tw - 5;   // 缩略图右侧 -5 像素
            int y = 10 + THUMB_H - 5;        // 缩略图底部 -5 像素

            g.setColor(Color.BLACK);
            g.drawString(text, x, y);

            if (pageIndex == selectedIndex) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 120, 215));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            }
        }

    }

    /** 空白区域右键菜单 */
    private void showBlankAreaMenu(Component src, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem add = new JMenuItem("新建页");
        add.addActionListener(e -> addPageAt(document.getPages().size())); // 末尾

        menu.add(add);
        menu.show(src, x, y);
    }

    /** 缩略图右键菜单 */
    private void showThumbnailMenu(Component src, int index, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem above = new JMenuItem("在上方新建页");
        JMenuItem below = new JMenuItem("在下方新建页");
        JMenuItem delete = new JMenuItem("删除页");

        above.addActionListener(e -> addPageAt(index));
        below.addActionListener(e -> addPageAt(index + 1));
        delete.addActionListener(e -> deletePage(index));

        menu.add(above);
        menu.add(below);
        menu.add(delete);

        menu.show(src, x, y);
    }

    /** 新建页（插入） */
    private void addPageAt(int index) {
        Page p = new Page();
        document.getPages().add(index, p);

        normalizePageIds();
        selectedIndex = index; // 选中新建的页

        refreshUI();
    }

    /** 删除页 */
    private void deletePage(int index) {
        if (document.getPages().size() <= 1) {
            JOptionPane.showMessageDialog(this, "至少保留一页");
            return;
        }

        document.getPages().remove(index);

        normalizePageIds();
        selectedIndex = Math.min(index, document.getPages().size() - 1);

        refreshUI();
    }

    /** 重新编号，使 Page.id 与列表顺序一致 */
    private void normalizePageIds() {
        for (int i = 0; i < document.getPages().size(); i++) {
            document.getPages().get(i).setId(i);
        }
    }

    /** 刷新 UI 与画布 */
    private void refreshUI() {

        // 先修正选中页
        if (selectedIndex >= document.getPages().size()) {
            selectedIndex = document.getPages().size() - 1;
        }

        setDocument(document);

        // 让画布显示选中的页
        AppFrame.getInstance()
                .getCanvasPanel()
                .setPage(document.getPages().get(selectedIndex));

        // 滚动到选中缩略图
        SwingUtilities.invokeLater(() -> {
            if (selectedIndex < getComponentCount()) {
                scrollRectToVisible(getComponent(selectedIndex).getBounds());
            }
        });
    }


    public void refreshThumbnail(int pageIndex) {
        Component comp = getComponent(pageIndex);
        if (comp instanceof ThumbnailPanel) {
            ThumbnailPanel tp = (ThumbnailPanel) comp;

            Page p = document.getPages().get(pageIndex);
            tp.thumbnail = ThumbnailRenderer.render(p, THUMB_W, THUMB_H);
            tp.repaint();
        }
    }


}
