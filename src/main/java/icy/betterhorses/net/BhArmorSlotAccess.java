package icy.betterhorses.net;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public interface BhArmorSlotAccess {

    LivingEntity bh_slotOwner();

    EquipmentSlot bh_equipmentSlot();
}
