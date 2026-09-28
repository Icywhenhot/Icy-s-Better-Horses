package icy.betterhorses.net.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IcysBetterHorsesClient;
import icy.betterhorses.net.network.RiderPanelPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

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
    private static final int CURIOS_X = 24;
    private static final int CURIOS_Y = 16;
    private static final boolean CURIOS = ModList.get().isLoaded("curios");

    private static double @Nullable [] cursor;

    private RiderPanel() {}

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof HorseInventoryScreen horse) {
            restoreCursor();
            AbstractWidget curios = CURIOS
                    ? CuriosLink.button(horse.getGuiLeft() + CURIOS_X, horse.getGuiTop() + CURIOS_Y)
                    : null;
            event.addListener(new Tab(horse.getGuiLeft() + ICON_X, horse.getGuiTop() + ICON_Y,
                    () -> shown(horse), b -> toggle(horse), curios));
            if (curios != null) {
                event.addListener(curios);
            }
        } else if (CURIOS && CuriosLink.isCuriosScreen(screen)) {
            restoreCursor();
            if (mountedOnHorse()) {
                AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;
                event.addListener(new Tab(gui.getGuiLeft() - CuriosLink.panelWidth(screen) + ICON_X,
                        gui.getGuiTop() + ICON_Y, () -> true, b -> backToHorse(), null));
            }
        }
    }

    public static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (!IcysBetterHorsesClient.RIDER_PANEL_KEY.isActiveAndMatches(
                InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
            return;
        }
        Screen screen = event.getScreen();
        if (screen instanceof HorseInventoryScreen horse) {
            toggle(horse);
        } else if (CURIOS && CuriosLink.isCuriosScreen(screen) && mountedOnHorse()) {
            backToHorse();
        } else {
            return;
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        event.setCanceled(true);
    }

    public static boolean shown(HorseInventoryScreen screen) {
        return ((HorseInventoryLayoutAccess) screen.getMenu()).bh_isRiderPanel();
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
        return Minecraft.getInstance().player.getVehicle() instanceof AbstractHorse horse && horse.isTamed();
    }

    private static void toggle(HorseInventoryScreen screen) {
        boolean shown = !shown(screen);
        ((HorseInventoryLayoutAccess) screen.getMenu()).bh_setRiderPanel(shown);
        PacketDistributor.sendToServer(new RiderPanelPayload(shown));
    }

    private static void backToHorse() {
        rememberCursor();
        Minecraft.getInstance().player.sendOpenInventory();
    }

    private static final class Tab extends Button {

        private final BooleanSupplier riderShown;
        private final @Nullable AbstractWidget curios;
        private boolean showing;
        private long popAt = -1L;

        Tab(int x, int y, BooleanSupplier riderShown, OnPress press, @Nullable AbstractWidget curios) {
            super(x, y, ICON_W, ICON_H, Component.empty(), press, DEFAULT_NARRATION);
            this.riderShown = riderShown;
            this.curios = curios;
            this.retip();
        }

        private void retip() {
            this.showing = this.riderShown.getAsBoolean();
            if (this.curios != null) {
                this.curios.visible = this.showing;
            }
            Component key = IcysBetterHorsesClient.RIDER_PANEL_KEY.getTranslatedKeyMessage();
            this.setTooltip(Tooltip.create(Component.translatable(this.showing
                    ? "gui.icys-better-horses.rider_panel.to_horse"
                    : "gui.icys-better-horses.rider_panel.to_rider", key)));
        }

        @Override
        protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            if (this.showing != this.riderShown.getAsBoolean()) {
                this.retip();
                this.popAt = System.currentTimeMillis();
            }
            float t = this.popAt < 0L ? 1.0F : (System.currentTimeMillis() - this.popAt) / (float) POP_MS;
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
