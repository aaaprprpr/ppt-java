package myppt.ui.slides;

import myppt.editor.EditorSession;
import myppt.model.Page;
import myppt.render.Colors;
import myppt.render.SlideRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class SlideListPanel extends JScrollPane {
    private final EditorSession session;
    private final SlideRenderer renderer;
    private final Consumer<Page> onSelection;
    private final Runnable beforeChange;
    private final Runnable onChange;
    private final DefaultListModel<Page> model = new DefaultListModel<>();
    private final JList<Page> list = new JList<>(model);
    private final Map<Page, BufferedImage> thumbnails = new IdentityHashMap<>();
    private boolean rebuilding;
    private int pressIndex = -1;
    private Point pressPoint;

    public SlideListPanel(EditorSession session, SlideRenderer renderer, Consumer<Page> onSelection,
                          Runnable beforeChange, Runnable onChange) {
        this.session = session;
        this.renderer = renderer;
        this.onSelection = onSelection;
        this.beforeChange = beforeChange;
        this.onChange = onChange;
        setViewportView(list);
        setPreferredSize(new Dimension(160, 0));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(94);
        list.setCellRenderer((items, page, index, selected, focused) -> {
            BufferedImage image = thumbnails.computeIfAbsent(page, p -> renderer.thumbnail(p, 120, 68));
            JLabel label = new JLabel(String.valueOf(index + 1), new ImageIcon(image), SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(selected ? new Color(200, 225, 250) : new Color(235, 235, 235));
            label.setBorder(BorderFactory.createLineBorder(selected ? new Color(0, 120, 215) : Color.LIGHT_GRAY, 2));
            return label;
        });
        list.addListSelectionListener(e -> {
            if (!rebuilding && !e.getValueIsAdjusting() && list.getSelectedValue() != null) {
                beforeChange.run();
                onSelection.accept(list.getSelectedValue());
            }
        });
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) showMenu(e);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressIndex = indexAt(e.getPoint());
                    pressPoint = e.getPoint();
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                int target = list.locationToIndex(e.getPoint());
                if (pressIndex >= 0 && target >= 0 && pressPoint != null
                        && pressPoint.distance(e.getPoint()) > 5 && pressIndex != target) {
                    beforeChange.run();
                    session.movePage(pressIndex, target);
                    rebuild(target);
                    onChange.run();
                }
                pressIndex = -1;
                pressPoint = null;
            }
        };
        list.addMouseListener(mouse);
    }

    public int getSelectedIndex() { return list.getSelectedIndex(); }

    public void documentLoaded() {
        thumbnails.clear();
        pressIndex = -1;
        pressPoint = null;
        rebuild(0);
    }

    private void rebuild(int selectedIndex) {
        rebuilding = true;
        try {
            model.clear();
            if (session.getDocument() != null) model.addAll(session.getDocument().getPages());
            if (!model.isEmpty()) list.setSelectedIndex(Math.max(0, Math.min(selectedIndex, model.size() - 1)));
        } finally {
            rebuilding = false;
        }
        onSelection.accept(list.getSelectedValue());
        list.ensureIndexIsVisible(list.getSelectedIndex());
    }

    public void refreshThumbnail(Page page) {
        thumbnails.remove(page);
        list.repaint();
    }

    private int indexAt(Point point) {
        int index = list.locationToIndex(point);
        return index >= 0 && list.getCellBounds(index, index).contains(point) ? index : -1;
    }

    private void showMenu(MouseEvent event) {
        if (session.getDocument() == null) return;
        int index = indexAt(event.getPoint());
        JPopupMenu menu = new JPopupMenu();
        if (index < 0) {
            item(menu, "新建页", () -> addPage(model.size()));
        } else {
            item(menu, "在上方新建页", () -> addPage(index));
            item(menu, "在下方新建页", () -> addPage(index + 1));
            item(menu, "删除页", () -> {
                beforeChange.run();
                if (!session.deletePage(index)) {
                    JOptionPane.showMessageDialog(this, "至少保留一页");
                    return;
                }
                thumbnails.keySet().retainAll(session.getDocument().getPages());
                rebuild(Math.min(index, model.size() - 2));
                onChange.run();
            });
            item(menu, "页面背景色", () -> {
                Page page = model.get(index);
                Color color = JColorChooser.showDialog(this, "页面背景色",
                        Colors.parse(page.getBackgroundColor(), Color.WHITE));
                if (color != null) {
                    page.setBackgroundColor(Colors.toHex(color));
                    refreshThumbnail(page);
                    onChange.run();
                }
            });
        }
        menu.show(list, event.getX(), event.getY());
    }

    private void addPage(int index) {
        beforeChange.run();
        session.addPage(index);
        rebuild(index);
        onChange.run();
    }

    private static void item(JPopupMenu menu, String title, Runnable action) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(e -> action.run());
        menu.add(item);
    }
}
