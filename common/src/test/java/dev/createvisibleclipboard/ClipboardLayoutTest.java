package dev.createvisibleclipboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(ClipboardLayout.rowHeight(0, false) > ClipboardLayout.CHECKBOX_SIZE);
        assertTrue(ClipboardLayout.rowHeight(1, true) >= 18);
        assertTrue(ClipboardLayout.rowHeight(3, false) > 2 * ClipboardLayout.LINE_HEIGHT + ClipboardLayout.TEXT_SIZE);
        assertTrue(ClipboardLayout.rowHeight(3, false) >= ClipboardLayout.rowHeight(1, false));
        assertEquals(1, ClipboardLayout.contentScale(0));
        assertEquals(1, ClipboardLayout.contentScale(ClipboardLayout.CONTENT_HEIGHT));
        int overflowingHeight = ClipboardLayout.CONTENT_HEIGHT * 2;
        assertEquals(ClipboardLayout.CONTENT_HEIGHT, overflowingHeight * ClipboardLayout.contentScale(overflowingHeight), 0.001);
    }

    @Test
    void pageButtonsDoNotCaptureTextOrThePageNumber() {
        double arrowCenter = ClipboardLayout.ARROW_SIZE / 2.0;
        double y = ClipboardLayout.ARROW_Y + arrowCenter;
        assertEquals(-1, ClipboardLayout.pageDirection(arrowCenter, y));
        assertEquals(1, ClipboardLayout.pageDirection(ClipboardLayout.WIDTH - arrowCenter, y));
        assertEquals(0, ClipboardLayout.pageDirection(ClipboardLayout.WIDTH / 2.0, y));
        assertEquals(0, ClipboardLayout.pageDirection(arrowCenter, ClipboardLayout.CONTENT_HEIGHT - 1));
        assertEquals(0, ClipboardLayout.pageDirection(-1, y));
        assertEquals(0, ClipboardLayout.pageDirection(ClipboardLayout.WIDTH, y));
        assertEquals(0, ClipboardLayout.pageDirection(arrowCenter, ClipboardLayout.ARROW_Y + ClipboardLayout.ARROW_SIZE));
    }

    @Test
    void checkboxDoesNotCaptureTheTextOrTheRestOfAWrappedEntry() {
        int rowY = 20;
        double center = ClipboardLayout.CHECKBOX_SIZE / 2.0;
        double centerX = ClipboardLayout.CHECKBOX_LEFT_OFFSET + center;
        assertTrue(ClipboardLayout.checkboxContains(centerX, rowY + center, rowY));
        assertTrue(ClipboardLayout.checkboxContains(ClipboardLayout.CHECKBOX_LEFT_OFFSET, rowY, rowY));
        assertFalse(ClipboardLayout.checkboxContains(ClipboardLayout.TEXT_X, rowY + center, rowY));
        assertFalse(ClipboardLayout.checkboxContains(ClipboardLayout.CHECKBOX_LEFT_OFFSET + ClipboardLayout.CHECKBOX_SIZE, rowY, rowY));
        assertFalse(ClipboardLayout.checkboxContains(centerX, rowY + ClipboardLayout.CHECKBOX_SIZE, rowY));
        assertFalse(ClipboardLayout.checkboxContains(ClipboardLayout.CHECKBOX_LEFT_OFFSET - 1, rowY, rowY));
        assertFalse(ClipboardLayout.checkboxContains(centerX, rowY - 1, rowY));
    }

    @Test
    void scaledTextFitsBesideTheCheckboxAndOptionalIcon() {
        for (int iconWidth : new int[] {0, 18}) {
            int wrapWidth = ClipboardLayout.textWidth(iconWidth);
            float available = ClipboardLayout.WIDTH - ClipboardLayout.TEXT_X - iconWidth;
            assertTrue(wrapWidth * ClipboardLayout.TEXT_SCALE <= available);
            assertTrue((wrapWidth + 1) * ClipboardLayout.TEXT_SCALE > available);
        }
        assertTrue(ClipboardLayout.CONTENT_HEIGHT < ClipboardLayout.FOOTER_Y);
        assertTrue(ClipboardLayout.ARROW_Y + ClipboardLayout.ARROW_SIZE <= ClipboardLayout.HEIGHT);
    }
}
