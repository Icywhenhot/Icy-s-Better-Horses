package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
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

    @Shadow public int repairItemCountCost;
    @Shadow @Final private DataSlot cost;
    @Shadow private String itemName;

    protected AnvilMenuMixin(MenuType<?> type, int id, Inventory inventory, ContainerLevelAccess access) {
        super(type, id, inventory, access);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void bh_refuelHarness(CallbackInfo ci) {
        ItemStack harness = this.inputSlots.getItem(0);
        ItemStack addition = this.inputSlots.getItem(1);
        if (!harness.is(ModItems.HORSE_STABILIZER) || addition.isEmpty()) {
            return;
        }
        ci.cancel();
        if (!addition.is(ModItems.CANISTER) || !harness.isDamaged()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            return;
        }
        ItemStack refuelled = harness.copy();
        refuelled.setDamageValue(Math.max(0, harness.getDamageValue() - harness.getMaxDamage() * 4 / 5));
        String name = this.itemName;
        if (name != null && !name.isBlank() && !name.equals(harness.getHoverName().getString())) {
            refuelled.setHoverName(Component.literal(name));
        }
        this.resultSlots.setItem(0, refuelled);
        this.cost.set(Math.max(1, harness.getBaseRepairCost() + 1));
        this.repairItemCountCost = 1;
    }
}
