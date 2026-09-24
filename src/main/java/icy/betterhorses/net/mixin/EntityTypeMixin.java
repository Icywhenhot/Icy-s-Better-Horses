package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseBackup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @Inject(method = "by", at = @At("RETURN"), cancellable = true)
    private static void bh_loadBreedHorse(ValueInput input, CallbackInfoReturnable<Optional<EntityType<?>>> cir) {
        if (cir.getReturnValue().filter(type -> type == EntityTypes.HORSE).isEmpty()) {
            return;
        }
        EntityType<?> saved = BhHorseBackup.savedType(input.getStringOr("BH_EntityId", null));
        if (saved != null) {
            cir.setReturnValue(Optional.of(saved));
        }
    }
}
