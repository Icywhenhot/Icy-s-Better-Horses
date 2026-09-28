package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.mixin.AbstractContainerScreenAccessor;
import icy.betterhorses.net.network.RiderPanelPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public final class RiderPanel {

    private static final int TAB_X = -22;
    private static final int TAB_Y = 28;
    private static final int TAB_SIZE = 20;

    private RiderPanel() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof HorseInventoryScreen horse) {
                Screens.getWidgets(screen).add(new Tab(horse));
            }
        });
    }

    public static boolean onKey(Screen screen, KeyEvent event) {
        if (!(screen instanceof HorseInventoryScreen horse) || !event.hasControlDown()
                || !Minecraft.getInstance().options.keyInventory.matches(event)) {
            return false;
        }
        toggle(horse);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return true;
    }

    public static boolean shown(HorseInventoryScreen screen) {
        return ((HorseInventoryLayoutAccess) screen.getMenu()).bh_isRiderPanel();
    }

    private static void toggle(HorseInventoryScreen screen) {
        boolean shown = !shown(screen);
        ((HorseInventoryLayoutAccess) screen.getMenu()).bh_setRiderPanel(shown);
        ClientPlayNetworking.send(new RiderPanelPayload(shown));
    }

    private static final class Tab extends Button {

        private final HorseInventoryScreen screen;
        private final ItemStack face;
        private final ItemStack saddle = new ItemStack(Items.SADDLE);
        private boolean showing;

        Tab(HorseInventoryScreen screen) {
            super(((AbstractContainerScreenAccessor) screen).bh_leftPos() + TAB_X,
                    ((AbstractContainerScreenAccessor) screen).bh_topPos() + TAB_Y, TAB_SIZE, TAB_SIZE,
                    Component.empty(), b -> toggle(screen), DEFAULT_NARRATION);
            this.screen = screen;
            this.face = new ItemStack(Items.PLAYER_HEAD);
            this.face.set(DataComponents.PROFILE,
                    ResolvableProfile.createResolved(Minecraft.getInstance().player.getGameProfile()));
            this.retip();
        }

        private void retip() {
            this.showing = shown(this.screen);
            Component key = Component.literal("Ctrl + ")
                    .append(Minecraft.getInstance().options.keyInventory.getTranslatedKeyMessage());
            this.setTooltip(Tooltip.create(Component.translatable(this.showing
                    ? "gui.icys-better-horses.rider_panel.to_horse"
                    : "gui.icys-better-horses.rider_panel.to_rider", key)));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float a) {
            if (this.showing != shown(this.screen)) {
                this.retip();
            }
            this.extractDefaultSprite(gfx);
            gfx.item(this.showing ? this.saddle : this.face, this.getX() + 2, this.getY() + 2);
        }
    }
}
