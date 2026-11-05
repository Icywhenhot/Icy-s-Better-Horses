package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import icy.betterhorses.net.entity.BhBreedDonkey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodOnAStickItem.class)
public abstract class FoodOnAStickItemMixin {

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"))
    private EntityType<?> bh_donkeyBoost(EntityType<?> type, Level level, Player player, InteractionHand hand) {
        return player.getItemInHand(hand).is(Items.CARROT_ON_A_STICK)
                && player.getControlledVehicle() instanceof BhBreedDonkey ? EntityType.PIG : type;
    }
}
