package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
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

@Mixin(HorseInventoryScreen.class)
public abstract class HorseInventoryScreenMixin extends AbstractContainerScreen<HorseInventoryMenu> {

    @Shadow @Final private AbstractHorse horse;
    @Shadow private float xMouse;
    @Shadow private float yMouse;

    @Unique private static final Identifier BH_SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/slot");
    @Unique private static final Identifier BH_SADDLE_SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/horse/saddle_slot");
    @Unique private static final Identifier BH_LLAMA_ARMOR_SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/horse/llama_armor_slot");
    @Unique private static final Identifier BH_ARMOR_SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/horse/armor_slot");
    @Unique private static final Identifier BH_HORSE_TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/horse.png");

    @Unique private static final int BH_VANILLA_IMAGE_HEIGHT = 166;
    @Unique private static final int BH_TOP_SECTION_HEIGHT = 77;
    @Unique private static final int BH_PLAYER_SECTION_Y_OFFSET = 54;
    @Unique private static final int BH_EXTENDED_IMAGE_HEIGHT = BH_VANILLA_IMAGE_HEIGHT + BH_PLAYER_SECTION_Y_OFFSET;
    @Unique private static final int BH_DEFAULT_INVENTORY_LABEL_Y = BH_VANILLA_IMAGE_HEIGHT - 94;
    @Unique private static final int BH_GEAR_PANEL_X = 79;
    @Unique private static final int BH_GEAR_PANEL_Y = 17;
    @Unique private static final int BH_CHEST_PANEL_X = 7;
    @Unique private static final int BH_CHEST_PANEL_Y = 78;
    @Unique private static final int BH_CHEST_PANEL_WIDTH = 9 * 18;
    @Unique private static final int BH_CHEST_PANEL_HEIGHT = 3 * 18;
    @Unique private static final int BH_SIDE_BORDER_WIDTH = 7;
    @Unique private static final int BH_HINT_TINT = 0xA06B5A46;
    @Unique private static final int BH_MIDDLE_FILL = 0xFFC6C6C6;
    @Unique private static final int BH_MIDDLE_HIGHLIGHT = 0xFFF7F7F7;
    @Unique private static final int BH_MIDDLE_SHADOW = 0xFF8B8B8B;
    @Unique private static final float BH_STATS_TEXT_SCALE = 0.8F;
    @Unique private static final int BH_STATS_TEXT_X_OFFSET = 2;

    // Pseudo-constructor required for compilation — never actually called at runtime
    protected HorseInventoryScreenMixin(HorseInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_configureInitialLayout(
            HorseInventoryMenu menu, Inventory inventory, AbstractHorse horse, int chestColumns,
            CallbackInfo ci) {
        HorseInventoryLayoutAccess layoutAccess = (HorseInventoryLayoutAccess) menu;
        this.imageHeight = layoutAccess.bh_hasChestStorageLayout() ? BH_EXTENDED_IMAGE_HEIGHT : BH_VANILLA_IMAGE_HEIGHT;
        this.inventoryLabelY = layoutAccess.bh_hasUpgradedSaddleLayout()
                ? this.imageHeight + 1000
                : BH_DEFAULT_INVENTORY_LABEL_Y;
    }

    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
    private void bh_renderChestLayout(GuiGraphics gfx, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        this.bh_applyLayoutState();
        if (!this.bh_hasChestStorageLayout()) {
            return;
        }

        int x = this.leftPos;
        int y = this.topPos;
        bh_blitGui(gfx, BH_HORSE_TEXTURE, x, y, 0, 0, this.imageWidth, BH_TOP_SECTION_HEIGHT);
        this.bh_drawMiddlePanel(gfx, x, y + BH_TOP_SECTION_HEIGHT, this.imageWidth, BH_PLAYER_SECTION_Y_OFFSET);
        bh_blitGui(gfx, BH_HORSE_TEXTURE,
                x,
                y + BH_TOP_SECTION_HEIGHT + BH_PLAYER_SECTION_Y_OFFSET,
                0,
                BH_TOP_SECTION_HEIGHT,
                this.imageWidth,
                BH_VANILLA_IMAGE_HEIGHT - BH_TOP_SECTION_HEIGHT);

        if (this.horse.canUseSlot(EquipmentSlot.SADDLE)) {
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SADDLE_SLOT_SPRITE, x + 7, y + 17, 18, 18);
        }

        if (this.horse.canUseSlot(EquipmentSlot.BODY)) {
            Identifier armorSlotSprite =
                    this.horse instanceof Llama ? BH_LLAMA_ARMOR_SLOT_SPRITE : BH_ARMOR_SLOT_SPRITE;
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, armorSlotSprite, x + 7, y + 35, 18, 18);
        }

