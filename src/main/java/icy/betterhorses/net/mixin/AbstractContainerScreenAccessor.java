package icy.betterhorses.net.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Mutable
    @Accessor("imageHeight")
    void bh_setImageHeight(int value);

    @Accessor("leftPos")
    int bh_leftPos();

    @Accessor("topPos")
    int bh_topPos();

    @Accessor("imageWidth")
    int bh_imageWidth();
}
