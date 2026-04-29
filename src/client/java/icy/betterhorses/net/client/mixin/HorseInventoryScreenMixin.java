package icy.betterhorses.net.client.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(HorseInventoryScreen.class)
public abstract class HorseInventoryScreenMixin extends AbstractContainerScreen<HorseInventoryMenu> {

    @Shadow @Final private AbstractHorse horse;

    @Unique private static final ResourceLocation BH_SLOT_SPRITE =
            ResourceLocation.withDefaultNamespace("container/slot");
    @Unique private static final ResourceLocation BH_HORSE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/horse.png");

    @Unique private static final int BH_VANILLA_IMAGE_HEIGHT = 166;
    @Unique private static final int BH_TOP_SECTION_HEIGHT = 77;
    @Unique private static final int BH_CHEST_PANEL_HEIGHT = 54;
    @Unique private static final int BH_EXTENDED_IMAGE_HEIGHT = BH_VANILLA_IMAGE_HEIGHT + BH_CHEST_PANEL_HEIGHT;

    @Unique private static final int BH_GEAR_PANEL_X = 79;
    @Unique private static final int BH_GEAR_PANEL_Y = 17;
    @Unique private static final int BH_STATS_TEXT_X = 81;
    @Unique private static final int BH_STATS_TEXT_Y = 38;
    @Unique private static final int BH_STATS_LINE_SPACING = 10;

    @Unique private static final int BH_CHEST_PANEL_X = 7;
    @Unique private static final int BH_CHEST_PANEL_Y = 78;

    @Unique private static final int BH_PANEL_FILL = 0xFFC6C6C6;
    @Unique private static final int BH_HINT_TINT = 0xA06B5A46;
    @Unique private static final int BH_TEXT_COLOR = 0xFF404040;

