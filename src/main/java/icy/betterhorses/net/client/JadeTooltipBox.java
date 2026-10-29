package icy.betterhorses.net.client;

public final class JadeTooltipBox {

    private static final int GAP = 4;
    private static final long FRESH_NANOS = 100_000_000L;

    private static int x;
    private static int y;
    private static int width;
    private static int height;
    private static long seenAt = Long.MIN_VALUE;

    private JadeTooltipBox() {}

    public static void shown(float x, float y, float width, float height) {
        JadeTooltipBox.x = (int) Math.floor(x);
        JadeTooltipBox.y = (int) Math.floor(y);
        JadeTooltipBox.width = (int) Math.ceil(width);
        JadeTooltipBox.height = (int) Math.ceil(height);
        seenAt = System.nanoTime();
    }

    public static int clearTop(int left, int top, int boxWidth, int boxHeight) {
        if (width <= 0 || height <= 0 || System.nanoTime() - seenAt > FRESH_NANOS) return top;
        boolean across = left < x + width && x < left + boxWidth;
        boolean down = top < y + height && y < top + boxHeight;
        return across && down ? y + height + GAP : top;
    }
}
