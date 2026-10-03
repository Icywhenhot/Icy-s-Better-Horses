package icy.betterhorses.net.client;

import com.mojang.blaze3d.systems.RenderSystem;
import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorsesClient;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.mixin.AbstractContainerScreenAccessor;
import icy.betterhorses.net.network.CartMenuPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class RiderPanel {

    private static final ResourceLocation HORSE_ICON =
            ResourceLocation.fromNamespaceAndPath("icys-better-horses", "textures/gui/switcher/horse.png");
    private static final int ICON_X = -18;
    private static final int ICON_Y = 24;
    private static final int ICON_W = 16;
    private static final int ICON_H = 21;
    private static final int FACE_SIZE = 16;
    private static final long POP_MS = 220L;
    private static final float POP_FROM = 0.4F;
    private static final int CART_TAB_GAP = 4;

    private static double @Nullable [] cursor;

    private RiderPanel() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof HorseInventoryScreen horse) {
                restoreCursor();
                Screens.getButtons(screen).add(new Tab(horse, false, b -> openPlayerInventory()));
                AbstractHorse mount = ((HorseInventoryLayoutAccess) horse.getMenu()).bh_mount();
                if (mount != null) {
                    AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) horse;
                    Screens.getButtons(screen).add(new CartTab(accessor.bh_leftPos() + ICON_X,
                            accessor.bh_topPos() + ICON_Y + ICON_H + CART_TAB_GAP, mount));
                }
            } else if (screen instanceof CartScreen) {
                restoreCursor();
            } else if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
                restoreCursor();
                if (mountedOnHorse()) {
                    Screens.getButtons(screen).add(new Tab((AbstractContainerScreen<?>) screen, true, b -> backToHorse()));
                }
            }
            ScreenKeyboardEvents.allowKeyPress(screen).register(RiderPanel::allowKeyPress);
        });
    }

    private static boolean allowKeyPress(Screen screen, int key, int scancode, int modifiers) {
        Minecraft mc = Minecraft.getInstance();
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (!ctrl || !mc.options.keyInventory.matches(key, scancode)) {
            return true;
        }
        if (screen instanceof HorseInventoryScreen) {
            openPlayerInventory();
        } else if (mountedOnHorse()
                && (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)) {
            backToHorse();
        } else {
            return true;
        }
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return false;
    }

    static void rememberCursor() {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        cursor = new double[]{mouse.xpos(), mouse.ypos()};
    }

    private static void restoreCursor() {
        if (cursor != null) {
            GLFW.glfwSetCursorPos(Minecraft.getInstance().getWindow().getWindow(), cursor[0], cursor[1]);
            cursor = null;
        }
    }

    private static boolean mountedOnHorse() {
        return Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.getVehicle() instanceof AbstractHorse horse
                && horse.isTamed();
    }

    private static void openPlayerInventory() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        rememberCursor();
        mc.player.closeContainer();
        mc.setScreen(new InventoryScreen(mc.player));
    }

    private static void backToHorse() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        rememberCursor();
        mc.player.closeContainer();
        mc.player.sendOpenInventory();
    }

    private static final class CartTab extends Button {
        private final AbstractHorse mount;
        private final ItemStack icon = new ItemStack(ModItems.HORSE_CART);

        CartTab(int x, int y, AbstractHorse mount) {
            super(x, y, ICON_W, ICON_W, Component.empty(), b -> {
                rememberCursor();
                ClientPlayNetworking.send(new CartMenuPayload(mount.getId()));
            }, DEFAULT_NARRATION);
            this.mount = mount;
            this.setTooltip(Tooltip.create(Component.translatable("gui.icys-better-horses.cart.open",
                    IcysBetterHorsesClient.CART_MENU_KEY.getTranslatedKeyMessage())));
        }

        private boolean hitched() { return IHorseData.of(this.mount).bh_hasCartGear(); }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.hitched() && super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            if (this.hitched()) gfx.renderItem(this.icon, this.getX(), this.getY());
        }
    }

    private static final class Tab extends Button {
        private final AbstractContainerScreen<?> screen;
        private final boolean showing;
        private final long popAt = System.currentTimeMillis();

        Tab(AbstractContainerScreen<?> screen, boolean showing, OnPress press) {
            super(((AbstractContainerScreenAccessor) screen).bh_leftPos() + ICON_X,
                    ((AbstractContainerScreenAccessor) screen).bh_topPos() + ICON_Y,
                    ICON_W, ICON_H, Component.empty(), press, DEFAULT_NARRATION);
            this.screen = screen;
            this.showing = showing;
            Component key = Component.literal("Ctrl + ")
                    .append(Minecraft.getInstance().options.keyInventory.getTranslatedKeyMessage());
            this.setTooltip(Tooltip.create(Component.translatable(this.showing
                    ? "gui.icys-better-horses.rider_panel.to_horse"
                    : "gui.icys-better-horses.rider_panel.to_rider", key)));
        }

        @Override
        protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this.screen;
            this.setPosition(accessor.bh_leftPos() + ICON_X, accessor.bh_topPos() + ICON_Y);
            float t = (System.currentTimeMillis() - this.popAt) / (float) POP_MS;
            float scale = t >= 1.0F ? 1.0F : POP_FROM + (1.0F - POP_FROM) * BhAnim.easeOutBack(t);
            gfx.pose().pushPose();
            gfx.pose().translate(this.getX() + ICON_W / 2.0F, this.getY() + ICON_H / 2.0F, 0.0F);
            gfx.pose().scale(scale, scale, 1.0F);
            gfx.pose().translate(-ICON_W / 2.0F, -ICON_H / 2.0F, 0.0F);
            if (this.showing) {
                PlayerFaceRenderer.draw(gfx, Minecraft.getInstance().player.getSkin(),
                        0, (ICON_H - FACE_SIZE) / 2, FACE_SIZE);
            } else {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                gfx.blit(HORSE_ICON, 0, 0, 0.0F, 0.0F, ICON_W, ICON_H, ICON_W, ICON_H);
                RenderSystem.disableBlend();
            }
            gfx.pose().popPose();
        }
    }
}
