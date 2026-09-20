package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhHorseModelAccess;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseModel.class)
public interface HorseModelAccessor extends BhHorseModelAccess {
    @Accessor("body")
    @Override
    ModelPart bh_getBody();
}
