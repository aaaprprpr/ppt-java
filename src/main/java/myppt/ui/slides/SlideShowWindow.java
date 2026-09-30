package myppt.ui.slides;

import myppt.model.Document;
import myppt.render.SlideRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public final class SlideShowWindow extends JFrame {
    private final Document document;
    private final SlideRenderer renderer;
    private int pageIndex;

    public SlideShowWindow(Document document, SlideRenderer renderer, int startPage) {
        if (document == null || document.getPages().isEmpty()) {
            throw new IllegalArgumentException("没有可放映的页面");
        }
        this.document = document;
        this.renderer = renderer;
        pageIndex = Math.max(0, Math.min(startPage, document.getPages().size() - 1));
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        JPanel screen = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    if (pageIndex == document.getPages().size()) {
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("SansSerif", Font.BOLD, 32));
                        String message = "放映结束，按 ESC 键退出";
                        g.drawString(message, (getWidth() - g.getFontMetrics().stringWidth(message)) / 2, getHeight() / 2);
                    } else {
                        double scale = Math.min(getWidth() / (double) SlideRenderer.WIDTH,
                                getHeight() / (double) SlideRenderer.HEIGHT);
                        g.translate((getWidth() - SlideRenderer.WIDTH * scale) / 2,
                                (getHeight() - SlideRenderer.HEIGHT * scale) / 2);
                        g.scale(scale, scale);
                        renderer.paintPage(g, document.getPages().get(pageIndex), null);
                    }
                } finally {
                    g.dispose();
                }
            }
        };
        screen.setBackground(Color.BLACK);
        setContentPane(screen);
        bind(screen, "ESCAPE", this::dispose);
        for (String key : new String[]{"LEFT", "UP"}) bind(screen, key, () -> advance(-1));
        for (String key : new String[]{"RIGHT", "DOWN", "SPACE"}) bind(screen, key, () -> advance(1));
        screen.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) advance(1);
            }
        });
        screen.addMouseWheelListener(e -> advance(Integer.signum(e.getWheelRotation())));
    }

    private void advance(int step) {
        pageIndex = Math.max(0, Math.min(pageIndex + step, document.getPages().size()));
        repaint();
    }

    private static void bind(JPanel panel, String key, Runnable action) {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        panel.getActionMap().put(key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }
}
