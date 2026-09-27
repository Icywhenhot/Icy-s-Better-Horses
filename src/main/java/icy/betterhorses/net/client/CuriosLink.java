package icy.betterhorses.net.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.client.ICuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

final class CuriosLink {

    private static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath("curios", "button"),
            ResourceLocation.fromNamespaceAndPath("curios", "button_highlighted"));

    private CuriosLink() {}

    static AbstractWidget button(int x, int y) {
        ImageButton button = new ImageButton(x, y, 10, 10, SPRITES, b -> open());
        button.setTooltip(Tooltip.create(Component.translatable("gui.icys-better-horses.rider_panel.curios")));
        return button;
    }

    static boolean isCuriosScreen(Screen screen) {
        return screen instanceof ICuriosScreen;
    }

    static int panelWidth(Screen screen) {
        return screen instanceof AbstractContainerScreen<?> gui && gui.getMenu() instanceof CuriosContainer menu
                ? menu.panelWidth
                : 0;
    }

    private static void open() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack carried = player.containerMenu.getCarried();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        RiderPanel.rememberCursor();
        PacketDistributor.sendToServer(new CPacketOpenCurios(carried));
    }
}
