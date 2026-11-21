package icy.betterhorses.net;

import icy.betterhorses.net.registry.BhContent;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BhHorseAttachments {

    public static final AttachmentType<Integer> BOND = integer(0);
    public static final AttachmentType<Integer> STABILIZER_STATE = integer(HorseStabilizerState.CLOSED.ordinal());
    public static final AttachmentType<Integer> GEAR_FLAGS = integer(0);
    public static final AttachmentType<Integer> STABILIZER_CHARGE = integer(0);
    public static final AttachmentType<Boolean> CART = bool(false);
    public static final AttachmentType<Integer> CARPET = integer(-1);
    public static final AttachmentType<Boolean> CART_CHEST = bool(false);
    public static final AttachmentType<Boolean> CART_PLOW = bool(false);
    public static final AttachmentType<Boolean> ENDER_CHEST = bool(false);
    public static final AttachmentType<Boolean> UPGRADED_SADDLE = bool(false);
    public static final AttachmentType<String> GENDER = string("");
    public static final AttachmentType<String> BREED = string("");
    public static final AttachmentType<String> SPECIES = string("");
    public static final AttachmentType<Boolean> BREED_MIXED = bool(false);
    public static final AttachmentType<Boolean> GELDED = bool(false);
    public static final AttachmentType<String> OWNER = string("");
    public static final AttachmentType<String> TREAT = string("");
    public static final AttachmentType<String> COMMAND = string(BhContent.COMMAND_FOLLOW.getKey().location().toString());
    public static final AttachmentType<Integer> GEAR = integer(0);
    public static final AttachmentType<Integer> GAIT_GEAR = integer(0);
    public static final AttachmentType<Integer> COMBAT = integer(0);
    public static final AttachmentType<Integer> KICK = integer(0);
    public static final AttachmentType<Boolean> FREE_LOOK = bool(false);
    public static final AttachmentType<Integer> CART_TYPE = integer(0);
    public static final AttachmentType<Integer> STOMP = integer(0);
    public static final AttachmentType<Integer> SURGE = integer(0);
    public static final AttachmentType<Integer> SURGE_1 = integer(0);
    public static final AttachmentType<Integer> SURGE_2 = integer(0);
    public static final AttachmentType<Integer> SURGE_3 = integer(0);
    public static final AttachmentType<Integer> PULSE = integer(0);
    public static final AttachmentType<Integer> PERK = integer(0);
    public static final AttachmentType<Integer> CHARGE = integer(BhSurge.HIDDEN);
    public static final AttachmentType<Integer> BEG = integer(0);
    public static final AttachmentType<ItemStack> BANNER = synced(() -> ItemStack.EMPTY,
            BhHorseAttachments::writeBanner, BhHorseAttachments::readBanner);

    private BhHorseAttachments() {}

    public static void register(RegisterEvent event) {
        register(event, "bond", BOND);
        register(event, "stabilizer_state", STABILIZER_STATE);
        register(event, "gear_flags", GEAR_FLAGS);
        register(event, "stabilizer_charge", STABILIZER_CHARGE);
        register(event, "cart", CART);
        register(event, "carpet", CARPET);
        register(event, "cart_chest", CART_CHEST);
        register(event, "cart_plow", CART_PLOW);
        register(event, "ender_chest", ENDER_CHEST);
        register(event, "upgraded_saddle", UPGRADED_SADDLE);
        register(event, "gender", GENDER);
        register(event, "breed", BREED);
        register(event, "species", SPECIES);
        register(event, "breed_mixed", BREED_MIXED);
        register(event, "gelded", GELDED);
        register(event, "owner", OWNER);
        register(event, "treat", TREAT);
        register(event, "command", COMMAND);
        register(event, "gear", GEAR);
        register(event, "gait_gear", GAIT_GEAR);
        register(event, "combat", COMBAT);
        register(event, "kick", KICK);
        register(event, "free_look", FREE_LOOK);
        register(event, "cart_type", CART_TYPE);
        register(event, "stomp", STOMP);
        register(event, "surge", SURGE);
        register(event, "surge_1", SURGE_1);
        register(event, "surge_2", SURGE_2);
        register(event, "surge_3", SURGE_3);
        register(event, "pulse", PULSE);
        register(event, "perk", PERK);
        register(event, "charge", CHARGE);
        register(event, "beg", BEG);
        register(event, "banner", BANNER);
    }

    private static void register(RegisterEvent event, String path, AttachmentType<?> type) {
        event.register(
                NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, path),
                () -> type);
    }

    private static void writeBanner(RegistryFriendlyByteBuf buffer, ItemStack stack) {
        RegistryFriendlyByteBuf packed = new RegistryFriendlyByteBuf(Unpooled.buffer(), buffer.registryAccess(), buffer.getConnectionType());
        try {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(packed, renderCopy(stack));
            byte[] bytes = new byte[packed.readableBytes()];
            packed.readBytes(bytes);
            buffer.writeByteArray(bytes);
        } catch (RuntimeException e) {
            buffer.writeByteArray(new byte[0]);
        } finally {
            packed.release();
        }
    }

    private static ItemStack readBanner(RegistryFriendlyByteBuf buffer) {
        byte[] bytes = buffer.readByteArray();
        if (bytes.length == 0) return ItemStack.EMPTY;
        RegistryFriendlyByteBuf packed = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), buffer.registryAccess(), buffer.getConnectionType());
        try {
            return ItemStack.OPTIONAL_STREAM_CODEC.decode(packed);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        } finally {
            packed.release();
        }
    }

    private static ItemStack renderCopy(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = new ItemStack(stack.getItemHolder(), 1);
        copyComponent(stack, copy, DataComponents.BANNER_PATTERNS);
        copyComponent(stack, copy, DataComponents.BASE_COLOR);
        copyComponent(stack, copy, DataComponents.CUSTOM_MODEL_DATA);
        copyComponent(stack, copy, DataComponents.DYED_COLOR);
        return copy;
    }

    private static <C> void copyComponent(ItemStack from, ItemStack to, DataComponentType<C> type) {
        C value = from.get(type);
        if (value != null) to.set(type, value);
    }

    private static AttachmentType<Integer> integer(int initial) {
        return synced(() -> initial, RegistryFriendlyByteBuf::writeVarInt, RegistryFriendlyByteBuf::readVarInt);
    }

    private static AttachmentType<Boolean> bool(boolean initial) {
        return synced(() -> initial, RegistryFriendlyByteBuf::writeBoolean, RegistryFriendlyByteBuf::readBoolean);
    }

    private static AttachmentType<String> string(String initial) {
        return synced(() -> initial, RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf);
    }

    private static <T> AttachmentType<T> synced(
            Supplier<T> initial,
            BiConsumer<RegistryFriendlyByteBuf, T> writer,
            Function<RegistryFriendlyByteBuf, T> reader) {
        return AttachmentType.builder(initial).sync(new AttachmentSyncHandler<>() {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, T value, boolean initialSync) {
                writer.accept(buffer, value);
            }

            @Override
            public T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buffer, @Nullable T previousValue) {
                return reader.apply(buffer);
            }
        }).build();
    }
}
