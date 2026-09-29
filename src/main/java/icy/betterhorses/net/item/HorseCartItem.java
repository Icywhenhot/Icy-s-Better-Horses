package icy.betterhorses.net.item;

import icy.betterhorses.net.entity.CartType;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HorseCartItem extends Item {

    private static final String SETUP = "Cart";

    public HorseCartItem(Properties properties) {
        super(properties);
    }

    private static CompoundTag setup(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompoundOrEmpty(SETUP);
    }

    private static void write(ItemStack stack, Consumer<CompoundTag> change) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag setup = tag.getCompoundOrEmpty(SETUP);
            change.accept(setup);
            tag.put(SETUP, setup);
        });
    }

    public static @Nullable CartType storedType(ItemStack stack) {
        return setup(stack).getString("Type").map(CartType::byId).orElse(null);
    }

    public static void setType(ItemStack stack, CartType type) {
        write(stack, tag -> tag.putString("Type", type.id()));
    }

    public static boolean partHidden(ItemStack stack, String part) {
        return setup(stack).getCompoundOrEmpty("Hidden").getBooleanOr(part, false);
    }

    public static void setPartHidden(ItemStack stack, String part, boolean hidden) {
        write(stack, tag -> {
            CompoundTag parts = tag.getCompoundOrEmpty("Hidden");
            if (hidden) {
                parts.putBoolean(part, true);
            } else {
                parts.remove(part);
            }
            tag.put("Hidden", parts);
        });
    }

    public static int pickup(ItemStack stack) {
        return setup(stack).getIntOr("Pickup", HorseCartEntity.PICKUP_DEFAULT);
    }

    public static void setPickup(ItemStack stack, int mask) {
        write(stack, tag -> tag.putInt("Pickup", mask));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        CartType type = storedType(stack);
        if (type != null) {
            lines.accept(type.displayName().withStyle(ChatFormatting.GRAY));
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

        ItemStack stack = context.getItemInHand();
        stack.consume(1, player);
        return InteractionResult.CONSUME;
    }
}
