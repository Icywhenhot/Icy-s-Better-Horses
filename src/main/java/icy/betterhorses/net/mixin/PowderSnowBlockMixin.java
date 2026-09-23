package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.ArchetypePerks;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {

    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void bh_allowHorseOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof AbstractHorse horse) || !BhHorseKind.managed(horse)
                || !(horse instanceof IHorseData data)) {
            return;
        }

        if (ArchetypePerks.walksOnPowderSnow(BhBreedData.of(data.bh_getBreedKey()).archetype())) {
            cir.setReturnValue(true);
            return;
        }

        if (!BhConfig.hoovesEnabled()) {
            return;
        }

        SimpleContainer gear = data.bh_getGearContainer();
        if (gear == null || gear.getContainerSize() <= GearSlot.HOOVES.ordinal()) {
            return;
        }

        ItemStack hooves = gear.getItem(GearSlot.HOOVES.ordinal());
        if (hooves.is(ModItems.HORSE_HOOVES.get())) {
            cir.setReturnValue(true);
        }
    }
}
