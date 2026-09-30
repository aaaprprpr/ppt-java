package ui;

import javax.swing.*;
import java.awt.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ui.menu.MainMenuBar;
import ui.menu.ToolBarPanel;
import ui.slide.SlideListPanel;
import ui.canvas.CanvasPanel;
import ui.property.PropertyPanel;

/**
 * 主界面窗口，仅负责拼装布局，不承担业务逻辑
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AppFrame extends JFrame {
    private static AppFrame instance;
    public static AppFrame getInstance() {
        return instance;
    }

    private SlideListPanel slideListPanel;
    private CanvasPanel canvasPanel;
    private PropertyPanel propertyPanel;
    private ToolBarPanel toolBarPanel;   // ★新增：保存工具栏引用

    public AppFrame() {
        instance = this;


        // 中文注释：初始化主窗口
        setTitle("MyPPT 幻灯片编辑器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // 中文注释：创建三个主要区域
        slideListPanel = new SlideListPanel();
        canvasPanel = new CanvasPanel();
        propertyPanel = new PropertyPanel();

        // 中文注释：创建顶部菜单栏
        MainMenuBar menu = new MainMenuBar();
        setJMenuBar(menu);

        // ★启动时禁用保存相关
        menu.setEditingEnabled(false);


        // 中文注释：主布局 BorderLayout
        setLayout(new BorderLayout());


        // ========= 工具栏放在顶部（菜单栏下面） =========
        toolBarPanel = new ToolBarPanel(canvasPanel);
        toolBarPanel.setEditingEnabled(false);
        add(toolBarPanel, BorderLayout.NORTH);


        // 左侧页面列表（固定宽度）
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(150, 0));
        leftPanel.add(new JScrollPane(
                slideListPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        ), BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);


        // 中间画布
        add(new JScrollPane(canvasPanel), BorderLayout.CENTER);


        // 右侧属性面板（固定宽度）
        add(propertyPanel, BorderLayout.EAST);
    }


    /**
     * 当文档加载（新建/打开）后调用，刷新整个UI
     */
    public void onDocumentLoaded() {

        var doc = service.EditorService.getInstance().getDocument();

        if (doc == null) {
            System.out.println("文档为空，无法刷新 UI");
            return;
        }

        // 1. 刷新左侧缩略图
        slideListPanel.setDocument(doc);

        // 2. 默认显示第一页
        if (!doc.getPages().isEmpty()) {
            var firstPage = doc.getPages().get(0);
            canvasPanel.setPage(firstPage);
        }
        toolBarPanel.setEditingEnabled(true);
        ((MainMenuBar)getJMenuBar()).setEditingEnabled(true);

    }
    public JLayeredPane getLayeredPaneWrapper() {
        return getLayeredPane();
    }

}
