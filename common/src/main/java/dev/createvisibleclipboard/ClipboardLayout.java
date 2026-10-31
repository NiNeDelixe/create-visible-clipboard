package dev.createvisibleclipboard;

public class ClipboardLayout {
    public static final int WIDTH = 164;
    public static final int HEIGHT = 204;
    public static final int TEXT_X = 14;
    public static final int TEXT_WIDTH = 150;
    public static final int CONTENT_HEIGHT = 185;
    public static final int FOOTER_Y = 190;
    public static final int CONTROL_SIZE = 12;

    public static int pageIndex(int savedPage, int pageCount) {
        return Math.max(0, Math.min(savedPage, pageCount - 1));
    }

    public static int rowHeight(int lines, boolean hasIcon) {
        return Math.max(hasIcon ? 18 : 12, lines * 9 + 3);
    }

    public static float contentScale(int height) {
        return height <= CONTENT_HEIGHT ? 1 : (float) CONTENT_HEIGHT / height;
    }

    public static int pageDirection(double x, double y) {
        if (y < FOOTER_Y || y >= FOOTER_Y + CONTROL_SIZE)
            return 0;
        if (x >= 0 && x < CONTROL_SIZE)
            return -1;
        if (x >= WIDTH - CONTROL_SIZE && x < WIDTH)
            return 1;
        return 0;
    }
}
