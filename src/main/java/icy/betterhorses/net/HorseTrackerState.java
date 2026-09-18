package icy.betterhorses.net;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HorseTrackerState extends SavedData {

    public record KnownPosition(ResourceKey<Level> dimension, BlockPos pos) {
        static final Codec<KnownPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(KnownPosition::dimension),
                BlockPos.CODEC.fieldOf("pos").forGetter(KnownPosition::pos)
        ).apply(instance, KnownPosition::new));
    }

    private static final Codec<HorseTrackerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC)
                    .optionalFieldOf("last_ridden_by_player", Map.of()).forGetter(state -> state.lastRiddenByPlayer),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, KnownPosition.CODEC)
                    .optionalFieldOf("last_known_positions", Map.of()).forGetter(state -> state.lastKnownPositions),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, CompoundTag.CODEC)
                    .optionalFieldOf("horse_snapshots", Map.of()).forGetter(state -> state.snapshots),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT)
                    .optionalFieldOf("horse_generations", Map.of()).forGetter(state -> state.generations),
            UUIDUtil.STRING_CODEC.listOf()
                    .optionalFieldOf("pending_disowns", List.of()).forGetter(state -> List.copyOf(state.pendingDisowns)),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC)
                    .optionalFieldOf("active_horse_by_player", Map.of()).forGetter(state -> state.activeHorseByPlayer),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING))
                    .optionalFieldOf("trusted_by_player", Map.of()).forGetter(state -> state.trustedByPlayer)
    ).apply(instance, HorseTrackerState::new));

    private static final String FILE_NAME = "icys-better-horses_horse_tracker";

    private final Map<UUID, UUID> lastRiddenByPlayer;
    private final Map<UUID, KnownPosition> lastKnownPositions;
    private final Map<UUID, CompoundTag> snapshots;
    private final Map<UUID, Integer> generations;
    private final Set<UUID> pendingDisowns;
    private final Map<UUID, UUID> activeHorseByPlayer;
    private final Map<UUID, Map<UUID, String>> trustedByPlayer;

    public HorseTrackerState() {
        this.lastRiddenByPlayer = new ConcurrentHashMap<>();
        this.lastKnownPositions = new ConcurrentHashMap<>();
        this.snapshots = new ConcurrentHashMap<>();
        this.generations = new ConcurrentHashMap<>();
        this.pendingDisowns = ConcurrentHashMap.newKeySet();
        this.activeHorseByPlayer = new ConcurrentHashMap<>();
        this.trustedByPlayer = new ConcurrentHashMap<>();
    }

    private HorseTrackerState(
            Map<UUID, UUID> lastRiddenByPlayer,
            Map<UUID, KnownPosition> lastKnownPositions,
            Map<UUID, CompoundTag> snapshots,
            Map<UUID, Integer> generations,
            List<UUID> pendingDisowns,
            Map<UUID, UUID> activeHorseByPlayer,
            Map<UUID, Map<UUID, String>> trustedByPlayer) {
        this.lastRiddenByPlayer = new ConcurrentHashMap<>(lastRiddenByPlayer);
        this.lastKnownPositions = new ConcurrentHashMap<>(lastKnownPositions);
        this.snapshots = new ConcurrentHashMap<>(snapshots);
        this.generations = new ConcurrentHashMap<>(generations);
        this.pendingDisowns = ConcurrentHashMap.newKeySet();
        this.pendingDisowns.addAll(pendingDisowns);
        this.activeHorseByPlayer = new ConcurrentHashMap<>(activeHorseByPlayer);
        this.trustedByPlayer = new ConcurrentHashMap<>();
        trustedByPlayer.forEach((owner, trusted) -> this.trustedByPlayer.put(owner, new ConcurrentHashMap<>(trusted)));
    }

    public static HorseTrackerState get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(HorseTrackerState::load, HorseTrackerState::new, FILE_NAME);
    }

    private static HorseTrackerState load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(message -> IcysBetterHorses.LOGGER.error("Could not load horse tracker: {}", message))
                .orElseGet(HorseTrackerState::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(message -> IcysBetterHorses.LOGGER.error("Could not save horse tracker: {}", message))
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .ifPresent(tag::merge);
        return tag;
    }

    public void setLastRidden(UUID playerId, UUID horseId) {
        lastRiddenByPlayer.put(playerId, horseId);
        setDirty();
    }

    public @Nullable UUID getLastRiddenId(UUID playerId) {
        return lastRiddenByPlayer.get(playerId);
    }

    public void recordHorse(AbstractHorse horse) {
        UUID horseId = horse.getUUID();
        lastKnownPositions.put(horseId, new KnownPosition(horse.level().dimension(), horse.blockPosition()));
        CompoundTag snapshot = horse.saveWithoutId(new CompoundTag());
        snapshot.putString("id", net.minecraft.world.entity.EntityType.getKey(horse.getType()).toString());
        IHorseData data = IHorseData.of(horse);
        CompoundTag summary = new CompoundTag();
        summary.putString("name", horse.hasCustomName() ? horse.getCustomName().getString() : "");
        summary.putString("breedId", data.bh_getBreed().id());
        summary.putInt("gender", data.bh_getGender().ordinal());
        summary.putBoolean("mixed", data.bh_isMixedBreed());
        summary.putInt("bond", data.bh_getBond());
        summary.putBoolean("home", data.bh_getHome() != null);
        summary.putString("type", net.minecraft.world.entity.EntityType.getKey(horse.getType()).toString());
        summary.putInt("variant", horse instanceof net.minecraft.world.entity.animal.horse.Horse h ? h.getVariant().ordinal() : -1);
        summary.putInt("markings", horse instanceof net.minecraft.world.entity.animal.horse.Horse h ? h.getMarkings().ordinal() : -1);
        summary.putBoolean("baby", horse.isBaby());
        summary.putInt("coat", horse instanceof icy.betterhorses.net.entity.BhBreedHorse h ? h.bhCoat() : -1);
        snapshot.put("BH_Roster", summary);
        CompoundTag old = snapshots.put(horseId, snapshot);
        if (!snapshot.equals(old)) setDirty();
    }

    public void forgetHorse(UUID horseId) {
        boolean removed = lastKnownPositions.remove(horseId) != null;
        removed |= snapshots.remove(horseId) != null;
        if (removed) {
            setDirty();
        }
    }

    public @Nullable KnownPosition getLastKnownPosition(UUID horseId) {
        return lastKnownPositions.get(horseId);
    }

    public @Nullable CompoundTag getSnapshot(UUID horseId) {
        return snapshots.get(horseId);
    }

    public @Nullable UUID findStoredHorseOwnedBy(UUID playerId) {
        for (Map.Entry<UUID, CompoundTag> entry : snapshots.entrySet()) {
            if (isOwnedBy(entry.getValue(), playerId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public List<UUID> findAllStoredHorsesOwnedBy(UUID playerId) {
        List<UUID> owned = new ArrayList<>();
        for (Map.Entry<UUID, CompoundTag> entry : snapshots.entrySet()) {
            if (isOwnedBy(entry.getValue(), playerId)) {
                owned.add(entry.getKey());
            }
        }
        return owned;
    }

    private static boolean isOwnedBy(CompoundTag snapshot, UUID playerId) {
        return snapshot.hasUUID("BH_Owner") && playerId.equals(snapshot.getUUID("BH_Owner"));
    }

    public void setActiveHorse(UUID playerId, UUID horseId) {
        activeHorseByPlayer.put(playerId, horseId);
        setDirty();
    }

    public @Nullable UUID getActiveHorseId(UUID playerId) {
        return activeHorseByPlayer.get(playerId);
    }

    public void clearActiveHorse(UUID horseId) {
        if (activeHorseByPlayer.values().removeIf(horseId::equals)) {
            setDirty();
        }
    }

    public boolean trust(UUID ownerId, UUID trustedId, String trustedName) {
        Map<UUID, String> trusted = trustedByPlayer.computeIfAbsent(ownerId, id -> new ConcurrentHashMap<>());
        String previous = trusted.put(trustedId, trustedName);
        setDirty();
        return previous == null;
    }

    public boolean untrust(UUID ownerId, UUID trustedId) {
        Map<UUID, String> trusted = trustedByPlayer.get(ownerId);
        if (trusted == null || trusted.remove(trustedId) == null) {
            return false;
        }
        if (trusted.isEmpty()) {
            trustedByPlayer.remove(ownerId, trusted);
        }
        setDirty();
        return true;
    }

    public boolean isTrusted(UUID ownerId, UUID playerId) {
        Map<UUID, String> trusted = trustedByPlayer.get(ownerId);
        return trusted != null && trusted.containsKey(playerId);
    }

    public Map<UUID, String> getTrusted(UUID ownerId) {
        Map<UUID, String> trusted = trustedByPlayer.get(ownerId);
        return trusted == null ? Map.of() : Map.copyOf(trusted);
    }

    public List<UUID> getTrustingOwners(UUID playerId) {
        List<UUID> owners = new ArrayList<>();
        trustedByPlayer.forEach((ownerId, trusted) -> {
            if (trusted.containsKey(playerId)) {
                owners.add(ownerId);
            }
        });
        return owners;
    }

    public void markPendingDisown(UUID horseId) {
        if (pendingDisowns.add(horseId)) {
            setDirty();
        }
    }

    public boolean consumePendingDisown(UUID horseId) {
        if (pendingDisowns.remove(horseId)) {
            setDirty();
            return true;
        }
        return false;
    }

    public int getGeneration(UUID horseId) {
        return generations.getOrDefault(horseId, 0);
    }

    public String describe() {
        return lastRiddenByPlayer.size() + " last-ridden entries, "
                + snapshots.size() + " snapshots, "
                + lastKnownPositions.size() + " positions, "
                + generations.size() + " generations";
    }

    public void setGeneration(UUID horseId, int generation) {
        generations.put(horseId, generation);
        setDirty();
    }
}
