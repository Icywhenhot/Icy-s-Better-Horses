package icy.betterhorses.net.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Flat-panel drawing helpers shared by the horse management and horse info screens. */
public final class BhScreenDraw {

    public static final int PANEL_BG = 0xE0101723;
    public static final int PANEL_BORDER = 0xFF2E3A52;
    public static final int ROW_BG = 0xFF172030;
    public static final int ROW_HOVER = 0xFF1F2B40;
    public static final int ROW_SELECTED = 0xFF2B3A54;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_MUTED = 0xFFB9C4D6;
    public static final int TEXT_DISABLED = 0xFF6C7789;
    /** Dark text for the amber "Active" state, which is too bright for white. */
    public static final int TEXT_ON_ACTIVE = 0xFF2A210A;

    public static final int BTN_WHISTLE = 0xFF2F6E8C;
    public static final int BTN_WHISTLE_HOVER = 0xFF3E8CB0;
    public static final int BTN_HOME = 0xFF3A7F5A;
    public static final int BTN_HOME_HOVER = 0xFF4FA374;
    public static final int BTN_DISOWN = 0xFF7A3A3A;
    public static final int BTN_DISOWN_HOVER = 0xFF994A4A;
    public static final int BTN_NEUTRAL = 0xFF3A4358;
    public static final int BTN_NEUTRAL_HOVER = 0xFF4A566F;
    public static final int BTN_GREY = 0xFF5A6270;
    public static final int BTN_GREY_HOVER = 0xFF717A8A;
    /** Amber, for the horse the whistle currently calls. */
    public static final int ACTIVE = 0xFFE7B43B;
    /** The colour a button flashes when the server refuses the action. */
    public static final int BTN_ERROR = 0xFFD24B4B;
    public static final int TEXT_ERROR = 0xFFFF8A8A;

    private BhScreenDraw() {}

    public static void panel(GuiGraphicsExtractor gfx, int x, int y, int width, int height) {
        gfx.fill(x - 1, y - 1, x + width + 1, y + height + 1, PANEL_BORDER);
        gfx.fill(x, y, x + width, y + height, PANEL_BG);
    }

    public static void button(GuiGraphicsExtractor gfx, Font font, int x, int y, int width, int height,
                              Component label, int color, int textColor) {
        gfx.fill(x, y, x + width, y + height, color);
        gfx.centeredText(font, label, x + width / 2, y + (height - font.lineHeight) / 2 + 1, textColor);
    }

    public static boolean inBox(double x, double y, int boxX, int boxY, int boxWidth, int boxHeight) {
        return x >= boxX && x < boxX + boxWidth && y >= boxY && y < boxY + boxHeight;
    }

    /** Turns {@code minecraft:the_nether} into "The Nether" so dimensions read nicely without extra lang keys. */
    public static String prettifyDimension(String dimensionId) {
        String path = dimensionId.contains(":") ? dimensionId.substring(dimensionId.indexOf(':') + 1) : dimensionId;
        if (path.isEmpty()) return "";

        StringBuilder pretty = new StringBuilder(path.length());
        boolean capitalise = true;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '_') {
                pretty.append(' ');
                capitalise = true;
            } else if (capitalise) {
                pretty.append(Character.toUpperCase(c));
                capitalise = false;
            } else {
                pretty.append(c);
            }
        }
        return pretty.toString();
    }
}
