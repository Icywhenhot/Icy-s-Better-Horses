package icy.betterhorses.net;

import icy.betterhorses.net.network.HorseRosterEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.ChunkPos;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class HorseManagement {

    private static final double CALL_TELEPORT_DIST_SQ = 32.0 * 32.0;

    private static final AtomicInteger scratchIds = new AtomicInteger();

    private HorseManagement() {}

    public record Outcome(boolean ok, String messageKey) {
        public static final Outcome OK = new Outcome(true, "");

        public static Outcome fail(String messageKey) {
            return new Outcome(false, messageKey);
        }
    }

    private static final String MSG = "message.icys-better-horses.manage.";
    public static final String MSG_GONE = MSG + "gone";
    public static final String MSG_NO_HOME = MSG + "no_home";
    public static final String MSG_HAS_EQUIPMENT = MSG + "has_equipment";
    public static final String MSG_OTHER_DIMENSION = MSG + "other_dimension";
    public static final String MSG_FAILED = MSG + "failed";

    public static List<HorseRosterEntry> buildRoster(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return List.of();

        UUID activeHorseId = HorseTracker.getActiveHorseId(player.getUUID());
        List<HorseRosterEntry> roster = new ArrayList<>();
        for (UUID horseId : HorseTracker.findAllStoredHorsesOwnedBy(player.getUUID())) {
            AbstractHorse loaded = HorseTracker.getLoaded(horseId);
            AbstractHorse horse = loaded != null ? loaded : materialize(server, horseId);
            if (horse == null) continue;

            IHorseData data = (IHorseData) horse;
            HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
            String dimension = loaded != null
                    ? loaded.level().dimension().identifier().toString()
                    : (known == null ? "" : known.dimension().identifier().toString());
            BlockPos pos = loaded != null
                    ? loaded.blockPosition()
                    : (known == null ? horse.blockPosition() : known.pos());

            roster.add(new HorseRosterEntry(
                    horseId,
                    horse.hasCustomName() ? horse.getCustomName().getString() : "",
                    data.bh_getBreed().ordinal(),
                    data.bh_getGender().ordinal(),
                    data.bh_isMixedBreed(),
                    data.bh_getBond(),
                    loaded != null,
                    data.bh_getHome() != null,
                    horseId.equals(activeHorseId),
                    dimension,
                    pos,
                    EntityType.getKey(horse.getType()).toString(),
                    horse instanceof Horse coloured ? coloured.getVariant().ordinal() : -1,
                    horse instanceof Horse coloured ? coloured.getMarkings().ordinal() : -1,
                    horse.isBaby(),
                    horse instanceof icy.betterhorses.net.entity.BhBreedHorse breedHorse
                            ? breedHorse.bhCoat() : -1));
        }
        roster.sort(Comparator
                .comparing((HorseRosterEntry entry) -> entry.customName().isEmpty())
                .thenComparing(entry -> entry.customName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(HorseRosterEntry::horseId));
        return roster;
    }

    public static Outcome whistle(ServerPlayer player, UUID horseId) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return Outcome.fail(MSG_FAILED);

        AbstractHorse loaded = findLoadedOwned(player, horseId);
        if (loaded != null) {
            if (loaded.level() != player.level()) {
                return Outcome.fail(MSG_OTHER_DIMENSION);
            }
            summonToPlayer(loaded, player);
            return Outcome.OK;
        }

        ServerLevel level = (ServerLevel) player.level();
        AbstractHorse respawned = respawnFromSnapshot(
                server, horseId, level, player.getX(), player.getY(), player.getZ());
        if (respawned == null) {
            return Outcome.fail(respawnFailureKey(player, horseId));
        }
        ((IHorseData) respawned).bh_setCommand(HorseCommand.FOLLOW);
        return Outcome.OK;
    }

    public static Outcome sendHome(ServerPlayer player, UUID horseId) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return Outcome.fail(MSG_FAILED);

        AbstractHorse loaded = findLoadedOwned(player, horseId);
        if (loaded != null) {
            BlockPos home = ((IHorseData) loaded).bh_getHome();
            if (home == null) return Outcome.fail(MSG_NO_HOME);

            loaded.ejectPassengers();
            keepHomeChunkLoaded((ServerLevel) loaded.level(), home);
            loaded.teleportTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);
            loaded.fallDistance = 0.0F;
            ((IHorseData) loaded).bh_setCommand(HorseCommand.STAY);
            return Outcome.OK;
        }

        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        if (snapshot == null || known == null) return Outcome.fail(MSG_GONE);

        BlockPos home = snapshot.read("BH_Home", BlockPos.CODEC).orElse(null);
        if (home == null) return Outcome.fail(MSG_NO_HOME);

        ServerLevel homeLevel = server.getLevel(known.dimension());
        if (homeLevel == null) return Outcome.fail(MSG_FAILED);

        keepHomeChunkLoaded(homeLevel, home);
        AbstractHorse respawned = respawnFromSnapshot(
                server, horseId, homeLevel, home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);
        if (respawned == null) return Outcome.fail(MSG_FAILED);

        ((IHorseData) respawned).bh_setCommand(HorseCommand.STAY);
        return Outcome.OK;
    }

    private static void keepHomeChunkLoaded(ServerLevel level, BlockPos home) {
        level.getChunkSource().addTicketWithRadius(ModTicketTypes.HORSE_TASK, ChunkPos.containing(home), 1);
    }

    public static Outcome setActive(ServerPlayer player, UUID horseId) {
        if (!ownsStoredHorse(player, horseId)) {
            return Outcome.fail(MSG_GONE);
        }
        HorseTracker.setActiveHorse(player.getUUID(), horseId);
        return Outcome.OK;
    }

    public static Outcome disown(ServerPlayer player, UUID horseId) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return Outcome.fail(MSG_FAILED);

        AbstractHorse loaded = findLoadedOwned(player, horseId);
        if (loaded != null) {
            if (((IHorseData) loaded).bh_hasAnyEquipment()) {
                return Outcome.fail(MSG_HAS_EQUIPMENT);
            }
            ((IHorseData) loaded).bh_disown();
            HorseTracker.clearActiveHorse(horseId);
            IcysBetterHorses.LOGGER.info("[manage] {} disowned loaded horse {}",
                    player.getName().getString(), horseId);
            return Outcome.OK;
        }

        if (!ownsStoredHorse(player, horseId)) {
            return Outcome.fail(MSG_GONE);
        }

        AbstractHorse snapshotHorse = materialize(server, horseId);
        if (snapshotHorse == null) return Outcome.fail(MSG_GONE);
        if (((IHorseData) snapshotHorse).bh_hasAnyEquipment()) {
            return Outcome.fail(MSG_HAS_EQUIPMENT);
        }

        HorseTracker.markPendingDisown(horseId);
        HorseTracker.forgetStoredHorse(horseId);
        HorseTracker.clearActiveHorse(horseId);
        IcysBetterHorses.LOGGER.info("[manage] {} disowned unloaded horse {} (release queued)",
                player.getName().getString(), horseId);
        return Outcome.OK;
    }

    public static void callNearestHorse(ServerPlayer player) {
        UUID playerId = player.getUUID();

        UUID activeHorseId = HorseTracker.getActiveHorseId(playerId);
        if (activeHorseId != null && HorseTracker.findAllStoredHorsesOwnedBy(playerId).contains(activeHorseId)) {
            whistle(player, activeHorseId);
            return;
        }

        AbstractHorse horse = findCallableHorse(player, playerId);
        if (horse != null) {
            IcysBetterHorses.LOGGER.info("[whistle] {} whistled: summoning loaded horse {}",
                    player.getName().getString(), horse.getUUID());
            summonToPlayer(horse, player);
            return;
        }

        IcysBetterHorses.LOGGER.info("[whistle] {} whistled: no loaded horse found, trying stored respawn",
                player.getName().getString());

        UUID horseId = HorseTracker.getLastRiddenId(playerId);
        if (horseId == null || HorseTracker.getSnapshot(horseId) == null) {
            horseId = HorseTracker.findStoredHorseOwnedBy(playerId);
        }
        if (horseId == null) {
            IcysBetterHorses.LOGGER.info("[whistle] no stored horse found for {}", playerId);
            return;
        }
        whistle(player, horseId);
    }

    public static void summonToPlayer(AbstractHorse horse, ServerPlayer player) {
        IHorseData data = (IHorseData) horse;
        if (data.bh_getBond() <= 0) return;

        data.bh_setCommand(HorseCommand.FOLLOW);

        BlockPos target = player.blockPosition();
        if (horse.distanceToSqr(player) > CALL_TELEPORT_DIST_SQ) {
            horse.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        }
    }

    private static @Nullable AbstractHorse findCallableHorse(ServerPlayer player, UUID playerId) {
        AbstractHorse lastRidden = HorseTracker.getLastRidden(playerId);
        if (lastRidden != null
                && playerId.equals(((IHorseData) lastRidden).bh_getOwner())
                && lastRidden.level() == player.level()
                && lastRidden.isAlive()) {
            return lastRidden;
        }

        AbstractHorse nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (AbstractHorse candidate : HorseTracker.getAll()) {
            if (!candidate.isAlive() || candidate.level() != player.level()) continue;
            if (!playerId.equals(((IHorseData) candidate).bh_getOwner())) continue;
            double distSq = candidate.distanceToSqr(player);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static @Nullable AbstractHorse findLoadedOwned(ServerPlayer player, UUID horseId) {
        AbstractHorse tracked = HorseTracker.getLoaded(horseId);
        if (tracked == null && player.level().getEntityInAnyDimension(horseId) instanceof AbstractHorse found) {
            tracked = found;
            if (player.getUUID().equals(((IHorseData) found).bh_getOwner())) {
                HorseTracker.register(found);
            }
        }
        if (tracked == null || !tracked.isAlive()) return null;
        return player.getUUID().equals(((IHorseData) tracked).bh_getOwner()) ? tracked : null;
    }

    private static boolean ownsStoredHorse(ServerPlayer player, UUID horseId) {
        return HorseTracker.findAllStoredHorsesOwnedBy(player.getUUID()).contains(horseId);
    }

    private static @Nullable AbstractHorse materialize(MinecraftServer server, UUID horseId) {
        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        if (snapshot == null) return null;

        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        ServerLevel level = known == null ? null : server.getLevel(known.dimension());
        if (level == null) level = server.overworld();

        Entity entity = EntityType.loadEntityRecursive(
                snapshot, level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true), EntityProcessor.NOP);
        if (!(entity instanceof AbstractHorse horse)) return null;

        horse.setId(scratchIds.decrementAndGet());
        return horse;
    }

    private static @Nullable AbstractHorse respawnFromSnapshot(
            MinecraftServer server, UUID horseId, ServerLevel level, double x, double y, double z) {
        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        if (known == null || snapshot == null) {
            IcysBetterHorses.LOGGER.info("[whistle] horse {} has no stored {} — cannot respawn",
                    horseId, snapshot == null ? "snapshot" : "position");
            return null;
        }
        if (!level.dimension().equals(known.dimension())) {
            IcysBetterHorses.LOGGER.info("[whistle] horse {} is in {}, target level is {} — not respawning",
                    horseId, known.dimension().identifier(), level.dimension().identifier());
            return null;
        }
        if (snapshot.getIntOr("BH_Bond", 0) <= 0) {
            IcysBetterHorses.LOGGER.info("[whistle] horse {} has no bond — not respawning", horseId);
            return null;
        }

        Entity loaded = EntityType.loadEntityRecursive(
                snapshot, level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true), EntityProcessor.NOP);
        if (!(loaded instanceof AbstractHorse horse)) {
            IcysBetterHorses.LOGGER.warn("[whistle] snapshot of horse {} did not deserialize to a horse", horseId);
            return null;
        }

        int newGeneration = HorseTracker.getGeneration(horseId) + 1;
        ((IHorseData) horse).bh_setGeneration(newGeneration);
        horse.snapTo(x, y, z, horse.getYRot(), horse.getXRot());
        horse.fallDistance = 0.0F;

        if (!level.addFreshEntity(horse)) {
            IcysBetterHorses.LOGGER.warn("[whistle] failed to spawn respawned horse {}", horseId);
            return null;
        }
        HorseTracker.setGeneration(horseId, newGeneration);
        IcysBetterHorses.LOGGER.info("[whistle] respawned horse {} at {} {} {} in {} (generation {})",
                horseId, x, y, z, level.dimension().identifier(), newGeneration);
        return horse;
    }

    private static String respawnFailureKey(ServerPlayer player, UUID horseId) {
        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        if (known == null || HorseTracker.getSnapshot(horseId) == null) {
            return MSG_GONE;
        }
        return player.level().dimension().equals(known.dimension()) ? MSG_FAILED : MSG_OTHER_DIMENSION;
    }
}