    // Pseudo-constructor required for compilation — never actually called at runtime.
    protected HorseInventoryScreenMixin(HorseInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_initLayout(HorseInventoryMenu menu, Inventory inventory, AbstractHorse horse, int columns, CallbackInfo ci) {
        bh_applyImageHeight();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (bh_applyImageHeight()) {
            this.topPos = (this.height - this.imageHeight) / 2;
            this.leftPos = (this.width - this.imageWidth) / 2;
        }
    }

    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
    private void bh_renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        HorseInventoryLayoutAccess layout = (HorseInventoryLayoutAccess) this.menu;
        if (bh_applyImageHeight()) {
            this.topPos = (this.height - this.imageHeight) / 2;
            this.leftPos = (this.width - this.imageWidth) / 2;
        }

        boolean upgraded = layout.bh_hasUpgradedSaddleLayout();
        boolean chest = layout.bh_hasChestStorageLayout();

        // For non-upgraded horses, fall through to vanilla.
        if (!upgraded) {
            return;
        }

        int x = this.leftPos;
        int y = this.topPos;

        // Draw background: top section, optional middle chest panel, bottom (player inv) section.
        gfx.blit(RenderPipelines.GUI_TEXTURED, BH_HORSE_TEXTURE,
                x, y, 0.0F, 0.0F, this.imageWidth, BH_TOP_SECTION_HEIGHT, 256, 256);

        int bottomY = y + BH_TOP_SECTION_HEIGHT;
        if (chest) {
            bh_drawMiddlePanel(gfx, x, bottomY, this.imageWidth, BH_CHEST_PANEL_HEIGHT);
            bottomY += BH_CHEST_PANEL_HEIGHT;
        }
        gfx.blit(RenderPipelines.GUI_TEXTURED, BH_HORSE_TEXTURE,
                x, bottomY, 0.0F, (float) BH_TOP_SECTION_HEIGHT,
                this.imageWidth, BH_VANILLA_IMAGE_HEIGHT - BH_TOP_SECTION_HEIGHT, 256, 256);

        // Empty slot sprites for saddle + armor (vanilla draws these at runtime, not in horse.png).
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SLOT_SPRITE, x + 7, y + 17, 18, 18);
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SLOT_SPRITE, x + 7, y + 35, 18, 18);

        bh_drawGearPanel(gfx, x, y);
        if (chest) {
            bh_drawChestPanel(gfx, x, y);
        }

        // Render horse entity preview.
        net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse(
                gfx, x + 26, y + 18, x + 78, y + 70, 17, 0.25F,
                (float) mouseX, (float) mouseY, this.horse);

        ci.cancel();
    }

    // HorseInventoryScreen doesn't override renderLabels in 1.21.10, so we hook the end of render()
    // instead. TAIL fires after super.render and renderTooltip, so the text sits visibly on top.
    @Inject(method = "render", at = @At("TAIL"))
    private void bh_renderTextOverlay(GuiGraphics gfx, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        HorseInventoryLayoutAccess layout = (HorseInventoryLayoutAccess) this.menu;
        if (!layout.bh_hasUpgradedSaddleLayout()) {
            return;
        }
        bh_drawStatsLines(gfx, this.leftPos, this.topPos);
        bh_drawBondLabel(gfx, this.leftPos, this.topPos);
    }

    @Unique
    private boolean bh_applyImageHeight() {
        HorseInventoryLayoutAccess layout = (HorseInventoryLayoutAccess) this.menu;
        layout.bh_refreshLayout();

        boolean chest = layout.bh_hasChestStorageLayout();
        boolean upgraded = layout.bh_hasUpgradedSaddleLayout();

        int desiredHeight = chest ? BH_EXTENDED_IMAGE_HEIGHT : BH_VANILLA_IMAGE_HEIGHT;
        boolean changed = this.imageHeight != desiredHeight;
        this.imageHeight = desiredHeight;
        // Hide vanilla "Inventory" label whenever we own the layout.
        this.inventoryLabelY = upgraded ? this.imageHeight + 1000 : BH_VANILLA_IMAGE_HEIGHT - 94;
        return changed;
    }

    @Unique
    private void bh_drawGearPanel(GuiGraphics gfx, int left, int top) {
        int x = left + BH_GEAR_PANEL_X;
        int y = top + BH_GEAR_PANEL_Y;
        for (int i = 0; i < GearSlot.COUNT; i++) {
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SLOT_SPRITE, x + i * 18, y, 18, 18);
        }
        bh_drawGearHint(gfx, x, y, GearSlot.CHEST, Items.CHEST);
        bh_drawGearHint(gfx, x, y, GearSlot.HOOVES, ModItems.HORSE_HOOVES);
        bh_drawGearHint(gfx, x, y, GearSlot.MEDKIT, ModItems.HORSE_MEDKIT);
        bh_drawGearHint(gfx, x, y, GearSlot.STABILIZER, ModItems.HORSE_STABILIZER);
        bh_drawGearHint(gfx, x, y, GearSlot.HITCHPOST, ModItems.HITCHPOST);
    }

    @Unique
    private void bh_drawGearHint(GuiGraphics gfx, int panelX, int panelY, GearSlot slot, Item item) {
        int slotIndex = bh_gearSlotIndex(slot);
        if (slotIndex < 0 || this.menu.getSlot(slotIndex).hasItem()) {
            return;
        }
        int x = panelX + slot.ordinal() * 18 + 1;
        int y = panelY + 1;
        gfx.renderItem(new ItemStack(item), x, y);
        gfx.fill(x, y, x + 16, y + 16, BH_HINT_TINT);
    }

    @Unique
    private void bh_drawChestPanel(GuiGraphics gfx, int left, int top) {
        int x = left + BH_CHEST_PANEL_X + 1;
        int y = top + BH_CHEST_PANEL_Y + 1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SLOT_SPRITE, x + col * 18, y + row * 18, 18, 18);
            }
        }
    }

    @Unique
    private void bh_drawStatsLines(GuiGraphics gfx, int left, int top) {
        double speedBps = this.horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 43.2D;
        double jumpBlk = Math.max(0.0D, this.horse.getAttributeValue(Attributes.JUMP_STRENGTH) * 6.0D - 1.0D);
        String speedText = String.format(Locale.ROOT, "Speed: %.1f blk/s", speedBps);
        String jumpText = String.format(Locale.ROOT, "Jump:  %.1f blk", jumpBlk);
        gfx.drawString(this.font, speedText, left + BH_STATS_TEXT_X, top + BH_STATS_TEXT_Y, BH_TEXT_COLOR, false);
        gfx.drawString(this.font, jumpText, left + BH_STATS_TEXT_X, top + BH_STATS_TEXT_Y + BH_STATS_LINE_SPACING, BH_TEXT_COLOR, false);
    }

    @Unique
    private void bh_drawBondLabel(GuiGraphics gfx, int left, int top) {
        String text = "Bond: " + ((IHorseData) this.horse).bh_getBond();
        int width = this.font.width(text);
        gfx.drawString(this.font, text, left + this.imageWidth - width - 8, top + 6, BH_TEXT_COLOR, false);
    }

    @Unique
    private void bh_drawMiddlePanel(GuiGraphics gfx, int x, int y, int width, int height) {
        final int border = 7;
        // Side bevels copied from horse.png upper section so the menu frame stays continuous.
        gfx.blit(RenderPipelines.GUI_TEXTURED, BH_HORSE_TEXTURE,
                x, y, 0.0F, 18.0F, border, height, 256, 256);
        gfx.blit(RenderPipelines.GUI_TEXTURED, BH_HORSE_TEXTURE,
                x + width - border, y, (float) (this.imageWidth - border), 18.0F, border, height, 256, 256);
        // Plain gray fill between the bevels.
        gfx.fill(x + border, y, x + width - border, y + height, BH_PANEL_FILL);
    }

    @Unique
    private int bh_gearSlotIndex(GearSlot slot) {
        int start = ((HorseInventoryLayoutAccess) this.menu).bh_getGearStartIndex();
        if (start < 0 || start + slot.ordinal() >= this.menu.slots.size()) {
            return -1;
        }
        return start + slot.ordinal();
    }
}
