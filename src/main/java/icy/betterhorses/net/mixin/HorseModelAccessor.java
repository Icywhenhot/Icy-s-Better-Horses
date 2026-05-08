package icy.betterhorses.net.mixin;

import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 1.21.11: the {@code body} ModelPart moved from {@code HorseModel} up to its new parent
 * {@link AbstractEquineModel}, so we accessor the parent class instead.
 */
@Mixin(AbstractEquineModel.class)
public interface HorseModelAccessor {
    @Accessor("body")
    ModelPart bh_getBody();
}
