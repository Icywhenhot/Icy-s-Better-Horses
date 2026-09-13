package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhHorseModelAccess;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractEquineModel.class)
public interface HorseModelAccessor extends BhHorseModelAccess {
    @Accessor("body")
    ModelPart bh_getBody();
}
