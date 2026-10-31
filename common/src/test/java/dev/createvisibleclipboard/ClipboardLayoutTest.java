package dev.createvisibleclipboard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClipboardLayoutTest {
    @Test
    void clampsSavedPagesIncludingEmptyClipboards() {
        assertEquals(0, ClipboardLayout.pageIndex(-10, 3));
        assertEquals(2, ClipboardLayout.pageIndex(10, 3));
        assertEquals(0, ClipboardLayout.pageIndex(10, 0));
    }

    @Test
    void fitsWrappedTextAndIconsInsideThePage() {
        assertEquals(12, ClipboardLayout.rowHeight(0, false));
        assertEquals(18, ClipboardLayout.rowHeight(1, true));
        assertEquals(30, ClipboardLayout.rowHeight(3, false));
        assertEquals(1, ClipboardLayout.contentScale(0));
        assertEquals(1, ClipboardLayout.contentScale(180));
        assertEquals(ClipboardLayout.CONTENT_HEIGHT, 400 * ClipboardLayout.contentScale(400), 0.001);
    }

    @Test
    void pageButtonsDoNotCaptureTextOrThePageNumber() {
        assertEquals(-1, ClipboardLayout.pageDirection(6, 195));
        assertEquals(1, ClipboardLayout.pageDirection(158, 195));
        assertEquals(0, ClipboardLayout.pageDirection(82, 195));
        assertEquals(0, ClipboardLayout.pageDirection(6, 180));
        assertEquals(0, ClipboardLayout.pageDirection(-1, 195));
        assertEquals(0, ClipboardLayout.pageDirection(165, 195));
        assertEquals(0, ClipboardLayout.pageDirection(6, 204));
    }
}
