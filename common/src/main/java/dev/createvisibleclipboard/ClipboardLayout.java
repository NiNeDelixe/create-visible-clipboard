package dev.createvisibleclipboard;

public class ClipboardLayout {
    // Offsets, sizes, and gaps are in page pixels; Minecraft's normal text height is 9.
    public static final int CHECKBOX_LEFT_OFFSET = 5;
    public static final int CHECKBOX_SIZE = 9;
    public static final int CHECKBOX_TEXT_GAP = 5;
    public static final int TEXT_SIZE = 9;
    public static final int ARROW_SIZE = 15;
    public static final int LINE_SPACING = 0;

    public static final int WIDTH = 164;
    public static final int HEIGHT = 204;
    public static final int TEXT_X = CHECKBOX_LEFT_OFFSET + CHECKBOX_SIZE + CHECKBOX_TEXT_GAP;
    public static final int TEXT_WIDTH = WIDTH - TEXT_X;
    public static final float TEXT_SCALE = TEXT_SIZE / 9f;
    public static final int LINE_HEIGHT = TEXT_SIZE + LINE_SPACING;
    public static final int FOOTER_HEIGHT = Math.max(ARROW_SIZE, TEXT_SIZE);
    public static final int FOOTER_Y = HEIGHT - FOOTER_HEIGHT - 2;
    public static final int ARROW_Y = FOOTER_Y + (FOOTER_HEIGHT - ARROW_SIZE) / 2;
    public static final int CONTENT_HEIGHT = FOOTER_Y - 5;

    public static int pageIndex(int savedPage, int pageCount) {
        return Math.max(0, Math.min(savedPage, pageCount - 1));
    }

    public static int rowHeight(int lines, boolean hasIcon) {
        return Math.max(Math.max(CHECKBOX_SIZE + 3, hasIcon ? 18 : 0), lines * LINE_HEIGHT + 3);
    }

    public static int textWidth(int iconWidth) {
        return Math.max(1, (int) ((TEXT_WIDTH - iconWidth) / TEXT_SCALE));
    }

    public static boolean checkboxContains(double x, double y, int rowY) {
        return x >= CHECKBOX_LEFT_OFFSET && x < CHECKBOX_LEFT_OFFSET + CHECKBOX_SIZE
            && y >= rowY && y < rowY + CHECKBOX_SIZE;
    }

    public static float contentScale(int height) {
        return height <= CONTENT_HEIGHT ? 1 : (float) CONTENT_HEIGHT / height;
    }

    public static int pageDirection(double x, double y) {
        if (y < ARROW_Y || y >= ARROW_Y + ARROW_SIZE)
            return 0;
        if (x >= 0 && x < ARROW_SIZE)
            return -1;
        if (x >= WIDTH - ARROW_SIZE && x < WIDTH)
            return 1;
        return 0;
    }
}
