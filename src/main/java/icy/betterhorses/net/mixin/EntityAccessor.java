package icy.betterhorses.net.mixin;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("vehicle")
    void bh_setVehicle(@Nullable Entity vehicle);

    @Invoker("unsetRemoved")
    void bh_unsetRemoved();
}
