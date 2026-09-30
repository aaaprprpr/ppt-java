package myppt;

import myppt.editor.EditorSession;
import myppt.io.DocumentIO;
import myppt.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EditorSessionTest {
    @TempDir Path directory;

    @Test void newDocumentsContainAPageOnDiskAndResetResources() throws Exception {
        EditorSession session = new EditorSession();
        Path first = directory.resolve("first.myppt");
        session.newDocument(first);
        assertEquals(1, DocumentIO.read(first).document().getPages().size());
        session.importImage(imageFile());
        session.markChanged();
        session.newDocument(directory.resolve("second.myppt"));
        assertTrue(session.getResources().isEmpty());
        assertFalse(session.isModified());
    }

    @Test void importingImagesNeverOverwritesExistingResourcesAndKeepsAspectRatio() throws Exception {
        EditorSession session = new EditorSession();
        session.newDocument(directory.resolve("images.myppt"));
        Path source = imageFile();
        PPTImage first = session.importImage(source);
        PPTImage second = session.importImage(source);
        assertNotEquals(first.getSrc(), second.getSrc());
        assertEquals(2, session.getResources().size());
        assertEquals(2, first.getWidth() / first.getHeight());
        session.getDocument().getPages().getFirst().getObjects().add(first);
        session.save(session.getCurrentFile());
        Files.delete(source);
        session.open(session.getCurrentFile());
        assertNotNull(session.getResources().get(first.getSrc()));
        assertInstanceOf(PPTImage.class, session.getDocument().getPages().getFirst().getObjects().getFirst());
    }

    @Test void failedOpenKeepsTheCurrentDocumentAndItsResources() throws Exception {
        EditorSession session = new EditorSession();
        session.newDocument(directory.resolve("current.myppt"));
        PPTImage image = session.importImage(imageFile());
        session.markChanged();
        Document original = session.getDocument();
        Path invalid = directory.resolve("broken.myppt");
        Files.writeString(invalid, "not a presentation");
        assertThrows(java.io.IOException.class, () -> session.open(invalid));
        assertSame(original, session.getDocument());
        assertNotNull(session.getResources().get(image.getSrc()));
        assertTrue(session.isModified());
    }

    @Test void repairsLegacyNullPageListsAndMaintainsPageOrder() throws Exception {
        Document legacy = new Document();
        legacy.setPages(null);
        Path file = directory.resolve("empty.myppt");
        DocumentIO.write(legacy, Map.of(), file);
        EditorSession session = new EditorSession();
        session.open(file);
        assertEquals(1, session.getDocument().getPages().size());
        Page original = session.getDocument().getPages().getFirst();
        Page added = session.addPage(1);
        session.movePage(1, 0);
        assertSame(added, session.getDocument().getPages().getFirst());
        assertEquals(0, added.getId());
        assertEquals(1, original.getId());
        assertTrue(session.deletePage(0));
        assertSame(original, session.getDocument().getPages().getFirst());
        assertEquals(0, original.getId());
        assertFalse(session.deletePage(0));
    }

    private Path imageFile() throws Exception {
        Path image = directory.resolve("source.png");
        ImageIO.write(new BufferedImage(80, 40, BufferedImage.TYPE_INT_RGB), "png", image.toFile());
        return image;
    }
}
