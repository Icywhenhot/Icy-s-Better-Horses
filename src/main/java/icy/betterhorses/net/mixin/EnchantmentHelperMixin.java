package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.ArmorEnchants;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(method = "getMobLooting", at = @At("RETURN"), cancellable = true)
    private static void bh_paintLooting(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof Player) || !(entity.getVehicle() instanceof AbstractHorse horse)
                || !BhHorseKind.managed(horse)) {
            return;
        }
        IHorseData d = IHorseData.of(horse);
        if (!Objects.equals(d.bh_getBreedKey(), BhContent.AMERICAN_PAINT.key()) || !BhAbility.PAINT_LOOTING.on()) {
            return;
        }
        cir.setReturnValue(Math.max(cir.getReturnValueI(), BhHorseTraits.bondTier(d.bh_getBond()) + 1));
    }

    @Redirect(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean bh_horseArmorTable(EnchantmentCategory category, Item item) {
        return item instanceof HorseArmorItem ? ArmorEnchants.accepts(category) : category.canEnchant(item);
    }

    @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"))
    private static void bh_horseArmorChoices(int level, ItemStack stack, boolean treasure,
                                           CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (stack.getItem() instanceof HorseArmorItem) {
            cir.getReturnValue().removeIf(entry -> !ArmorEnchants.accepts(entry.enchantment));
        }
    }
}
