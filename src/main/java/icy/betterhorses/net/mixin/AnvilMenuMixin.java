package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow private int repairItemCountCost;
    @Shadow @Final private DataSlot cost;

    protected AnvilMenuMixin(MenuType<?> type, int id, Inventory inventory,
                             ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slots) {
        super(type, id, inventory, access, slots);
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void bh_refuelHarness(CallbackInfo ci) {
        ItemStack harness = this.inputSlots.getItem(0);
        ItemStack addition = this.inputSlots.getItem(1);
        if (!harness.is(ModItems.HORSE_STABILIZER) || addition.isEmpty()) {
            return;
        }
        ItemStack result = this.resultSlots.getItem(0);
        if (!addition.is(ModItems.CANISTER) || result.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            return;
        }
        result.setDamageValue(Math.max(0, harness.getDamageValue() - harness.getMaxDamage() * 4 / 5));
        this.repairItemCountCost = 1;
    }
}
