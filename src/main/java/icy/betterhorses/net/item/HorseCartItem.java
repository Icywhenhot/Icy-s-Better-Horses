package icy.betterhorses.net.item;

import icy.betterhorses.net.entity.CartType;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HorseCartItem extends Item {

    private static final String SETUP = "Cart";

    public HorseCartItem(Properties properties) {
        super(properties);
    }

    public static @Nullable CartType storedType(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(SETUP);
        return tag == null || !tag.contains("Type", Tag.TAG_STRING) ? null : CartType.byId(tag.getString("Type"));
    }

    public static void setType(ItemStack stack, CartType type) {
        stack.getOrCreateTagElement(SETUP).putString("Type", type.id());
    }

    public static boolean partHidden(ItemStack stack, String part) {
        CompoundTag tag = stack.getTagElement(SETUP);
        return tag != null && tag.getCompound("Hidden").getBoolean(part);
    }

    public static void setPartHidden(ItemStack stack, String part, boolean hidden) {
        CompoundTag setup = stack.getOrCreateTagElement(SETUP);
        CompoundTag parts = setup.getCompound("Hidden");
        if (hidden) {
            parts.putBoolean(part, true);
        } else {
            parts.remove(part);
        }
        setup.put("Hidden", parts);
    }

    public static int pickup(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(SETUP);
        return tag == null || !tag.contains("Pickup", Tag.TAG_INT) ? HorseCartEntity.PICKUP_DEFAULT : tag.getInt("Pickup");
    }

    public static void setPickup(ItemStack stack, int mask) {
        stack.getOrCreateTagElement(SETUP).putInt("Pickup", mask);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        CartType type = storedType(stack);
        if (type != null) {
            lines.add(type.displayName().withStyle(ChatFormatting.GRAY));
        }
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
        HorseCartEntity cart = HorseCartEntity.place(level, pos, player.getYRot() + 180.0F, context.getItemInHand());
        if (cart == null) {
            return InteractionResult.PASS;
        }

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
