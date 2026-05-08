package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractMountInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMountInventoryScreen.class)
public abstract class HorseInventoryScreenMixin extends AbstractContainerScreen<AbstractMountInventoryMenu> {

    @Shadow protected LivingEntity mount;
    @Shadow private float xMouse;
    @Shadow private float yMouse;

    @Unique private static final Identifier BH_SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/slot");
    @Unique private static final Identifier BH_HORSE_TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/horse.png");

    @Unique private static final int BH_VANILLA_IMAGE_HEIGHT = 166;
    @Unique private static final int BH_TOP_SECTION_HEIGHT = 77;
    @Unique private static final int BH_PLAYER_SECTION_Y_OFFSET = 54;
    @Unique private static final int BH_PLAYER_SLOT_Y_OFFSET = 54;
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
    @Unique private static final int BH_STATS_TEXT_X = 81;
    @Unique private static final int BH_STATS_TEXT_Y = 38;
    @Unique private static final int BH_STATS_LINE_SPACING = 10;
    @Unique private static final int BH_TEXT_COLOR = 0xFF404040;

    // Pseudo-constructor required for compilation — never actually called at runtime
    protected HorseInventoryScreenMixin(AbstractMountInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_configureInitialLayout(
            AbstractMountInventoryMenu menu,
            Inventory inventory,
            Component title,
            int inventoryColumns,
            LivingEntity mount,
            CallbackInfo ci) {
        if (!(menu instanceof HorseInventoryLayoutAccess layoutAccess) || !(mount instanceof AbstractHorse)) {
            return;
        }
        this.inventoryLabelY = layoutAccess.bh_hasUpgradedSaddleLayout()
                ? this.imageHeight + 1000
                : BH_DEFAULT_INVENTORY_LABEL_Y;
    }

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void bh_renderChestLayout(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractHorse horse = this.bh_getHorseOrNull();
        if (horse == null) {
            return;
        }

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

        // 1.21.11 split horse slot rendering: vanilla draws an 18x18 "container/slot" sprite for
        // the slot frame, and AbstractContainerScreen.renderSlot overlays the 16x16 empty-icon
        // sprite (saddle / horse_armor / llama_armor) on top when the slot is empty. Replicate the
        // frame here; the empty-icon overlay is drawn automatically by vanilla's slot rendering.
        if (horse.canUseSlot(EquipmentSlot.SADDLE)) {
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SLOT_SPRITE, x + 7, y + 17, 18, 18);
        }

        if (horse.canUseSlot(EquipmentSlot.BODY)) {
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BH_SLOT_SPRITE, x + 7, y + 35, 18, 18);
        }

        this.bh_drawGearPanel(gfx);
        this.bh_drawChestPanel(gfx);
        InventoryScreen.extractEntityInInventoryFollowsMouse(
                gfx,
                x + 26,
                y + 18,
                x + 78,
                y + 70,
                17,
                0.25F,
                this.xMouse,
                this.yMouse,
                horse);
        ci.cancel();
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void bh_renderGearOnlyOverlay(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.bh_getHorseOrNull() == null) {
            return;
        }
        if (!this.bh_hasUpgradedSaddleInMenu() || this.bh_hasChestStorageLayout()) {
            return;
        }

