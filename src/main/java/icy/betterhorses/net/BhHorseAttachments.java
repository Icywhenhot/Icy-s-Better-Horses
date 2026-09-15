package icy.betterhorses.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BhHorseAttachments {

    public static final AttachmentType<Integer> BOND = integer(0);
    public static final AttachmentType<Integer> STABILIZER_STATE = integer(HorseStabilizerState.CLOSED.ordinal());
    public static final AttachmentType<Integer> GEAR_FLAGS = integer(0);
    public static final AttachmentType<Boolean> CART = bool(false);
    public static final AttachmentType<Boolean> CART_CHEST = bool(false);
    public static final AttachmentType<Boolean> CART_PLOW = bool(false);
    public static final AttachmentType<Boolean> ENDER_CHEST = bool(false);
    public static final AttachmentType<Boolean> UPGRADED_SADDLE = bool(false);
    public static final AttachmentType<Optional<BlockPos>> HITCHPOST_POS = optionalBlockPos();
    public static final AttachmentType<Integer> GENDER = integer(0);
    public static final AttachmentType<Integer> BREED = integer(HorseBreed.UNKNOWN_SPECIES.ordinal());
    public static final AttachmentType<Boolean> BREED_MIXED = bool(false);
    public static final AttachmentType<String> OWNER = string("");
    public static final AttachmentType<Integer> COMMAND = integer(HorseCommand.FOLLOW.ordinal());
    public static final AttachmentType<Integer> GEAR = integer(0);
    public static final AttachmentType<Integer> GAIT_GEAR = integer(0);
    public static final AttachmentType<Integer> COMBAT = integer(0);
    public static final AttachmentType<Integer> KICK = integer(0);
    public static final AttachmentType<Boolean> FREE_LOOK = bool(false);
    public static final AttachmentType<Boolean> CART_LARGE = bool(false);
    public static final AttachmentType<Integer> STOMP = integer(0);
    public static final AttachmentType<Integer> SURGE = integer(0);
    public static final AttachmentType<Integer> PULSE = integer(0);
    public static final AttachmentType<Integer> PERK = integer(0);
    public static final AttachmentType<Integer> CHARGE = integer(BhSurge.HIDDEN);

    private BhHorseAttachments() {}

    public static void register(RegisterEvent event) {
        register(event, "bond", BOND);
        register(event, "stabilizer_state", STABILIZER_STATE);
        register(event, "gear_flags", GEAR_FLAGS);
        register(event, "cart", CART);
        register(event, "cart_chest", CART_CHEST);
        register(event, "cart_plow", CART_PLOW);
        register(event, "ender_chest", ENDER_CHEST);
        register(event, "upgraded_saddle", UPGRADED_SADDLE);
        register(event, "hitchpost_pos", HITCHPOST_POS);
        register(event, "gender", GENDER);
        register(event, "breed", BREED);
        register(event, "breed_mixed", BREED_MIXED);
        register(event, "owner", OWNER);
        register(event, "command", COMMAND);
        register(event, "gear", GEAR);
        register(event, "gait_gear", GAIT_GEAR);
        register(event, "combat", COMBAT);
        register(event, "kick", KICK);
        register(event, "free_look", FREE_LOOK);
        register(event, "cart_large", CART_LARGE);
        register(event, "stomp", STOMP);
        register(event, "surge", SURGE);
        register(event, "pulse", PULSE);
        register(event, "perk", PERK);
        register(event, "charge", CHARGE);
    }

    private static void register(RegisterEvent event, String path, AttachmentType<?> type) {
        event.register(
                NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, path),
                () -> type);
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

    private static AttachmentType<Optional<BlockPos>> optionalBlockPos() {
        return synced(
                Optional::empty,
                (buffer, value) -> {
                    buffer.writeBoolean(value.isPresent());
                    value.ifPresent(buffer::writeBlockPos);
                },
                buffer -> buffer.readBoolean() ? Optional.of(buffer.readBlockPos()) : Optional.empty());
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
