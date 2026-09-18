package icy.betterhorses.net.client.book;

import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.client.book.page.PageEntity;

import java.util.function.UnaryOperator;

public class BhCartModelsComponent implements ICustomComponent {

    private static final int BOX_WIDTH = 104;
    private static final int MODEL_Y = 84;
    private static final int ARROW_Y = 96;
    private static final int ARROW_W = 10;
    private static final int ARROW_H = 12;
    private static final int NAME_Y = 112;
    private static final int COUNT_Y = 124;
    private static final int INK = 0xFF3A2B1B;
    private static final float SMALL_SCALE = 16.0F;
    private static final float LARGE_SCALE = 12.0F;

    private transient int x;
    private transient int y;
    private transient int index;
    private transient HorseCartEntity cart;
    private transient CartSize built;

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
    }

    @Override
    public void build(int x, int y, int pageNum) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void onDisplayed(IComponentRenderContext context) {
        rebuild();
    }

    private void rebuild() {
        CartSize want = CartSize.values()[this.index];
        if (this.cart != null && this.built == want) {
            return;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        this.cart = HorseCartEntity.preview(level, want);
        this.built = want;
    }

    @Override
    public void render(GuiGraphics gfx, IComponentRenderContext context,
                       float partialTicks, int mouseX, int mouseY) {
        rebuild();
        var font = Minecraft.getInstance().font;
        if (this.cart == null) {
            return;
        }

        CartSize size = CartSize.values()[this.index];
        PageEntity.renderEntity(gfx, this.cart,
                this.x + BOX_WIDTH / 2.0F, this.y + MODEL_Y, rotation(),
                size.isLarge() ? LARGE_SCALE : SMALL_SCALE, 0.0F);

        arrow(gfx, font, "<", this.x, mouseX, mouseY);
        arrow(gfx, font, ">", this.x + BOX_WIDTH - ARROW_W, mouseX, mouseY);

        Component name = Component.translatable(
                "book.icys-better-horses.carts." + (size.isLarge() ? "large" : "small"));
        gfx.drawString(font, name,
                this.x + (BOX_WIDTH - font.width(name)) / 2, this.y + NAME_Y, INK, false);

        String count = (this.index + 1) + " / " + CartSize.values().length;
        gfx.drawString(font, count,
                this.x + (BOX_WIDTH - font.width(count)) / 2, this.y + COUNT_Y, INK, false);
    }

    private float rotation() {
        return (Minecraft.getInstance().level.getGameTime() % 360L) * 0.5F;
    }

    private void arrow(GuiGraphics gfx, net.minecraft.client.gui.Font font,
                       String glyph, int left, int mouseX, int mouseY) {
        boolean lit = hovered(left, mouseX, mouseY);
        gfx.drawString(font, glyph,
                left + (ARROW_W - font.width(glyph)) / 2, this.y + ARROW_Y,
                lit ? 0xFF8A6A3A : INK, false);
    }

    private boolean hovered(int left, int mouseX, int mouseY) {
        return mouseX >= left && mouseX < left + ARROW_W
                && mouseY >= this.y + ARROW_Y && mouseY < this.y + ARROW_Y + ARROW_H;
    }

    @Override
    public boolean mouseClicked(IComponentRenderContext context, double mouseX, double mouseY, int button) {
        int total = CartSize.values().length;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (hovered(this.x, mx, my)) {
            this.index = Math.floorMod(this.index - 1, total);
            return true;
        }
        if (hovered(this.x + BOX_WIDTH - ARROW_W, mx, my)) {
            this.index = Math.floorMod(this.index + 1, total);
            return true;
        }
        return false;
    }
}
