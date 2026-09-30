package icy.betterhorses.net.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import top.theillusivec4.curios.api.client.ICuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerV2;

final class CuriosLink {

    private CuriosLink() {}

    static boolean isCuriosScreen(Screen screen) {
        return screen instanceof ICuriosScreen;
    }

    static int panelWidth(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> gui)) {
            return 0;
        }
        if (gui.getMenu() instanceof CuriosContainerV2 menu) {
            return menu.panelWidth;
        }
        if (gui.getMenu() instanceof CuriosContainer menu) {
            return 26 + (menu.hasCosmeticColumn() ? 19 : 0) + (menu.canScroll() ? 16 : 0);
        }
        return 0;
    }

}
