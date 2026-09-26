package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {

    @Shadow @Final Container repairSlots;
    @Shadow @Final private Container resultSlots;

    @Inject(method = "createResult", at = @At("TAIL"))
    private void bh_noHarnessMerge(CallbackInfo ci) {
        ItemStack first = this.repairSlots.getItem(0);
        ItemStack second = this.repairSlots.getItem(1);
        if (!first.isEmpty() && !second.isEmpty() && first.is(ModItems.HORSE_STABILIZER)) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }
}
