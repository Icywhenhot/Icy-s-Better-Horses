package icy.betterhorses.net.mixin;

// Dormant on 1.21.10. Saddle insertion flows through the equipment-slot system: the upgraded saddle is registered as Equippable(EquipmentSlot.SADDLE) (see ModItems), so the horse's saddle slot accepts it automatically. 1.21.11 routed saddle acceptance through the shared package-private net.minecraft.world.inventory.ArmorSlot, but that target/behaviour does not apply here, so this is kept as a non-mixin placeholder and is not listed in the mixin config.
public final class HorseSaddleSlotMixin {

    private HorseSaddleSlotMixin() {
    }
}
