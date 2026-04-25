package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
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

/**
 * Makes horses wearing the {@link ModItems#HORSE_HOOVES} gear walk on top of powdered snow
 * instead of sinking through it. Vanilla only allows entities in the
 * {@code minecraft:powder_snow_walkable_mobs} tag or entities wearing leather boots.
 */
@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {

    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void bh_allowHorseWithHoovesGear(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof AbstractHorse horse) || !(horse instanceof IHorseData data)) {
            return;
        }

        SimpleContainer gear = data.bh_getGearContainer();
        if (gear == null || gear.getContainerSize() <= GearSlot.HOOVES.ordinal()) {
            return;
        }

        ItemStack hooves = gear.getItem(GearSlot.HOOVES.ordinal());
        if (hooves.is(ModItems.HORSE_HOOVES)) {
            cir.setReturnValue(true);
        }
    }
}
