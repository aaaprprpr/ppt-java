package myppt.io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import myppt.model.Document;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.*;

public final class DocumentIO {
    // Older documents contain unused dirty/rotation/zIndex fields.
    private static final ObjectMapper JSON = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private DocumentIO() {}

    public record LoadedDocument(Document document, Map<String, byte[]> resources) {}

    public static LoadedDocument read(Path file) throws IOException {
        Document document = null;
        Map<String, byte[]> resources = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("document.json".equals(entry.getName())) {
                    document = JSON.readValue(zip.readAllBytes(), Document.class);
                } else if (entry.getName().startsWith("resources/") && !entry.isDirectory()) {
                    resources.put(entry.getName(), zip.readAllBytes());
                }
                zip.closeEntry();
            }
        }
        if (document == null) {
            throw new IOException("文件缺少 document.json，无法打开此演示文稿");
        }
        return new LoadedDocument(document, resources);
    }

    public static void write(Document document, Map<String, byte[]> resources, Path file) throws IOException {
        if (document == null) throw new IOException("没有可保存的文档");
        byte[] json = JSON.writerWithDefaultPrettyPrinter().writeValueAsBytes(document);
        Path target = file.toAbsolutePath();
        Path temporary = Files.createTempFile(target.getParent(), ".myppt-", ".tmp");
        try {
            // Complete the archive before replacing the previous document.
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(temporary))) {
                zip.putNextEntry(new ZipEntry("document.json"));
                zip.write(json);
                zip.closeEntry();
                zip.putNextEntry(new ZipEntry("resources/"));
                zip.closeEntry();
                for (var resource : resources.entrySet()) {
                    zip.putNextEntry(new ZipEntry(resource.getKey()));
                    zip.write(resource.getValue());
                    zip.closeEntry();
                }
            }
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
