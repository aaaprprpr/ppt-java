package ui.slideshow;

import model.Document;
import model.Page;
import service.EditorService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 全屏放映窗口
 */
public class SlideShowWindow extends JFrame {

    private static Document doc;
    private static int pageIndex;
    private static final int END_PAGE_ID = 999999; // 结束页的临时ID

    public static void showFromPage(int startPage) {
        doc = EditorService.getInstance().getDocument();
        pageIndex = startPage;

        SlideShowWindow win = new SlideShowWindow();
        win.setVisible(true);
    }

    private SlideShowWindow() {
        setUndecorated(true);         // 无边框全屏
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setBackground(Color.BLACK);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                int code = e.getKeyCode();

                // ESC 退出
                if (code == KeyEvent.VK_ESCAPE) {
                    dispose();
                }

                // 上一页
                if (code == KeyEvent.VK_UP ||
                        code == KeyEvent.VK_LEFT) {
                    pageIndex--;
                    if (pageIndex < 0) pageIndex = 0;
                    repaint();
                }

                // 下一页
                if (code == KeyEvent.VK_DOWN ||
                        code == KeyEvent.VK_RIGHT) {
                    pageIndex++;
                    if (pageIndex > doc.getPages().size()) pageIndex = doc.getPages().size(); // 结尾页
                    repaint();
                }
            }
        });

        addMouseWheelListener(e -> {
            if (e.getWheelRotation() > 0) {
                // 下滚 = 下一页
                pageIndex++;
                if (pageIndex > doc.getPages().size()) pageIndex = doc.getPages().size();
            } else {
                // 上滚 = 上一页
                pageIndex--;
                if (pageIndex < 0) pageIndex = 0;
            }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // 左键 → 下一页
                    pageIndex++;
                    if (pageIndex > doc.getPages().size())
                        pageIndex = doc.getPages().size();
                    repaint();
                }
            }
        });

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        // ======= 结束页 =======
        if (pageIndex == doc.getPages().size()) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 32));

            String msg = "放映结束，按 ESC 键退出";
            int tw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - tw) / 2, getHeight() / 2);
            return;
        }

        // ======= 正常页 =======
        Page page = doc.getPages().get(pageIndex);

        // 绘制白底
        int cw = getWidth();
        int ch = getHeight();

        double pptW = 1280, pptH = 720;
        double scale = Math.min(cw / pptW, ch / pptH);

        int offsetX = (int) ((cw - pptW * scale) / 2);
        int offsetY = (int) ((ch - pptH * scale) / 2);

        g2.scale(scale, scale);
        g2.translate(offsetX / scale, offsetY / scale);

        // 白色背景
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, (int)pptW, (int)pptH);

        // 绘制 Page 中的所有对象
        ui.canvas.drawable.Drawable d;

        for (var obj : page.getObjects()) {
            d = ui.canvas.drawable.DrawableFactory.createDrawable(obj);
            if (d != null) {
                ui.canvas.drawable.DrawableModelMapper.sync(d, obj);
                d.draw(g2);

            }
        }

        g2.translate(-offsetX / scale, -offsetY / scale);
        g2.scale(1/scale, 1/scale);
    }
}
