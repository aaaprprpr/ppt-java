package ui.menu;

import io.PPTFileWriter;
import model.Document;
import service.EditorService;
import ui.AppFrame;
import ui.slideshow.SlideShowWindow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Map;

/**
 * 顶部菜单栏
 */
public class MainMenuBar extends JMenuBar {
    private JMenuItem saveFileItem;
    private JMenuItem saveAsFileItem;

    public MainMenuBar() {
        // ===== 文件菜单 =====
        JMenu fileMenu = new JMenu("文件");

        // 占位：新建
        JMenuItem newFileItem = new JMenuItem("新建");
        newFileItem.addActionListener(e -> onNewFile());
        fileMenu.add(newFileItem);

        // 占位：打开
        JMenuItem openFileItem = new JMenuItem("打开");
        openFileItem.addActionListener(e -> onOpenFile());
        fileMenu.add(openFileItem);

        // 占位：保存
        saveFileItem = new JMenuItem("保存");
        saveFileItem.addActionListener(e -> onSaveFile());
        fileMenu.add(saveFileItem);

        // 占位：另存为
        saveAsFileItem = new JMenuItem("另存为");
        saveAsFileItem.addActionListener(e -> onSaveAsFile());
        fileMenu.add(saveAsFileItem);



        // 将文件菜单加入菜单栏
        add(fileMenu);


        // ===== 放映菜单 =====
        JMenu playMenu = new JMenu("放映");

        // 从头放映
        JMenuItem playFromStart = new JMenuItem("从头放映");
        playFromStart.addActionListener(e -> {
            SlideShowWindow.showFromPage(0);
        });
        playMenu.add(playFromStart);

        // 从当前页放映
        JMenuItem playFromCurrent = new JMenuItem("从当前页放映");
        playFromCurrent.addActionListener(e -> {
            int idx = AppFrame.getInstance().getSlideListPanel().getSelectedIndex();
            SlideShowWindow.showFromPage(idx);
        });
        playMenu.add(playFromCurrent);



        // 将放映菜单加入菜单栏
        add(playMenu);
    }

    // ===== 文件菜单占位方法（无逻辑实现） =====
    private void onNewFile() {

        // 1. 选择文件保存路径
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("新建 MyPPT 文件");
        chooser.setFileFilter(new FileNameExtensionFilter("MyPPT 文件 (*.myppt)", "myppt"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return; // 用户取消
        }

        File file = chooser.getSelectedFile();

        // 自动补 .myppt 后缀
        if (!file.getName().toLowerCase().endsWith(".myppt")) {
            file = new File(file.getParentFile(), file.getName() + ".myppt");
        }

        try {
            // 2. 创建空文档
            Document doc = new Document();
            doc.setId(java.util.UUID.randomUUID().toString());
            doc.setPages(new java.util.ArrayList<>()); // 空页面列表

            // 空资源
            Map<String, byte[]> emptyResources = new java.util.HashMap<>();

            // 3. 保存为新的 .myppt 文件
            PPTFileWriter.write(doc, emptyResources, file);

            // 4. 将该文件作为“当前文档”打开
            EditorService.getInstance().openDocument(doc, emptyResources);
            AppFrame.getInstance().onDocumentLoaded();

            EditorService.getInstance().setCurrentFile(file);

            System.out.println("新建文件成功：" + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "新建文件失败:\n" + e.getMessage());
        }
    }



    private void onOpenFile() {
        // Swing文件选择器
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("打开文件");

        // 只允许选择 .myppt
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "MyPPT 文件 (*.myppt)", "myppt"
        ));

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return; // 用户取消
        }

        File file = chooser.getSelectedFile();
        System.out.println("开始打开文件: " + file.getAbsolutePath());

        try {
            // 调用 io 层读取文件
            io.PPTFileReader.PPTReadResult readResult = io.PPTFileReader.read(file);

            // 获取文档与资源
            model.Document document = readResult.document;
            Map<String, byte[]> resources = readResult.resources;

            // 将文档交给 EditorService
            service.EditorService.getInstance().openDocument(document, resources);
            AppFrame.getInstance().onDocumentLoaded();



            EditorService.getInstance().setCurrentFile(file);
            System.out.println("打开文件完成，文档页数 = " + document.getPages().size());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "打开文件失败:\n" + e.getMessage());
        }
    }


    private void onSaveFile() {
        File current = EditorService.getInstance().getCurrentFile();

        if (current == null) {
            onSaveAsFile();  // 第一次保存
            return;
        }

        try {
            Document document = EditorService.getInstance().getDocument();
            Map<String, byte[]> resources = EditorService.getInstance().getResources();

            PPTFileWriter.write(document, resources, current);
            System.out.println("保存成功：" + current.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "保存失败：" + e.getMessage());
        }
    }


    private void onSaveAsFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("另存为");
        chooser.setFileFilter(new FileNameExtensionFilter("MyPPT 文件 (*.myppt)", "myppt"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        // 自动补 .myppt 后缀
        if (!file.getName().toLowerCase().endsWith(".myppt")) {
            file = new File(file.getParentFile(), file.getName() + ".myppt");
        }

        try {
            Document document = EditorService.getInstance().getDocument();
            Map<String, byte[]> resources = EditorService.getInstance().getResources();

            // 写入 myppt
            PPTFileWriter.write(document, resources, file);

            // 更新当前文件路径
            EditorService.getInstance().setCurrentFile(file);

            System.out.println("另存为成功：" + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "保存失败：" + e.getMessage());
        }
    }
    public void setEditingEnabled(boolean enabled) {
        saveFileItem.setEnabled(enabled);
        saveAsFileItem.setEnabled(enabled);
    }


}
