package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Document;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 负责将Document对象写入 .myppt 文件
 */
public class PPTFileWriter {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 写入myppt文件
     */
    public static void write(Document document, Map<String, byte[]> resources, File outputFile) throws Exception {

        // 序列化为JSON
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(document);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {

            // 写入 document.json
            zos.putNextEntry(new ZipEntry("document.json"));
            zos.write(json.getBytes());
            zos.closeEntry();

            // 写入资源文件夹（即使空也创建）
            zos.putNextEntry(new ZipEntry("resources/"));
            zos.closeEntry();

            // 写入所有资源文件
            for (Map.Entry<String, byte[]> entry : resources.entrySet()) {

                String path = entry.getKey();
                byte[] data = entry.getValue();

                zos.putNextEntry(new ZipEntry(path));
                zos.write(data);
                zos.closeEntry();
            }
        }
    }
}
