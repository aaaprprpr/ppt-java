package myppt;

import myppt.editor.ObjectGeometry;
import myppt.model.*;
import myppt.render.SlideRenderer;
import myppt.ui.canvas.CanvasPanel;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EditorBehaviorTest {
    @Test void deletingAnIdenticalShapeRemovesTheSelectedInstance() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CanvasPanel canvas = canvas();
            Page page = new Page();
            canvas.setPage(page);
            PPTRect first = new PPTRect(), second = new PPTRect();
            canvas.addObject(first);
            canvas.addObject(second);
            canvas.deleteSelected();
            assertEquals(1, page.getObjects().size());
            assertSame(first, page.getObjects().getFirst());
            assertNull(canvas.getSelectedObject());
        });
    }

    @Test void switchingPagesCommitsTextAndResetsTools() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CanvasPanel canvas = canvas();
            Page oldPage = new Page(), newPage = new Page();
            canvas.setPage(oldPage);
            canvas.setSize(1280, 720);
            canvas.setZoom(1);
            PPTText text = new PPTText();
            text.setX(20);
            text.setY(20);
            text.setText("old");
            canvas.addObject(text);
            press(canvas, 30, 30, 2);
            JScrollPane editor = (JScrollPane) canvas.getComponent(0);
            assertTrue(editor.isVisible());
            ((JTextArea) editor.getViewport().getView()).setText("中文\n下一行");
            canvas.setPage(newPage);
            assertEquals("中文\n下一行", text.getText());
            assertFalse(editor.isVisible());
            canvas.selectTool(CanvasPanel.Tool.RECT);
            press(canvas, 100, 100, 1);
            canvas.setPage(oldPage);
            press(canvas, 200, 200, 1);
            assertEquals(1, oldPage.getObjects().size());
            assertTrue(newPage.getObjects().isEmpty());
        });
    }

    @Test void centeredZoomUsesTheSameCoordinatesForTextEditingAndHitTesting() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CanvasPanel canvas = canvas();
            canvas.setPage(new Page());
            canvas.setSize(1600, 900);
            canvas.setZoom(0.5);
            PPTText text = new PPTText();
            text.setX(100);
            text.setY(100);
            text.setText("Text");
            canvas.addObject(text);
            press(canvas, 530, 320, 2);
            Component editor = canvas.getComponent(0);
            assertTrue(editor.isVisible());
            assertEquals(530, editor.getX());
            assertEquals(320, editor.getY());
            assertEquals(100, canvas.toSlide(new Point(530, 320)).getX());
            canvas.finishTextEdit();
        });
    }

    @Test void rectangleResizeKeepsTheOppositeCornerFixedAtMinimumSize() {
        PPTRect rect = new PPTRect();
        rect.setX(10);
        rect.setY(20);
        rect.setWidth(100);
        rect.setHeight(60);
        ObjectGeometry.resize(rect, 0, 500, 500);
        assertEquals(5, rect.getWidth());
        assertEquals(5, rect.getHeight());
        assertEquals(110, rect.getX() + rect.getWidth());
        assertEquals(80, rect.getY() + rect.getHeight());
    }

    @Test void slowTextResizeAccumulatesSmallMouseMovements() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CanvasPanel canvas = canvas();
            canvas.setPage(new Page());
            canvas.setSize(1280, 720);
            canvas.setZoom(1);
            PPTText text = new PPTText();
            text.setText("A line of text");
            text.setX(100);
            text.setY(100);
            canvas.addObject(text);
            var bounds = ObjectGeometry.bounds(text);
            int x = (int) Math.round(bounds.getMaxX()), y = (int) Math.round(bounds.getMaxY());
            press(canvas, x, y, 1);
            for (int delta = 1; delta <= 30; delta++) {
                canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_DRAGGED,
                        System.currentTimeMillis(), MouseEvent.BUTTON1_DOWN_MASK,
                        x + delta, y, 0, false, MouseEvent.NOBUTTON));
            }
            assertTrue(text.getFontSize() > 20);
            assertEquals(100, text.getX());
            assertEquals(100, text.getY());
        });
    }

    @Test void reverseDirectionLinesMoveAndResizeTheirActualEndpoints() {
        PPTLine line = new PPTLine();
        line.setX(100);
        line.setY(20);
        line.setX2(10);
        line.setY2(200);
        ObjectGeometry.resize(line, 0, 5, -10);
        assertEquals(105, line.getX());
        assertEquals(10, line.getY());
        assertEquals(10, line.getX2());
        ObjectGeometry.move(line, 20, 30);
        assertEquals(125, line.getX());
        assertEquals(30, line.getX2());
        assertEquals(230, line.getY2());
    }

    @Test void rendererUsesBackgroundAndShapeStylesAndMeasuresMultilineText() {
        Page page = new Page();
        page.setBackgroundColor("#123456");
        PPTRect rect = new PPTRect();
        rect.setX(100);
        rect.setY(100);
        rect.setWidth(100);
        rect.setHeight(100);
        rect.setFillEnabled(true);
        rect.setFillColor("#ff0000");
        page.getObjects().add(rect);
        var image = new SlideRenderer(Map.of()).thumbnail(page, 1280, 720);
        assertEquals(new Color(0x123456).getRGB(), image.getRGB(20, 20));
        assertEquals(Color.RED.getRGB(), image.getRGB(150, 150));
        PPTText text = new PPTText();
        text.setText("First\nSecond");
        assertEquals(ObjectGeometry.lineHeight(text) * 2, ObjectGeometry.bounds(text).getHeight(), 0.01);
        ObjectGeometry.resize(text, 7, 50, 50);
        assertTrue(text.getFontSize() > 20);
    }

    private static CanvasPanel canvas() {
        return new CanvasPanel(new SlideRenderer(Map.of()), ignored -> {}, () -> {});
    }

    private static void press(CanvasPanel canvas, int x, int y, int count) {
        canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                0, x, y, count, false, MouseEvent.BUTTON1));
    }
}
