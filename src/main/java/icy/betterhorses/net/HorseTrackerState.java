package icy.betterhorses.net;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueOutput;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World-saved half of the horse tracker: which horse each player last rode, where each owned horse
 * was last seen, a full NBT snapshot of it, and a generation counter per horse. The whistle respawns
 * a horse from its snapshot when the real entity is unreachable (unloaded chunk); the generation
 * counter marks the copy left behind in the unloaded chunk as stale so it is discarded when its
 * chunk eventually loads. Everything survives restarts.
 */
public class HorseTrackerState extends SavedData {

    /** Where an owned horse was last seen, used for the same-dimension check when whistling. */
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
                    .optionalFieldOf("active_horse_by_player", Map.of()).forGetter(state -> state.activeHorseByPlayer)
    ).apply(instance, HorseTrackerState::new));

    public static final SavedDataType<HorseTrackerState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_tracker"),
            HorseTrackerState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, UUID> lastRiddenByPlayer;
    private final Map<UUID, KnownPosition> lastKnownPositions;
    private final Map<UUID, CompoundTag> snapshots;
    private final Map<UUID, Integer> generations;
    /**
     * Horses disowned while their chunk was unloaded. The stored snapshot is dropped immediately so
     * the roster and the whistle forget them at once, but the real entity is still sitting in an
     * unloaded chunk with its owner tag intact — so it is released the next time it loads.
     */
    private final Set<UUID> pendingDisowns;
    /**
     * The horse each player picked in the management screen. The whistle calls this one before
     * falling back to "last ridden, else nearest", and it only changes when the player says so.
     */
    private final Map<UUID, UUID> activeHorseByPlayer;

    // Concurrent collections: parallel-ticking mods mutate these from multiple entity-tick threads at
    // once (e.g. two horses unloading, or a dismount landing while another horse records its position).
    public HorseTrackerState() {
        this.lastRiddenByPlayer = new ConcurrentHashMap<>();
        this.lastKnownPositions = new ConcurrentHashMap<>();
        this.snapshots = new ConcurrentHashMap<>();
        this.generations = new ConcurrentHashMap<>();
        this.pendingDisowns = ConcurrentHashMap.newKeySet();
        this.activeHorseByPlayer = new ConcurrentHashMap<>();
    }

    private HorseTrackerState(
            Map<UUID, UUID> lastRiddenByPlayer,
            Map<UUID, KnownPosition> lastKnownPositions,
            Map<UUID, CompoundTag> snapshots,
            Map<UUID, Integer> generations,
            List<UUID> pendingDisowns,
            Map<UUID, UUID> activeHorseByPlayer) {
        this.lastRiddenByPlayer = new ConcurrentHashMap<>(lastRiddenByPlayer);
        this.lastKnownPositions = new ConcurrentHashMap<>(lastKnownPositions);
        this.snapshots = new ConcurrentHashMap<>(snapshots);
        this.generations = new ConcurrentHashMap<>(generations);
        this.pendingDisowns = ConcurrentHashMap.newKeySet();
        this.pendingDisowns.addAll(pendingDisowns);
        this.activeHorseByPlayer = new ConcurrentHashMap<>(activeHorseByPlayer);
    }

    public static HorseTrackerState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void setLastRidden(UUID playerId, UUID horseId) {
        lastRiddenByPlayer.put(playerId, horseId);
        setDirty();
    }

    public @Nullable UUID getLastRiddenId(UUID playerId) {
        return lastRiddenByPlayer.get(playerId);
    }

    /** Records both the horse's position and a full NBT snapshot the whistle can respawn it from. */
    public void recordHorse(AbstractHorse horse) {
        UUID horseId = horse.getUUID();
        lastKnownPositions.put(horseId, new KnownPosition(horse.level().dimension(), horse.blockPosition()));
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, horse.registryAccess());
        if (horse.save(output)) {
            snapshots.put(horseId, output.buildResult());
        }
        setDirty();
    }

    /**
     * Drops the position and snapshot of a horse that died, was discarded, or was disowned. The
     * generation entry is deliberately kept forever so a stale copy of a dead horse can never be
     * resurrected when its chunk loads.
     */
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

    /** Fallback lookup when no last-ridden entry exists: any stored horse owned by this player. */
    public @Nullable UUID findStoredHorseOwnedBy(UUID playerId) {
        for (Map.Entry<UUID, CompoundTag> entry : snapshots.entrySet()) {
            if (isOwnedBy(entry.getValue(), playerId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** Every horse this player owns that has a stored snapshot — the source list for the roster screen. */
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
        return snapshot.read("BH_Owner", UUIDUtil.CODEC).map(playerId::equals).orElse(false);
    }

    public void setActiveHorse(UUID playerId, UUID horseId) {
        activeHorseByPlayer.put(playerId, horseId);
        setDirty();
    }

    public @Nullable UUID getActiveHorseId(UUID playerId) {
        return activeHorseByPlayer.get(playerId);
    }

    /** Drops a horse from every player's active slot — called when it is disowned or dies. */
    public void clearActiveHorse(UUID horseId) {
        if (activeHorseByPlayer.values().removeIf(horseId::equals)) {
            setDirty();
        }
    }

    public void markPendingDisown(UUID horseId) {
        if (pendingDisowns.add(horseId)) {
            setDirty();
        }
    }

    /** True (once) when this horse was disowned while unloaded and still needs to be released. */
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
