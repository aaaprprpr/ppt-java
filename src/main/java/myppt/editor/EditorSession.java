package myppt.editor;

import myppt.io.DocumentIO;
import myppt.model.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class EditorSession {
    private Document document;
    private Path currentFile;
    private final Map<String, byte[]> resources = new HashMap<>();
    private boolean modified;

    public Document getDocument() { return document; }
    public Path getCurrentFile() { return currentFile; }
    public Map<String, byte[]> getResources() { return Collections.unmodifiableMap(resources); }
    public boolean isModified() { return modified; }

    public void newDocument(Path file) throws IOException {
        Document next = new Document();
        next.getPages().add(new Page());
        DocumentIO.write(next, Map.of(), file);
        replace(next, Map.of(), file);
    }

    public void open(Path file) throws IOException {
        var loaded = DocumentIO.read(file);
        normalize(loaded.document());
        replace(loaded.document(), loaded.resources(), file);
    }

    private void replace(Document next, Map<String, byte[]> nextResources, Path file) {
        document = next;
        resources.clear();
        resources.putAll(nextResources);
        currentFile = file;
        modified = false;
    }

    private static void normalize(Document document) throws IOException {
        if (document.getPages() == null) document.setPages(new ArrayList<>());
        if (document.getPages().isEmpty()) document.getPages().add(new Page());
        for (int i = 0; i < document.getPages().size(); i++) {
            Page page = document.getPages().get(i);
            if (page == null) throw new IOException("文档包含空页面");
            page.setId(i);
            if (page.getObjects() == null) page.setObjects(new ArrayList<>());
            if (page.getObjects().contains(null)) throw new IOException("页面包含空对象");
        }
    }

    public void save(Path file) throws IOException {
        DocumentIO.write(document, resources, file);
        currentFile = file;
        modified = false;
    }

    public void markChanged() {
        if (document != null) modified = true;
    }

    public Page addPage(int index) {
        Page page = new Page();
        document.getPages().add(index, page);
        pagesChanged();
        return page;
    }

    public boolean deletePage(int index) {
        if (document.getPages().size() <= 1) return false;
        document.getPages().remove(index);
        pagesChanged();
        return true;
    }

    public void movePage(int from, int to) {
        if (from == to) return;
        Page page = document.getPages().remove(from);
        document.getPages().add(to, page);
        pagesChanged();
    }

    private void pagesChanged() {
        for (int i = 0; i < document.getPages().size(); i++) document.getPages().get(i).setId(i);
        markChanged();
    }

    public PPTImage importImage(Path file) throws IOException {
        if (document == null) throw new IOException("请先新建或打开文档");
        byte[] data = Files.readAllBytes(file);
        var image = ImageIO.read(new ByteArrayInputStream(data));
        if (image == null) throw new IOException("不支持的图片格式");
        String name = file.getFileName().toString();
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
        String resource = "resources/" + UUID.randomUUID() + extension;
        resources.put(resource, data);
        PPTImage object = new PPTImage();
        object.setSrc(resource);
        object.setX(10);
        object.setY(10);
        double scale = Math.min(1, 400.0 / Math.max(image.getWidth(), image.getHeight()));
        object.setWidth(image.getWidth() * scale);
        object.setHeight(image.getHeight() * scale);
        return object;
    }
}
