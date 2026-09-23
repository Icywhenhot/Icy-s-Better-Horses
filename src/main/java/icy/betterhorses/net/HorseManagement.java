package icy.betterhorses.net;

import icy.betterhorses.net.network.HorseRosterEntry;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import icy.betterhorses.net.entity.BhBreedHorse;

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
    public static final String MSG_HOME_ELSEWHERE = MSG + "home_elsewhere";
    public static final String MSG_NO_BOND = MSG + "no_bond";
    public static final String MSG_FAILED = MSG + "failed";
    public static final String MSG_CART = MSG + "cart_attached";
    public static final String MSG_UNSAFE = MSG + "unsafe";

    public static List<HorseRosterEntry> buildRoster(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return List.of();

        UUID activeHorseId = HorseTracker.getActiveHorseId(player.getUUID());
        List<HorseRosterEntry> roster = new ArrayList<>();
        for (UUID horseId : HorseTracker.findAllStoredHorsesOwnedBy(player.getUUID())) {
            AbstractHorse loaded = HorseTracker.getLoaded(horseId);
            if (loaded == null && !storedIsOurs(horseId)) {
                continue;
            }
            if (loaded == null) {
                CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
                CompoundTag summary = snapshot == null || !snapshot.contains("BH_Roster")
                        ? null : snapshot.getCompound("BH_Roster");
                HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
                if (summary != null && known != null) {
                    roster.add(new HorseRosterEntry(horseId, summary.getString("name"),
                            bh_summaryBreed(summary), summary.getString("gender"),
                            summary.getBoolean("mixed"), summary.getInt("bond"), false,
                            summary.getBoolean("home"), horseId.equals(activeHorseId),
                            known.dimension().location().toString(), known.pos(), summary.getString("type"),
                            summary.contains("variant") ? summary.getInt("variant") : -1,
                            summary.contains("markings") ? summary.getInt("markings") : -1,
                            summary.getBoolean("baby"), summary.contains("coat") ? summary.getInt("coat") : -1));
                    continue;
                }
            }
            AbstractHorse horse = loaded != null ? loaded : materialize(server, horseId);
            if (horse == null) continue;

            IHorseData data = IHorseData.of(horse);
            HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
            String dimension = loaded != null
                    ? loaded.level().dimension().location().toString()
                    : (known == null ? "" : known.dimension().location().toString());
            BlockPos pos = loaded != null
                    ? loaded.blockPosition()
                    : (known == null ? horse.blockPosition() : known.pos());

            roster.add(new HorseRosterEntry(
                    horseId,
                    horse.hasCustomName() ? horse.getCustomName().getString() : "",
                    data.bh_getBreedKey() != null ? data.bh_getBreedKey().location().toString() : data.bh_getBreed().id(),
                    data.bh_getGender().location().toString(),
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
                    horse instanceof BhBreedHorse breedHorse
                            ? breedHorse.bhCoat() : -1));
            if (loaded == null) {
                HorseTrackerState.get(server).recordHorse(horse);
                release(horse);
            }
        }
        roster.sort(Comparator
                .comparing((HorseRosterEntry entry) -> entry.customName().isEmpty())
                .thenComparing(entry -> entry.customName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(HorseRosterEntry::horseId));
        return roster;
    }

    private static boolean storedIsOurs(UUID horseId) {
        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        return snapshot != null && BhHorseKind.managedId(snapshot.getString("id"));
    }

    private static String bh_summaryBreed(CompoundTag summary) {
        String id = summary.getString("breedId");
        if (!id.isEmpty()) {
            return id;
        }
        return HorseBreed.fromId(summary.getInt("breed")).id();
    }

    public static Outcome whistle(ServerPlayer player, UUID horseId) {
        if (!ownsStoredHorse(player, horseId)) return Outcome.fail(MSG_GONE);
        if (hasCart(player, horseId)) return Outcome.fail(MSG_CART);
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return Outcome.fail(MSG_FAILED);

        AbstractHorse loaded = findLoadedOwned(player, horseId);
        if (loaded != null) {
            if (loaded.level() != player.level()) {
                return Outcome.fail(MSG_OTHER_DIMENSION);
            }
            IcysBetterHorses.LOGGER.info("[whistle] {} calls horse {}: body is loaded in {} at {} {} {}, teleporting it",
                    player.getName().getString(), horseId, loaded.level().dimension().location(),
                    loaded.getBlockX(), loaded.getBlockY(), loaded.getBlockZ());
            return summonToPlayer(loaded, player);
        }

        HorseTrackerState.KnownPosition seen = HorseTracker.getLastKnownPosition(horseId);
        IcysBetterHorses.LOGGER.info("[whistle] {} calls horse {}: no loaded body, last seen in {} at {}, respawning from snapshot",
                player.getName().getString(), horseId,
                seen == null ? "nowhere" : seen.dimension().location(), seen == null ? "-" : seen.pos());

        ServerLevel level = (ServerLevel) player.level();
        AbstractHorse respawned = respawnFromSnapshot(
                server, horseId, level, player.getX(), player.getY(), player.getZ());
        if (respawned == null) {
            return Outcome.fail(respawnFailureKey(player, horseId));
        }
        IHorseData.of(respawned).bh_setCommand(BhContent.COMMAND_FOLLOW.getKey());
        return Outcome.OK;
    }

    public static Outcome sendHome(ServerPlayer player, UUID horseId) {
        if (!ownsStoredHorse(player, horseId)) return Outcome.fail(MSG_GONE);
        if (hasCart(player, horseId)) return Outcome.fail(MSG_CART);
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return Outcome.fail(MSG_FAILED);

        AbstractHorse loaded = findLoadedOwned(player, horseId);
        if (loaded != null) {
            BlockPos home = IHorseData.of(loaded).bh_getHome();
            if (home == null) return Outcome.fail(MSG_NO_HOME);
            ResourceKey<Level> homeDim = IHorseData.of(loaded).bh_getHomeDimension();
            if (homeDim != null && !homeDim.equals(loaded.level().dimension())) {
                return Outcome.fail(MSG_HOME_ELSEWHERE);
            }

            keepHomeChunkLoaded((ServerLevel) loaded.level(), home);
            if (!HorsePlacement.teleport(loaded, home)) return Outcome.fail(MSG_UNSAFE);
            IHorseData.of(loaded).bh_setCommand(BhContent.COMMAND_STAY.getKey());
            return Outcome.OK;
        }

        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        if (snapshot == null || known == null) return Outcome.fail(MSG_GONE);

        BlockPos home = BhHorseStorage.readLegacyBlockPos(snapshot, "BH_Home");
        if (home == null) return Outcome.fail(MSG_NO_HOME);

        ResourceKey<Level> homeDim = snapshot.contains("BH_HomeDim")
                ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(snapshot.getString("BH_HomeDim")))
                : known.dimension();
        if (!homeDim.equals(known.dimension())) return Outcome.fail(MSG_HOME_ELSEWHERE);

        ServerLevel homeLevel = server.getLevel(homeDim);
        if (homeLevel == null) return Outcome.fail(MSG_FAILED);

        keepHomeChunkLoaded(homeLevel, home);
        AbstractHorse respawned = respawnFromSnapshot(
                server, horseId, homeLevel, home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);
        if (respawned == null) return Outcome.fail(MSG_UNSAFE);

        IHorseData.of(respawned).bh_setCommand(BhContent.COMMAND_STAY.getKey());
        return Outcome.OK;
    }

    private static void keepHomeChunkLoaded(ServerLevel level, BlockPos home) {
        ChunkPos chunk = new ChunkPos(home);
        level.getChunkSource().addRegionTicket(ModTicketTypes.HORSE_TASK, chunk, 1, chunk);
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
            if (IHorseData.of(loaded).bh_hasAnyEquipment()) {
                return Outcome.fail(MSG_HAS_EQUIPMENT);
            }
            IHorseData.of(loaded).bh_disown();
            HorseTracker.clearActiveHorse(horseId);
            IcysBetterHorses.LOGGER.info("[manage] {} disowned loaded horse {}",
                    player.getName().getString(), horseId);
            return Outcome.OK;
        }

        if (!ownsStoredHorse(player, horseId)) {
            return Outcome.fail(MSG_GONE);
        }

        AbstractHorse scratch = materialize(server, horseId);
        if (scratch == null) return Outcome.fail(MSG_GONE);
        boolean carrying = IHorseData.of(scratch).bh_hasAnyEquipment();
        release(scratch);
        if (carrying) {
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
            announce(player, whistle(player, activeHorseId));
            return;
        }

        AbstractHorse horse = findCallableHorse(player, playerId);
        if (horse != null) {
            IcysBetterHorses.LOGGER.debug("[whistle] {} whistled: summoning loaded horse {}",
                    player.getName().getString(), horse.getUUID());
            announce(player, whistle(player, horse.getUUID()));
            return;
        }

        IcysBetterHorses.LOGGER.debug("[whistle] {} whistled: no loaded horse found, trying stored respawn",
                player.getName().getString());

        UUID horseId = HorseTracker.getLastRiddenId(playerId);
        if (horseId == null || HorseTracker.getSnapshot(horseId) == null) {
            horseId = HorseTracker.findStoredHorseOwnedBy(playerId);
        }
        if (horseId == null) {
            IcysBetterHorses.LOGGER.debug("[whistle] no stored horse found for {}", playerId);
            return;
        }
        announce(player, whistle(player, horseId));
    }

    private static void announce(ServerPlayer player, Outcome outcome) {
        if (outcome.ok()) return;
        IcysBetterHorses.LOGGER.info("[whistle] {} got {}", player.getName().getString(), outcome.messageKey());
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(outcome.messageKey()));
    }

    private static Outcome summonToPlayer(AbstractHorse horse, ServerPlayer player) {
        IHorseData data = IHorseData.of(horse);
        if (data.bh_hasCartGear()) return Outcome.fail(MSG_CART);
        if (data.bh_getBond() <= 0) return Outcome.fail(MSG_NO_BOND);
        if (horse.distanceToSqr(player) > CALL_TELEPORT_DIST_SQ
                && !HorsePlacement.teleport(horse, player.blockPosition())) return Outcome.fail(MSG_UNSAFE);
        data.bh_setCommand(BhContent.COMMAND_FOLLOW.getKey());
        return Outcome.OK;
    }

    private static boolean hasCart(ServerPlayer player, UUID id) {
        AbstractHorse horse = HorseTracker.getLoaded(id);
        if (horse != null) return IHorseData.of(horse).bh_hasCartGear();
        CompoundTag tag = HorseTracker.getSnapshot(id);
        if (tag == null) return false;
        return BhHorseStorage.contains(tag, "BH_Gear",
                new net.minecraft.world.item.ItemStack(ModItems.HORSE_CART.get()));
    }

    private static @Nullable AbstractHorse findCallableHorse(ServerPlayer player, UUID playerId) {
        AbstractHorse lastRidden = HorseTracker.getLastRidden(playerId);
        if (lastRidden != null
                && playerId.equals(IHorseData.of(lastRidden).bh_getOwner())
                && lastRidden.level() == player.level()
                && lastRidden.isAlive()) {
            return lastRidden;
        }

        AbstractHorse nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (AbstractHorse candidate : HorseTracker.getAll()) {
            if (!candidate.isAlive() || candidate.level() != player.level()) continue;
            if (!playerId.equals(IHorseData.of(candidate).bh_getOwner())) continue;
            double distSq = candidate.distanceToSqr(player);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static @Nullable AbstractHorse anyLevel(ServerPlayer player, UUID horseId) {
        for (ServerLevel level : player.server.getAllLevels()) {
            if (level.getEntity(horseId) instanceof AbstractHorse horse) {
                return horse;
            }
        }
        return null;
    }

    private static @Nullable AbstractHorse findLoadedOwned(ServerPlayer player, UUID horseId) {
        AbstractHorse tracked = HorseTracker.getLoaded(horseId);
        AbstractHorse found = anyLevel(player, horseId);
        if (tracked == null && found != null) {
            tracked = found;
            if (player.getUUID().equals(IHorseData.of(found).bh_getOwner())) {
                HorseTracker.register(found);
            }
        }
        if (tracked == null || !tracked.isAlive()) return null;
        return player.getUUID().equals(IHorseData.of(tracked).bh_getOwner()) ? tracked : null;
    }

    private static boolean ownsStoredHorse(ServerPlayer player, UUID horseId) {
        AbstractHorse tracked = HorseTracker.getLoaded(horseId);
        if (tracked != null && tracked.isAlive()) return player.getUUID().equals(IHorseData.of(tracked).bh_getOwner());
        AbstractHorse horse = anyLevel(player, horseId);
        if (horse != null && horse.isAlive()) {
            return player.getUUID().equals(IHorseData.of(horse).bh_getOwner());
        }
        AbstractHorse loaded = findLoadedOwned(player, horseId);
        if (loaded != null) return true;
        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        return snapshot != null && snapshot.hasUUID("BH_Owner")
                && player.getUUID().equals(snapshot.getUUID("BH_Owner"));
    }

    private static @Nullable AbstractHorse materialize(MinecraftServer server, UUID horseId) {
        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        if (snapshot == null) return null;

        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        ServerLevel level = known == null ? null : server.getLevel(known.dimension());
        if (level == null) level = server.overworld();

        Entity entity = EntityType.loadEntityRecursive(snapshot, level, loaded -> loaded);
        if (!(entity instanceof AbstractHorse horse)) return null;

        horse.setId(scratchIds.decrementAndGet());
        return horse;
    }

    private static void discardOldBody(MinecraftServer server, AbstractHorse fresh) {
        for (ServerLevel level : server.getAllLevels()) {
            if (!(level.getEntity(fresh.getUUID()) instanceof AbstractHorse old) || old == fresh) {
                continue;
            }
            IcysBetterHorses.LOGGER.info("[whistle] discarding old body of horse {} in {}",
                    fresh.getUUID(), level.dimension().location());
            old.ejectPassengers();
            old.discard();
        }
    }

    private static void release(AbstractHorse scratch) {
        for (Entity rider : scratch.getIndirectPassengers()) {
            rider.discard();
        }
        scratch.discard();
    }

    private static @Nullable AbstractHorse respawnFromSnapshot(
            MinecraftServer server, UUID horseId, ServerLevel level, double x, double y, double z) {
        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        if (known == null || snapshot == null) {
            IcysBetterHorses.LOGGER.debug("[whistle] horse {} has no stored {} — cannot respawn",
                    horseId, snapshot == null ? "snapshot" : "position");
            return null;
        }
        if (!level.dimension().equals(known.dimension())) {
            IcysBetterHorses.LOGGER.debug("[whistle] horse {} is in {}, target level is {} — not respawning",
                    horseId, known.dimension().location(), level.dimension().location());
            return null;
        }
        if (snapshot.getInt("BH_Bond") <= 0) {
            IcysBetterHorses.LOGGER.debug("[whistle] horse {} has no bond — not respawning", horseId);
            return null;
        }

        if (!BhHorseKind.managedId(snapshot.getString("id"))) {
            IcysBetterHorses.LOGGER.debug("[whistle] horse {} is a {}, not one of ours — not respawning",
                    horseId, snapshot.getString("id"));
            return null;
        }

        Entity loaded = EntityType.loadEntityRecursive(snapshot, level, entity -> entity);
        if (!(loaded instanceof AbstractHorse horse)) {
            IcysBetterHorses.LOGGER.warn("[whistle] snapshot of horse {} did not deserialize to a horse", horseId);
            return null;
        }

        int newGeneration = HorseTracker.getGeneration(horseId) + 1;
        IHorseData.of(horse).bh_setGeneration(newGeneration);
        var at = HorsePlacement.find(horse, BlockPos.containing(x, y, z));
        if (at == null) {
            release(horse);
            return null;
        }
        horse.moveTo(at.x, at.y, at.z, horse.getYRot(), horse.getXRot());
        horse.fallDistance = 0.0F;

        if (!level.tryAddFreshEntityWithPassengers(horse)) {
            IcysBetterHorses.LOGGER.warn("[whistle] failed to spawn respawned horse {}", horseId);
            return null;
        }
        HorseTracker.setGeneration(horseId, newGeneration);
        discardOldBody(server, horse);
        if (IHorseData.of(horse).bh_isOwned()) HorseTracker.register(horse);
        IcysBetterHorses.LOGGER.info("[whistle] respawned horse {} at {} {} {} in {} (generation {})",
                horseId, x, y, z, level.dimension().location(), newGeneration);
        return horse;
    }

    private static String respawnFailureKey(ServerPlayer player, UUID horseId) {
        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        if (known == null || snapshot == null) {
            return MSG_GONE;
        }
        if (!player.level().dimension().equals(known.dimension())) {
            return MSG_OTHER_DIMENSION;
        }
        return snapshot.getInt("BH_Bond") <= 0 ? MSG_NO_BOND : MSG_UNSAFE;
    }
}
