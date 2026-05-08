package icy.betterhorses.net.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.HorseInventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseInventoryMenu.class)
public interface HorseInventoryMenuAccessor {
    @Accessor("mount")
    LivingEntity bh_getMount();
}
