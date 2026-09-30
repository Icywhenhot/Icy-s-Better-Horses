package icy.betterhorses.net.mixin;

import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HorseArmorItem.class)
public abstract class HorseArmorItemMixin extends Item {

    protected HorseArmorItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        if ((Object) this == Items.LEATHER_HORSE_ARMOR) return 15;
        if ((Object) this == Items.IRON_HORSE_ARMOR) return 9;
        if ((Object) this == Items.GOLDEN_HORSE_ARMOR) return 25;
        return 10;
    }
}
