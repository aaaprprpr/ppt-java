package myppt;

import myppt.io.DocumentIO;
import myppt.model.*;
import myppt.ui.AppFrame;
import myppt.ui.canvas.CanvasPanel;
import myppt.ui.slides.SlideShowWindow;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Tag("desktop")
class DesktopSmokeTest {
    @TempDir Path directory;

    @Test void newEditSaveReopenAndPresent() throws Exception {
        assertFalse(GraphicsEnvironment.isHeadless(), "desktopTest requires a desktop session");
        Path document = directory.resolve("演示.myppt");
        Path picture = directory.resolve("picture.png");
        BufferedImage bitmap = new BufferedImage(160, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D imageGraphics = bitmap.createGraphics();
        imageGraphics.setColor(new Color(255, 180, 80));
        imageGraphics.fillRect(0, 0, 160, 80);
        imageGraphics.dispose();
        ImageIO.write(bitmap, "png", picture.toFile());
        AtomicReference<Throwable> backgroundError = new AtomicReference<>();
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> backgroundError.set(error));
        try {
            SwingUtilities.invokeAndWait(() -> {
                com.formdev.flatlaf.FlatLightLaf.setup();
                AppFrame frame = new AppFrame();
                try {
                    frame.setVisible(true);
                    JMenu file = frame.getJMenuBar().getMenu(0);
                    JMenu play = frame.getJMenuBar().getMenu(1);
                    assertFalse(file.getItem(2).isEnabled());
                    assertFalse(play.getItem(0).isEnabled());
                    chooseFile(() -> file.getItem(0).doClick(), document);
                    CanvasPanel canvas = find(frame, CanvasPanel.class);
                    assertNotNull(canvas.getPage());
                    assertEquals(1, find(frame, JList.class).getModel().getSize());
                    PPTRect rectangle = new PPTRect();
                    rectangle.setX(100);
                    rectangle.setY(180);
                    rectangle.setWidth(260);
                    rectangle.setHeight(180);
                    rectangle.setFillEnabled(true);
                    rectangle.setFillColor("#a5d8ff");
                    canvas.addObject(rectangle);
                    PPTText text = new PPTText();
                    text.setX(430);
                    text.setY(180);
                    text.setFontSize(32);
                    text.setText("before editing");
                    canvas.addObject(text);
                    canvas.setZoom(0.5);
                    frame.validate();
                    var origin = canvas.toSlide(new Point(0, 0));
                    int x = (int) Math.round((text.getX() + 10 - origin.getX()) * 0.5);
                    int y = (int) Math.round((text.getY() + 10 - origin.getY()) * 0.5);
                    canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED,
                            System.currentTimeMillis(), 0, x, y, 2, false, MouseEvent.BUTTON1));
                    JScrollPane editor = (JScrollPane) canvas.getComponent(0);
                    assertTrue(editor.isVisible());
                    ((JTextArea) editor.getViewport().getView()).setText("MyPPT 编辑测试\n中文、多行文本");
                    // Saving via the menu must flush an editor that still owns focus.
                    file.getItem(2).doClick();
                    assertEquals("MyPPT 编辑测试\n中文、多行文本", text.getText());
                    assertFalse(editor.isVisible());
                    chooseFile(() -> button(frame, "图片").doClick(), picture);
                    assertInstanceOf(PPTImage.class, canvas.getSelectedObject());
                    file.getItem(2).doClick();
                    assertFalse(frame.getTitle().startsWith("*"));
                    try {
                        Files.delete(picture);
                    } catch (java.io.IOException e) {
                        throw new AssertionError(e);
                    }
                    chooseFile(() -> file.getItem(1).doClick(), document);
                    assertEquals(3, canvas.getPage().getObjects().size());
                    assertEquals("MyPPT 编辑测试\n中文、多行文本",
                            ((PPTText) canvas.getPage().getObjects().get(1)).getText());
                    JList<?> pages = find(frame, JList.class);
                    popupAction(pages, 0, "在下方新建页");
                    assertEquals(2, pages.getModel().getSize());
                    PPTText secondPageText = new PPTText();
                    secondPageText.setText("第二页");
                    canvas.addObject(secondPageText);
                    pages.setSelectedIndex(0);
                    assertEquals(3, canvas.getPage().getObjects().size());
                    pages.dispatchEvent(new MouseEvent(pages, MouseEvent.MOUSE_PRESSED,
                            System.currentTimeMillis(), 0, 60, 40, 1, false, MouseEvent.BUTTON1));
                    pages.dispatchEvent(new MouseEvent(pages, MouseEvent.MOUSE_RELEASED,
                            System.currentTimeMillis(), 0, 60, 140, 1, false, MouseEvent.BUTTON1));
                    assertEquals(1, pages.getSelectedIndex());
                    assertEquals(3, canvas.getPage().getObjects().size());
                    assertSame(secondPageText, ((Page) pages.getModel().getElementAt(0)).getObjects().getFirst());
                    file.getItem(2).doClick();
                    frame.validate();
                    capture(frame);
                    play.getItem(0).doClick();
                    SlideShowWindow show = null;
                    for (Window window : Window.getWindows()) {
                        if (window instanceof SlideShowWindow slideShow && window.isVisible()) show = slideShow;
                    }
                    assertNotNull(show);
                    JComponent screen = (JComponent) show.getContentPane();
                    for (String key : new String[]{"RIGHT", "RIGHT", "LEFT", "ESCAPE"}) {
                        screen.getActionMap().get(key).actionPerformed(new ActionEvent(screen, 0, key));
                    }
                    assertFalse(show.isDisplayable());
                } finally {
                    for (Window window : Window.getWindows()) window.dispose();
                }
            });
            assertNull(backgroundError.get(), "No exceptions should escape on the Swing event thread");
            var saved = DocumentIO.read(document);
            assertEquals(2, saved.document().getPages().size());
            assertEquals(3, saved.document().getPages().getLast().getObjects().size());
            assertEquals(1, saved.resources().size());
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous);
        }
    }

    private static void popupAction(JList<?> list, int index, String text) {
        list.dispatchEvent(new MouseEvent(list, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                0, 30, index * list.getFixedCellHeight() + 30, 1, true, MouseEvent.BUTTON3));
        for (MenuElement element : MenuSelectionManager.defaultManager().getSelectedPath()) {
            if (element instanceof JPopupMenu popup) {
                for (Component child : popup.getComponents()) {
                    if (child instanceof JMenuItem item && text.equals(item.getText())) {
                        item.doClick();
                        MenuSelectionManager.defaultManager().clearSelectedPath();
                        return;
                    }
                }
            }
        }
        fail("Missing page action: " + text);
    }

    private static void chooseFile(Runnable action, Path path) {
        AtomicBoolean chosen = new AtomicBoolean();
        int[] attempts = {0};
        Timer timer = new Timer(100, null);
        timer.addActionListener(e -> {
            for (Window window : Window.getWindows()) {
                if (!window.isVisible()) continue;
                JFileChooser chooser = find(window, JFileChooser.class);
                if (chooser != null) {
                    timer.stop();
                    chosen.set(true);
                    chooser.setSelectedFile(path.toFile());
                    chooser.approveSelection();
                    return;
                }
            }
            if (++attempts[0] > 100) {
                timer.stop();
                for (Window window : Window.getWindows()) if (window instanceof JDialog) window.dispose();
            }
        });
        timer.start();
        try {
            action.run();
        } finally {
            timer.stop();
        }
        assertTrue(chosen.get(), "The expected file chooser did not open");
    }

    private static <T> T find(Component root, Class<T> type) {
        if (type.isInstance(root)) return type.cast(root);
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                T found = find(child, type);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JButton button(Container root, String text) {
        for (Component child : root.getComponents()) {
            if (child instanceof JButton button && text.equals(button.getText())) return button;
            if (child instanceof Container container) {
                JButton found = button(container, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void capture(JFrame frame) {
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        frame.printAll(graphics);
        graphics.dispose();
        try {
            Path file = Path.of("build/reports/desktop/editor.png");
            Files.createDirectories(file.getParent());
            ImageIO.write(image, "png", file.toFile());
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }
}
