package icy.betterhorses.net.client.mixin;

import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractEquineModel.class)
public interface AbstractEquineModelAccessor {
    @Accessor("body")
    ModelPart bh_getBody();
}
