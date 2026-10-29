package icy.betterhorses.net.client;

import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;

public final class JadeTooltipBox {

    private static final int GAP = 4;

    @Nullable private static Rect2i shown;

    private JadeTooltipBox() {}

    public static void clear() {
        shown = null;
    }

    public static void shown(Rect2i rect) {
        shown = rect.getWidth() > 0 && rect.getHeight() > 0
                ? new Rect2i(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight())
                : null;
    }

    public static int clearTop(int left, int top, int width, int height) {
        Rect2i box = shown;
        if (box == null) return top;
        boolean across = left < box.getX() + box.getWidth() && box.getX() < left + width;
        boolean down = top < box.getY() + box.getHeight() && box.getY() < top + height;
        return across && down ? box.getY() + box.getHeight() + GAP : top;
    }
}
