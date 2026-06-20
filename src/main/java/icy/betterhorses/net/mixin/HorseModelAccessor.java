package icy.betterhorses.net.mixin;

import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseModel.class)
public interface HorseModelAccessor {
    @Accessor("body")
    ModelPart bh_getBody();
}
