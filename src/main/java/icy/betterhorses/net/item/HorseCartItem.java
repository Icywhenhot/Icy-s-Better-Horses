package icy.betterhorses.net.item;

import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

public class HorseCartItem extends Item {

    public HorseCartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos above = context.getClickedPos().above();
        Vec3 pos = new Vec3(above.getX() + 0.5D, above.getY(), above.getZ() + 0.5D);
        HorseCartEntity cart = HorseCartEntity.place(level, pos, player.getYRot() + 180.0F, CartSize.NORMAL);
        if (cart == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        stack.consume(1, player);
        return InteractionResult.CONSUME;
    }
}