        this.bh_drawGearPanel(gfx);
        this.bh_drawStatsPanel(gfx);
        this.bh_drawChestPanel(gfx);
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                gfx,
                x + 26,
                y + 18,
                x + 78,
                y + 70,
                17,
                0.25F,
                this.xMouse,
                this.yMouse,
                this.horse);
        this.bh_drawBondLabel(gfx);
        ci.cancel();
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void bh_renderGearOnlyOverlay(GuiGraphics gfx, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (!this.bh_hasUpgradedSaddleInMenu() || this.bh_hasChestStorageLayout()) {
            return;
        }

        this.bh_drawGearPanel(gfx);
        this.bh_drawStatsPanel(gfx);
        this.bh_drawBondLabel(gfx);
    }

    @Unique
    private void bh_applyLayoutState() {
        HorseInventoryLayoutAccess layoutAccess = (HorseInventoryLayoutAccess) this.menu;
        layoutAccess.bh_refreshLayout();

        boolean chestLayout = layoutAccess.bh_hasChestStorageLayout();
        boolean upgradedSaddleLayout = layoutAccess.bh_hasUpgradedSaddleLayout();
        int desiredImageHeight = chestLayout ? BH_EXTENDED_IMAGE_HEIGHT : BH_VANILLA_IMAGE_HEIGHT;

        if (this.imageHeight != desiredImageHeight) {
            this.imageHeight = desiredImageHeight;
            this.topPos = (this.height - this.imageHeight) / 2;
            this.leftPos = (this.width - this.imageWidth) / 2;
        }

        this.inventoryLabelY = upgradedSaddleLayout ? this.imageHeight + 1000 : BH_DEFAULT_INVENTORY_LABEL_Y;
    }

    @Unique
    private void bh_drawBondLabel(GuiGraphics gfx) {
        IHorseData data = (IHorseData) this.horse;
        String text = "Bond: " + data.bh_getBond();
        int textWidth = this.font.width(text);
        // renderBg is not matrix-translated, so use absolute screen coords.
        gfx.drawString(this.font, text, this.leftPos + this.imageWidth - textWidth - 7, this.topPos + 6, 0x404040, false);
    }

    @Unique
    private void bh_drawStatsPanel(GuiGraphics gfx) {
        double speedAttr = this.horse.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double jumpAttr = this.horse.getAttributeValue(Attributes.JUMP_STRENGTH);

        // Approximate conversions so the numbers feel intuitive in-game.
        // Horse base speed 0.225 * 43.2 ~= 9.7 blk/s (matches vanilla roughly).
        double speedBps = speedAttr * 43.2;
        // Base horse jump 0.7 gives ~3.2 block height; linear fit within vanilla jump range.
        double jumpHeight = Math.max(0.0, jumpAttr * 6.0 - 1.0);

        int x = this.leftPos + BH_GEAR_PANEL_X + BH_STATS_TEXT_X_OFFSET;
        // Gear slots occupy y=17..34, so draw stats at y=37 and y=47 (2 rows, 10px spacing).
        int y = this.topPos + BH_GEAR_PANEL_Y + 20;
        int lineSpacing = Math.round(10.0F / BH_STATS_TEXT_SCALE);

        String speedText = String.format("Speed: %.1f blk/s", speedBps);
        String jumpText = String.format("Jump:  %.1f blk", jumpHeight);

        // 1.21.11: gfx.pose() returns a Matrix3x2fStack (joml 2D), so push/pop are matrix-named
        // and translate/scale take 2 floats, not 3.
        gfx.pose().pushMatrix();
        gfx.pose().translate((float) x, (float) y);
        gfx.pose().scale(BH_STATS_TEXT_SCALE, BH_STATS_TEXT_SCALE);
        gfx.drawString(this.font, speedText, 0, 0, 0x404040, false);
        gfx.drawString(this.font, jumpText, 0, lineSpacing, 0x404040, false);
        gfx.pose().popMatrix();
    }

    @Unique
    private void bh_drawGearPanel(GuiGraphics gfx) {
        int x = this.leftPos + BH_GEAR_PANEL_X;
        int y = this.topPos + BH_GEAR_PANEL_Y;
        for (int i = 0; i < GearSlot.COUNT; i++) {
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SLOT_SPRITE, x + i * 18, y, 18, 18);
        }
        if (!this.bh_hasUpgradedSaddleInMenu()) {
            return;
        }
        this.bh_drawGearHint(gfx, x, y, GearSlot.CHEST, Items.CHEST);
        this.bh_drawGearHint(gfx, x, y, GearSlot.HOOVES, ModItems.HORSE_HOOVES);
        this.bh_drawGearHint(gfx, x, y, GearSlot.MEDKIT, ModItems.HORSE_MEDKIT);
        this.bh_drawGearHint(gfx, x, y, GearSlot.STABILIZER, ModItems.HORSE_STABILIZER);
        this.bh_drawGearHint(gfx, x, y, GearSlot.HITCHPOST, ModItems.HITCHPOST);
    }

    @Unique
    private void bh_drawChestPanel(GuiGraphics gfx) {
        if (!this.bh_hasUpgradedSaddleInMenu() || !this.bh_hasChestGearInMenu()) {
            return;
        }
        int x = this.leftPos + BH_CHEST_PANEL_X;
        int y = this.topPos + BH_CHEST_PANEL_Y;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SLOT_SPRITE, x + col * 18, y + row * 18, 18, 18);
            }
        }
    }

    @Unique
    private void bh_drawMiddlePanel(GuiGraphics gfx, int x, int y, int width, int height) {
        int innerLeft = x + BH_SIDE_BORDER_WIDTH;
        int innerRight = x + width - BH_SIDE_BORDER_WIDTH;

        bh_blitGui(gfx, BH_HORSE_TEXTURE, x, y, 0, BH_TOP_SECTION_HEIGHT, BH_SIDE_BORDER_WIDTH, height);
        bh_blitGui(gfx, BH_HORSE_TEXTURE, innerRight, y,
                this.imageWidth - BH_SIDE_BORDER_WIDTH, BH_TOP_SECTION_HEIGHT, BH_SIDE_BORDER_WIDTH, height);

        gfx.fill(innerLeft, y, innerRight, y + height, BH_MIDDLE_FILL);
        gfx.fill(innerLeft, y, innerRight, y + 1, BH_MIDDLE_HIGHLIGHT);
        gfx.fill(innerLeft, y + height - 1, innerRight, y + height, BH_MIDDLE_SHADOW);
    }

    /**
     * 1.21.11 removed {@code gfx.setColor} (the old way to tint a {@code renderItem} call).
     * To replicate the "ghost" feel of the old hint icons, we draw the item normally and then
     * lay a translucent overlay rectangle on top of it: a milky-white wash dims & desaturates
     * the icon so it reads as a placeholder rather than a real equipped item.
     */
    @Unique
    private void bh_drawGearHint(GuiGraphics gfx, int x, int y, GearSlot slot, Item item) {
        int slotIndex = this.bh_getGearSlotIndex(slot.ordinal());
        if (slotIndex < 0 || this.menu.getSlot(slotIndex).hasItem()) {
            return;
        }

        int iconX = x + slot.ordinal() * 18 + 1;
        int iconY = y + 1;
        gfx.renderItem(new ItemStack(item), iconX, iconY);
        // Translucent wash. ARGB: alpha 0xA0 (~63%), warm-grey RGB matching old BH_HINT_TINT.
        gfx.fill(iconX, iconY, iconX + 16, iconY + 16, 0xA0B7AB99);
    }

    /**
     * Helper for the new 1.21.11 {@code blit} signature, which now requires an explicit
     * {@link com.mojang.blaze3d.pipeline.RenderPipeline} and texture sheet dimensions.
     * The vanilla horse GUI texture is the standard 256×256 sheet.
     */
    @Unique
    private static void bh_blitGui(GuiGraphics gfx, Identifier texture,
                                   int x, int y, int u, int v, int width, int height) {
        gfx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, 256, 256);
    }

    @Unique
    private boolean bh_hasUpgradedSaddleInMenu() {
        return this.menu.getSlot(0).getItem().is(ModItems.UPGRADED_SADDLE);
    }

    @Unique
    private boolean bh_hasChestGearInMenu() {
        int chestSlotIndex = this.bh_getGearSlotIndex(GearSlot.CHEST.ordinal());
        if (chestSlotIndex < 0) {
            return false;
        }

        ItemStack chestStack = this.menu.getSlot(chestSlotIndex).getItem();
        return chestStack.is(Items.CHEST);
    }

    @Unique
    private int bh_getGearSlotIndex(int slotOffset) {
        int gearStartIndex = ((HorseInventoryLayoutAccess) this.menu).bh_getGearStartIndex();
        if (gearStartIndex < 0 || gearStartIndex + slotOffset >= this.menu.slots.size()) {
            return -1;
        }
        return gearStartIndex + slotOffset;
    }

    @Unique
    private boolean bh_hasChestStorageLayout() {
        return ((HorseInventoryLayoutAccess) this.menu).bh_hasChestStorageLayout();
    }
}
