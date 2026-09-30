package service;

import lombok.Data;
import model.Document;
import model.Page;

import java.io.File;
import java.util.Map;

/**
 * 管理当前打开的PPT文档
 */
@Data
public class EditorService {
    private File currentFile;// 当前文件
    private Document currentDocument;// 当前文档
    private Map<String, byte[]> resourceMap;// 资源文件，例如图片
    private static final EditorService instance = new EditorService();// 单例

    public static EditorService getInstance() {
        return instance;
    }

    private EditorService() {}
    public void createEmptyDocument() {
        Document doc = new Document();

        Page page = new Page();
        page.setId(0);
        page.setBackgroundColor("white");

        doc.getPages().add(page);

        this.currentDocument = doc;
        this.currentFile = null;

        // UI 刷新
        ui.AppFrame.getInstance().onDocumentLoaded();
    }

    // 打开文件后调用
    public void openDocument(Document document, Map<String, byte[]> resources) {
        this.currentDocument = document;
        this.resourceMap = resources;

        // 自动创建第一页
        if (document.getPages() == null || document.getPages().isEmpty()) {
            Page page = new Page();
            page.setId(0);                     // 页索引 = 0
            page.setBackgroundColor("white");  // 默认背景
            document.getPages().add(page);     // 添加到列表
        }

        // 对所有页重新编号，使 id = index
        normalizePageIds(document);
    }

    private void normalizePageIds(Document document) {
        for (int i = 0; i < document.getPages().size(); i++) {
            document.getPages().get(i).setId(i);
        }
    }

    public Document getDocument() {
        return currentDocument;
    }

    public Map<String, byte[]> getResources() {
        return resourceMap;
    }
}
