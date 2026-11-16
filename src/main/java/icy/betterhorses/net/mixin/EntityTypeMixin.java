package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseBackup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @Inject(method = "by", at = @At("RETURN"), cancellable = true)
    private static void bh_loadBreedHorse(CompoundTag compound, CallbackInfoReturnable<Optional<EntityType<?>>> cir) {
        EntityType<?> vanilla = cir.getReturnValue().orElse(null);
        if (vanilla != EntityType.HORSE && vanilla != EntityType.DONKEY && vanilla != EntityType.MULE
                && vanilla != EntityType.SKELETON_HORSE && vanilla != EntityType.ZOMBIE_HORSE) {
            return;
        }
        EntityType<?> saved = BhHorseBackup.savedType(compound.getString("BH_EntityId"));
        if (saved != null) {
            cir.setReturnValue(Optional.of(saved));
        }
    }
}
