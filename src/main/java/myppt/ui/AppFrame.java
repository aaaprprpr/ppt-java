package myppt.ui;

import myppt.editor.EditorSession;
import myppt.model.PPTObject;
import myppt.render.SlideRenderer;
import myppt.ui.canvas.CanvasPanel;
import myppt.ui.property.PropertyPanel;
import myppt.ui.slides.SlideListPanel;
import myppt.ui.slides.SlideShowWindow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AppFrame extends JFrame {
    private final EditorSession session = new EditorSession();
    private final SlideRenderer renderer = new SlideRenderer(session.getResources());
    private final CanvasPanel canvas;
    private final PropertyPanel properties;
    private final SlideListPanel slides;
    private final ToolBarPanel toolbar;
    private final List<JMenuItem> documentActions = new ArrayList<>();

    public AppFrame() {
        super("MyPPT 幻灯片编辑器");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        canvas = new CanvasPanel(renderer, this::selectionChanged, this::documentChanged);
        properties = new PropertyPanel(canvas::propertyChanged);
        slides = new SlideListPanel(session, renderer, canvas::setPage, canvas::finishTextEdit, this::documentChanged);
        toolbar = new ToolBarPanel(canvas, this::addImage);
        setJMenuBar(createMenu());
        add(toolbar, BorderLayout.NORTH);
        add(slides, BorderLayout.WEST);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        add(properties, BorderLayout.EAST);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (allowLeave()) dispose();
            }
        });
    }

    private JMenuBar createMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("文件");
        menuItem(file, "新建", "control N", this::newDocument, false);
        menuItem(file, "打开", "control O", this::openDocument, false);
        menuItem(file, "保存", "control S", () -> save(false), true);
        menuItem(file, "另存为", "control shift S", () -> save(true), true);
        bar.add(file);
        JMenu play = new JMenu("放映");
        menuItem(play, "从头放映", "F5", () -> play(0), true);
        menuItem(play, "从当前页放映", "shift F5", () -> play(slides.getSelectedIndex()), true);
        bar.add(play);
        return bar;
    }

    private void menuItem(JMenu menu, String title, String shortcut, Runnable action, boolean needsDocument) {
        JMenuItem item = new JMenuItem(title);
        item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
        item.addActionListener(e -> action.run());
        if (needsDocument) {
            documentActions.add(item);
            item.setEnabled(false);
        }
        menu.add(item);
    }

    private void selectionChanged(PPTObject selected) {
        properties.showProperties(selected);
        toolbar.setDeleteEnabled(selected != null);
    }

    private void documentChanged() {
        session.markChanged();
        slides.refreshThumbnail(canvas.getPage());
        canvas.repaint();
        updateTitle();
    }

    private void documentLoaded() {
        renderer.clearCache();
        slides.documentLoaded();
        toolbar.setEditingEnabled(true);
        documentActions.forEach(item -> item.setEnabled(true));
        updateTitle();
    }

    private void updateTitle() {
        String name = session.getCurrentFile() == null ? "未命名" : session.getCurrentFile().getFileName().toString();
        setTitle((session.isModified() ? "* " : "") + name + " — MyPPT 幻灯片编辑器");
    }

    private void newDocument() {
        if (!allowLeave()) return;
        Path file = chooseSaveFile("新建 MyPPT 文件");
        if (file == null) return;
        try {
            session.newDocument(file);
            documentLoaded();
        } catch (Exception e) {
            showError("新建失败", e);
        }
    }

    private void openDocument() {
        if (!allowLeave()) return;
        JFileChooser chooser = documentChooser();
        chooser.setDialogTitle("打开 MyPPT 文件");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            session.open(chooser.getSelectedFile().toPath());
            documentLoaded();
        } catch (Exception e) {
            showError("打开失败", e);
        }
    }

    private boolean save(boolean saveAs) {
        canvas.finishTextEdit();
        if (session.getDocument() == null) return false;
        Path file = session.getCurrentFile();
        if (saveAs || file == null) file = chooseSaveFile("保存 MyPPT 文件");
        if (file == null) return false;
        try {
            session.save(file);
            updateTitle();
            return true;
        } catch (Exception e) {
            showError("保存失败", e);
            return false;
        }
    }

    private boolean allowLeave() {
        canvas.finishTextEdit();
        if (!session.isModified()) return true;
        int answer = JOptionPane.showConfirmDialog(this, "文档有未保存的修改，是否保存？", "保存修改",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.NO_OPTION || answer == JOptionPane.YES_OPTION && save(false);
    }

    private JFileChooser documentChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("MyPPT 文件 (*.myppt)", "myppt"));
        if (session.getCurrentFile() != null) chooser.setSelectedFile(session.getCurrentFile().toFile());
        return chooser;
    }

    private Path chooseSaveFile(String title) {
        JFileChooser chooser = documentChooser();
        chooser.setDialogTitle(title);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        Path file = chooser.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".myppt")) {
            file = file.resolveSibling(file.getFileName() + ".myppt");
        }
        if (Files.exists(file) && JOptionPane.showConfirmDialog(this, "文件已存在，是否覆盖？", "覆盖文件",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return null;
        return file;
    }

    private void addImage() {
        canvas.finishTextEdit();
        if (canvas.getPage() == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("图片", "png", "jpg", "jpeg", "gif", "bmp"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            canvas.addObject(session.importImage(chooser.getSelectedFile().toPath()));
        } catch (Exception e) {
            showError("插入图片失败", e);
        }
    }

    private void play(int startPage) {
        canvas.finishTextEdit();
        if (session.getDocument() != null) {
            new SlideShowWindow(session.getDocument(), renderer, startPage).setVisible(true);
        }
    }

    private void showError(String title, Exception error) {
        error.printStackTrace();
        JOptionPane.showMessageDialog(this, error.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }
}
