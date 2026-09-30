package myppt;

import myppt.io.DocumentIO;
import myppt.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.zip.*;

import static org.junit.jupiter.api.Assertions.*;

class DocumentIOTest {
    @TempDir Path directory;

    @Test void readsLegacyDocumentsAndRoundTripsEveryObjectType() throws Exception {
        // Old @Data models serialized "type" twice and included these unused fields.
        String legacy = """
                {"title":"中文文档","pages":[{"id":4,"dirty":true,"backgroundColor":"#abcdef","objects":[
                  {"type":"text","type":"text","text":"第一行\\n第二行","fontSize":24,"fontFamily":"SimSun","x":10,"y":20,"rotation":0,"zindex":0},
                  {"type":"rect","fillEnabled":true,"fillColor":"#ff0000","width":100,"height":80},
                  {"type":"circle","strokeColor":"#00ff00","width":50,"height":50},
                  {"type":"line","x":100,"y":20,"x2":10,"y2":200},
                  {"type":"image","src":"resources/image.png","imagePath":"C:/old/image.png","width":80,"height":40}
                ]}]}
                """;
        Path file = directory.resolve("legacy.myppt");
        try (var zip = new ZipOutputStream(Files.newOutputStream(file))) {
            zip.putNextEntry(new ZipEntry("document.json"));
            zip.write(legacy.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("resources/image.png"));
            zip.write(new byte[]{1, 2, 3});
            zip.closeEntry();
        }
        var read = DocumentIO.read(file);
        assertEquals("中文文档", read.document().getTitle());
        var objects = read.document().getPages().getFirst().getObjects();
        assertEquals(5, objects.size());
        assertEquals("第一行\n第二行", ((PPTText) objects.get(0)).getText());
        assertTrue(((PPTRect) objects.get(1)).isFillEnabled());
        assertInstanceOf(PPTCircle.class, objects.get(2));
        assertEquals(10, ((PPTLine) objects.get(3)).getX2());
        assertInstanceOf(PPTImage.class, objects.get(4));
        Path copy = directory.resolve("copy.myppt");
        DocumentIO.write(read.document(), read.resources(), copy);
        var roundTrip = DocumentIO.read(copy);
        assertArrayEquals(new byte[]{1, 2, 3}, roundTrip.resources().get("resources/image.png"));
        assertEquals("#abcdef", roundTrip.document().getPages().getFirst().getBackgroundColor());
        assertEquals(5, roundTrip.document().getPages().getFirst().getObjects().size());
    }

    @Test void failedSaveDoesNotOverwriteExistingDocument() throws Exception {
        Path file = directory.resolve("document.myppt");
        Document document = new Document();
        document.getPages().add(new Page());
        DocumentIO.write(document, Map.of(), file);
        byte[] original = Files.readAllBytes(file);
        // A duplicate entry makes ZipOutputStream fail after writing the new JSON.
        assertThrows(java.io.IOException.class, () ->
                DocumentIO.write(document, Map.of("document.json", new byte[]{1}), file));
        assertArrayEquals(original, Files.readAllBytes(file));
        try (var children = Files.list(directory)) {
            assertEquals(1, children.count(), "Failed saves must also remove the temporary file");
        }
    }

    @Test void rejectsArchivesWithoutADocument() throws Exception {
        Path file = directory.resolve("invalid.myppt");
        try (var zip = new ZipOutputStream(Files.newOutputStream(file))) {
            zip.putNextEntry(new ZipEntry("resources/"));
            zip.closeEntry();
        }
        assertThrows(java.io.IOException.class, () -> DocumentIO.read(file));
    }
}
