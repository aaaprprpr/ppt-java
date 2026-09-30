package myppt.ui.canvas;

import myppt.editor.ObjectGeometry;
import myppt.model.PPTText;
import myppt.render.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.function.Consumer;

final class TextEditor extends JScrollPane {
    private final JTextArea input = new JTextArea();
    private final Consumer<PPTText> onCommit;
    private PPTText model;

    TextEditor(Consumer<PPTText> onCommit) {
        super();
        this.onCommit = onCommit;
        setViewportView(input);
        setVisible(false);
        input.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) finish();
            }
        });
        input.getInputMap().put(KeyStroke.getKeyStroke("control ENTER"), "commit");
        input.getActionMap().put("commit", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { finish(); }
        });
        input.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        input.getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { cancel(); }
        });
    }

    PPTText getModel() { return model; }

    void begin(PPTText text) {
        finish();
        model = text;
        input.setText(text.getText());
        input.setForeground(Colors.parse(text.getFontColor(), Color.BLACK));
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            if (model == text) input.requestFocusInWindow();
        });
    }

    void position(int x, int y, double zoom) {
        if (model == null) return;
        input.setFont(ObjectGeometry.font(model).deriveFont((float) Math.max(1, model.getFontSize() * zoom)));
        setBounds(x, y, Math.max(180, (int) (300 * zoom)), Math.max(80, (int) (150 * zoom)));
    }

    void finish() {
        PPTText edited = model;
        if (edited == null) return;
        model = null;
        setVisible(false);
        if (!Objects.equals(edited.getText(), input.getText())) {
            edited.setText(input.getText());
            onCommit.accept(edited);
        }
        getParent().repaint();
    }

    private void cancel() {
        model = null;
        setVisible(false);
        getParent().repaint();
    }
}
