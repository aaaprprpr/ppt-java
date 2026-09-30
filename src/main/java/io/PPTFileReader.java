package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Document;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 负责从 .myppt 文件读取Document对象与资源
 */
public class PPTFileReader {

    private static final ObjectMapper mapper = new ObjectMapper();// JSON映射器

    /**
     * 读取myppt文件
     */
    public static PPTReadResult read(File file) throws Exception {

        Document document = null;
        Map<String, byte[]> resources = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // 解析 document.json
                if (name.equals("document.json")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    zis.transferTo(baos);
                    String json = baos.toString();
                    document = mapper.readValue(json, Document.class);
                }

                // 解析资源文件
                else if (name.startsWith("resources/") && !entry.isDirectory()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    zis.transferTo(baos);
                    resources.put(name, baos.toByteArray());
                }
                zis.closeEntry();
            }
        }


        return new PPTReadResult(document, resources);
    }

    /**
     * 包装读取结果（文档 + 资源）
     */
    public static class PPTReadResult {
        public final Document document;
        public final Map<String, byte[]> resources;
        public PPTReadResult(Document document, Map<String, byte[]> resources) {
            this.document = document;
            this.resources = resources;
        }
    }
}
