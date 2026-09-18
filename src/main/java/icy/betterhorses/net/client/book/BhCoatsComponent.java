package icy.betterhorses.net.client.book;

import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.client.book.page.PageEntity;

import java.util.function.UnaryOperator;

public class BhCoatsComponent implements ICustomComponent {

    private static final int BOX_WIDTH = 104;
    private static final int MODEL_Y = 84;
    private static final int ARROW_Y = 96;
    private static final int ARROW_W = 10;
    private static final int ARROW_H = 12;
    private static final int NAME_Y = 112;
    private static final int COUNT_Y = 124;
    private static final int INK = 0xFF3A2B1B;
    private static final float MIN_SCALE = 18.0F;
    private static final float MAX_SCALE = 46.0F;
    private static final float MODEL_FILL = 0.7F;
    private static final float VISUAL_HEIGHT = 1.5F;

    public IVariable entity;

    private transient int x;
    private transient int y;
    private transient String id;
    private transient BhBreedHorse horse;
    private transient int coat;
    private transient boolean errored;

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        this.id = lookup.apply(this.entity).asString();
    }

    @Override
    public void build(int x, int y, int pageNum) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void onDisplayed(IComponentRenderContext context) {
        loadHorse();
    }

    private void loadHorse() {
        if (this.horse != null || this.errored) {
            return;
        }
        Level level = Minecraft.getInstance().level;
        ResourceLocation key = ResourceLocation.tryParse(this.id);
        if (level == null || key == null) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
        Entity made = type == null ? null : type.create(level);
        if (made instanceof BhBreedHorse bred) {
            bred.setId(-1);
            bred.bhSetCoat(this.coat);
            this.horse = bred;
        } else {
            this.errored = true;
        }
    }

    @Override
    public void render(GuiGraphics gfx, IComponentRenderContext context,
                       float partialTicks, int mouseX, int mouseY) {
        loadHorse();
        var font = Minecraft.getInstance().font;
        if (this.horse == null) {
            gfx.drawString(font, Component.translatable("book.icys-better-horses.coats.unavailable"),
                    this.x, this.y, INK, false);
            return;
        }

        this.horse.bhSetCoat(this.coat);
        float visual = Math.max(0.1F, this.horse.getBbHeight() * VISUAL_HEIGHT);
        float scale = Math.min(MODEL_Y * MODEL_FILL / visual,
                BOX_WIDTH * MODEL_FILL / Math.max(0.1F, this.horse.getBbWidth()));
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));

        PageEntity.renderEntity(gfx, this.horse,
                this.x + BOX_WIDTH / 2.0F, this.y + MODEL_Y, rotation(), scale, 0.0F);

        int total = this.horse.bhCoats().count();
        if (total > 1) {
            arrow(gfx, font, "<", this.x, mouseX, mouseY);
            arrow(gfx, font, ">", this.x + BOX_WIDTH - ARROW_W, mouseX, mouseY);
        }

        Component name = this.horse.bhCoats().displayName(this.coat);
        gfx.drawString(font, name,
                this.x + (BOX_WIDTH - font.width(name)) / 2, this.y + NAME_Y, INK, false);

        String count = (this.coat + 1) + " / " + total;
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
        if (this.horse == null) {
            return false;
        }
        int total = this.horse.bhCoats().count();
        if (total <= 1) {
            return false;
        }
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (hovered(this.x, mx, my)) {
            this.coat = Math.floorMod(this.coat - 1, total);
            return true;
        }
        if (hovered(this.x + BOX_WIDTH - ARROW_W, mx, my)) {
            this.coat = Math.floorMod(this.coat + 1, total);
            return true;
        }
        return false;
    }
}