        this.bh_drawGearPanel(gfx);
    }

    /**
     * Bond label and speed/jump stat lines are drawn at the very end of {@code render()} so they
     * sit on top of every layer (slots, hovered-slot highlight, vanilla labels) and aren't masked
     * by anything drawn after {@code renderBg}. The vanilla label-rendering matrix is already
     * popped by this point, so coordinates here are in absolute screen space.
     */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void bh_drawTextOverlay(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractHorse horse = this.bh_getHorseOrNull();
        if (horse == null || !this.bh_hasUpgradedSaddleInMenu()) {
            return;
        }
        this.bh_drawStatsLines(gfx, horse);
        this.bh_drawBondLabel(gfx, horse);
    }

    @Unique
    private void bh_applyLayoutState() {
        HorseInventoryLayoutAccess layoutAccess = this.bh_getLayoutAccessOrNull();
        if (layoutAccess == null || this.bh_getHorseOrNull() == null) {
            return;
        }
        layoutAccess.bh_refreshLayout();

        boolean chestLayout = layoutAccess.bh_hasChestStorageLayout();
        boolean upgradedSaddleLayout = layoutAccess.bh_hasUpgradedSaddleLayout();
        int desiredTopPos = (this.height - BH_VANILLA_IMAGE_HEIGHT) / 2
                - (chestLayout ? BH_PLAYER_SLOT_Y_OFFSET / 2 : 0);

        if (this.topPos != desiredTopPos) {
            this.topPos = desiredTopPos;
            this.leftPos = (this.width - this.imageWidth) / 2;
        }

        this.inventoryLabelY = upgradedSaddleLayout ? this.imageHeight + 1000 : BH_DEFAULT_INVENTORY_LABEL_Y;
    }

    @Unique
    private void bh_drawBondLabel(GuiGraphicsExtractor gfx, AbstractHorse horse) {
        String text = "Bond: " + ((IHorseData) horse).bh_getBond();
        int textWidth = this.font.width(text);
        gfx.text(this.font, text,
                this.leftPos + this.imageWidth - textWidth - 8,
                this.topPos + 6,
                BH_TEXT_COLOR);
    }

    @Unique
    private void bh_drawStatsLines(GuiGraphicsExtractor gfx, AbstractHorse horse) {
        // Horse base speed 0.225 * 43.2 ~= 9.7 blk/s (matches vanilla roughly).
        double speedBps = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 43.2D;
        // Base horse jump 0.7 yields ~3.2 block height; linear fit within vanilla jump range.
        double jumpBlk = Math.max(0.0D, horse.getAttributeValue(Attributes.JUMP_STRENGTH) * 6.0D - 1.0D);
        String speedText = String.format(java.util.Locale.ROOT, "Speed: %.1f blk/s", speedBps);
        String jumpText = String.format(java.util.Locale.ROOT, "Jump:  %.1f blk", jumpBlk);

        gfx.text(this.font, speedText,
                this.leftPos + BH_STATS_TEXT_X,
                this.topPos + BH_STATS_TEXT_Y,
                BH_TEXT_COLOR);
        gfx.text(this.font, jumpText,
                this.leftPos + BH_STATS_TEXT_X,
                this.topPos + BH_STATS_TEXT_Y + BH_STATS_LINE_SPACING,
                BH_TEXT_COLOR);
    }

    @Unique
    private void bh_drawGearPanel(GuiGraphicsExtractor gfx) {
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
    private void bh_drawChestPanel(GuiGraphicsExtractor gfx) {
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
    private void bh_drawMiddlePanel(GuiGraphicsExtractor gfx, int x, int y, int width, int height) {
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
    private void bh_drawGearHint(GuiGraphicsExtractor gfx, int x, int y, GearSlot slot, Item item) {
        int slotIndex = this.bh_getGearSlotIndex(slot.ordinal());
        if (slotIndex < 0 || this.menu.getSlot(slotIndex).hasItem()) {
            return;
        }

        int iconX = x + slot.ordinal() * 18 + 1;
        int iconY = y + 1;
        gfx.item(new ItemStack(item), iconX, iconY);
        // Translucent wash. ARGB: alpha 0xA0 (~63%), warm-grey RGB matching old BH_HINT_TINT.
        gfx.fill(iconX, iconY, iconX + 16, iconY + 16, 0xA0B7AB99);
    }

    /**
     * Helper for the new 1.21.11 {@code blit} signature, which now requires an explicit
     * {@link com.mojang.blaze3d.pipeline.RenderPipeline} and texture sheet dimensions.
     * The vanilla horse GUI texture is the standard 256×256 sheet.
     */
    @Unique
    private static void bh_blitGui(GuiGraphicsExtractor gfx, Identifier texture,
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
        HorseInventoryLayoutAccess layoutAccess = this.bh_getLayoutAccessOrNull();
        if (layoutAccess == null) {
            return -1;
        }

        int gearStartIndex = layoutAccess.bh_getGearStartIndex();
        if (gearStartIndex < 0 || gearStartIndex + slotOffset >= this.menu.slots.size()) {
            return -1;
        }
        return gearStartIndex + slotOffset;
    }

    @Unique
    private boolean bh_hasChestStorageLayout() {
        HorseInventoryLayoutAccess layoutAccess = this.bh_getLayoutAccessOrNull();
        return layoutAccess != null && layoutAccess.bh_hasChestStorageLayout();
    }

    @Unique
    private @Nullable HorseInventoryLayoutAccess bh_getLayoutAccessOrNull() {
        return this.menu instanceof HorseInventoryLayoutAccess layoutAccess ? layoutAccess : null;
    }

    @Unique
    private @Nullable AbstractHorse bh_getHorseOrNull() {
        return this.mount instanceof AbstractHorse horse ? horse : null;
    }
}
