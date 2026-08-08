package icy.betterhorses.net.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

// flat-panel drawing helpers shared by the horse management and horse info screens
public final class BhScreenDraw {

    public static final int PANEL_BG = 0xE0101723;
    public static final int PANEL_BORDER = 0xFF2E3A52;
    public static final int ROW_BG = 0xFF172030;
    public static final int ROW_HOVER = 0xFF1F2B40;
    public static final int ROW_SELECTED = 0xFF2B3A54;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_MUTED = 0xFFB9C4D6;
    public static final int TEXT_DISABLED = 0xFF6C7789;
    // dark text for the amber "Active" state, which is too bright for white
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
    // amber, for the horse the whistle currently calls
    public static final int ACTIVE = 0xFFE7B43B;
    // the colour a button flashes when the server refuses the action
    public static final int BTN_ERROR = 0xFFD24B4B;
    public static final int TEXT_ERROR = 0xFFFF8A8A;

    // single nine-sliced button texture, shared by every button at every size
    public static final Identifier BUTTON_SPRITE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "widget/button");
    // flip to true once button.png + its .mcmeta are in place; false keeps the flat-colour buttons
    private static final boolean TEXTURED_BUTTONS = false;

    // custom panel background textures (exact panel size; drawn 1:1, art includes its own border)
    public static final Identifier SCREEN_INFO_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/screen_info.png");
    public static final Identifier SCREEN_MANAGE_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/screen_manage.png");
    public static final Identifier SCREEN_CONFIRM_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/screen_confirm.png");
    // roster row plate (300×30)
    public static final Identifier ROW_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/row.png");
    // info screen's bespoke "Disown horse" button (110×24, light plank with a red x emblem)
    public static final Identifier DISOWN_BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/disown_button.png");
    // the same plank at confirm-panel size (84×20), used for the "yes, let go" button
    public static final Identifier DISOWN_BUTTON_SMALL_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/disown_button_small.png");
    // confirm panel's "keep him" button (84×20, dark slate plank), the safe counterpart to the red one
    public static final Identifier CANCEL_BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/cancel_button.png");
    // the roster row's disown button: a 15×17 red pennant
    public static final Identifier CROSS_BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/cross_button.png");
    // the roster row's two word buttons (50×14 and 62×14), sized to the row's existing button slots
    public static final Identifier WHISTLE_BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/whistle_button.png");
    public static final Identifier SEND_HOME_BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/send_home_button.png");
    // preview pane's "Set active" button in its two states (both 110×22): dark slate, and lit amber
    public static final Identifier SET_ACTIVE_BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/set_active_button.png");
    public static final Identifier ACTIVE_BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/active_button.png");

    private BhScreenDraw() {}

    public static void panel(GuiGraphicsExtractor gfx, int x, int y, int width, int height) {
        gfx.fill(x - 1, y - 1, x + width + 1, y + height + 1, PANEL_BORDER);
        gfx.fill(x, y, x + width, y + height, PANEL_BG);
    }

    // draws a panel-background texture at its exact panel size (the PNG is authored w×h, blitted 1:1)
    public static void panelTexture(GuiGraphicsExtractor gfx, int x, int y, int width, int height, Identifier texture) {
        gfx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, width, height);
    }

    // draws a fixed-size button texture (blitted 1:1) with a shadowless centred label
    public static void textureButton(GuiGraphicsExtractor gfx, Font font, Identifier texture,
                                     int x, int y, int width, int height, Component label, int textColor, int tint) {
        gfx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, width, height, tint);
        int textY = y + (height - font.lineHeight) / 2 + 1;
        gfx.text(font, label, x + width / 2 - font.width(label) / 2, textY, textColor, false);
    }

    // soft drop shadow for a fixed-size button texture: the button's own silhouette
    public static void textureShadow(GuiGraphicsExtractor gfx, Identifier texture,
                                     int x, int y, int width, int height, float spread, float alpha) {
        // furthest + faintest first; the stacked passes are what make the edge read soft
        shadowPass(gfx, texture, x + 1, y + Math.round(2f + spread * 1.5f), width, height, 0x22, alpha);
        shadowPass(gfx, texture, x, y + Math.round(2f + spread), width, height, 0x30, alpha);
        shadowPass(gfx, texture, x, y + Math.round(1f + spread * 0.5f), width, height, 0x44, alpha);
    }

    private static void shadowPass(GuiGraphicsExtractor gfx, Identifier texture, int x, int y,
                                   int width, int height, int baseAlpha, float alpha) {
        int a = Math.round(baseAlpha * BhAnim.clamp01(alpha));
        if (a <= 0) return;
        // tint multiplies the texture, so RGB 0 flattens the art to a pure black silhouette
        gfx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, width, height, a << 24);
    }

    // as panelTexture, but fades the whole plate by alpha (0..1) for entrance animations
    public static void panelTexture(GuiGraphicsExtractor gfx, int x, int y, int width, int height,
                                    Identifier texture, float alpha) {
        int a = Math.round(255f * BhAnim.clamp01(alpha));
        gfx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, width, height,
                (a << 24) | 0xFFFFFF);
    }

    // draws the roster row plate
    public static void rowPlate(GuiGraphicsExtractor gfx, int x, int rowTop, int rowWidth, int rowHeight) {
        int texHeight = rowHeight + 2;
        gfx.blit(RenderPipelines.GUI_TEXTURED, ROW_TEXTURE, x, rowTop - 1, 0.0F, 0.0F,
                rowWidth, texHeight, rowWidth, texHeight);
    }

    public static void button(GuiGraphicsExtractor gfx, Font font, int x, int y, int width, int height,
                              Component label, int color, int textColor) {
        button(gfx, font, x, y, width, height, label, color, textColor, true);
    }

    // shadow=false draws the label shadowless, used for dark text on a bright button (e.g
    public static void button(GuiGraphicsExtractor gfx, Font font, int x, int y, int width, int height,
                              Component label, int color, int textColor, boolean shadow) {
        if (TEXTURED_BUTTONS) {
            // nine-slice the one button sprite to this button's size
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_SPRITE, x, y, width, height, color);
        } else {
            gfx.fill(x, y, x + width, y + height, color);
        }
        int textY = y + (height - font.lineHeight) / 2 + 1;
        if (shadow) {
            gfx.centeredText(font, label, x + width / 2, textY, textColor);
        } else {
            gfx.text(font, label, x + width / 2 - font.width(label) / 2, textY, textColor, false);
        }
    }

    // translucent red wash marking a refused action on a dark plank
    public static void errorWash(GuiGraphicsExtractor gfx, int x, int y, int width, int height) {
        gfx.fill(x, y + 1, x + width, y + height - 1, ERROR_WASH);
        gfx.fill(x + 1, y, x + width - 1, y + 1, ERROR_WASH);
        gfx.fill(x + 1, y + height - 1, x + width - 1, y + height, ERROR_WASH);
    }

    private static final int ERROR_WASH = 0x99C43A3A;

    public static boolean inBox(double x, double y, int boxX, int boxY, int boxWidth, int boxHeight) {
        return x >= boxX && x < boxX + boxWidth && y >= boxY && y < boxY + boxHeight;
    }

    // turns minecraft:the_nether into "The nether" so dimensions read nicely without extra lang keys
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
