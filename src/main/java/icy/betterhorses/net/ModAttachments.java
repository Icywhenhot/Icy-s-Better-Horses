package icy.betterhorses.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BhHorseSyncState>> HORSE_SYNC =
            ATTACHMENTS.register("horse_sync", () -> AttachmentType
                    .builder(holder -> new BhHorseSyncState())
                    .sync(new HorseSyncHandler())
                    .build());

    private ModAttachments() {}

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }

    public static final class BhHorseSyncState {
        public int bond = 0;
        public int stabilizerStateId = HorseStabilizerState.CLOSED.ordinal();
        public int gearFlags = 0;
        public int genderId = HorseGender.MALE.ordinal();
        public int breedId = HorseBreed.UNKNOWN_SPECIES.ordinal();
        public boolean breedMixed = false;
        // Owner UUID as a String ("" = unowned), so the client can compare against the local player when opening the command wheel.
        public String ownerUuid = "";
    }

    private static final class HorseSyncHandler implements AttachmentSyncHandler<BhHorseSyncState> {
        @Override
        public void write(RegistryFriendlyByteBuf buf, BhHorseSyncState value, boolean initialSync) {
            buf.writeVarInt(value.bond);
            buf.writeVarInt(value.stabilizerStateId);
            buf.writeVarInt(value.gearFlags);
            buf.writeVarInt(value.genderId);
            buf.writeVarInt(value.breedId);
            buf.writeBoolean(value.breedMixed);
            buf.writeUtf(value.ownerUuid);
        }

        @Override
        public BhHorseSyncState read(net.neoforged.neoforge.attachment.IAttachmentHolder holder,
                                     RegistryFriendlyByteBuf buf,
                                     BhHorseSyncState value) {
            value.bond = buf.readVarInt();
            value.stabilizerStateId = buf.readVarInt();
            value.gearFlags = buf.readVarInt();
            value.genderId = buf.readVarInt();
            value.breedId = buf.readVarInt();
            value.breedMixed = buf.readBoolean();
            value.ownerUuid = buf.readUtf();
            return value;
        }
    }
}
