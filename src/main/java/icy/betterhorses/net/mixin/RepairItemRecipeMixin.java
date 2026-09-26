package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"), cancellable = true)
    private void bh_noHarnessCombine(CraftingContainer grid, Level level, CallbackInfoReturnable<Boolean> cir) {
        for (int i = 0; i < grid.getContainerSize(); i++) {
            if (grid.getItem(i).is(ModItems.HORSE_STABILIZER)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
