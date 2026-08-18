package icy.betterhorses.net.client;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.mixin.HorseAccessor;
import icy.betterhorses.net.network.HorseRosterEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class HorsePreviewCache {

    private static final Map<UUID, AbstractHorse> cache = new HashMap<>();

    private static final AtomicInteger previewIds = new AtomicInteger();

    private HorsePreviewCache() {}

    private static final Set<UUID> broken = new HashSet<>();

    public static @Nullable AbstractHorse getOrBuild(HorseRosterEntry entry) {
        if (broken.contains(entry.horseId())) return null;

        AbstractHorse cached = cache.get(entry.horseId());
        if (cached != null) return cached;

        AbstractHorse built = build(entry);
        if (built != null) {
            cache.put(entry.horseId(), built);
        }
        return built;
    }

    public static void markBroken(UUID horseId, Throwable error) {
        if (broken.add(horseId)) {
            cache.remove(horseId);
            IcysBetterHorses.LOGGER.warn("[manage] preview render failed for horse {}, hiding it", horseId, error);
        }
    }

    private static @Nullable AbstractHorse build(HorseRosterEntry entry) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return null;

        Identifier typeId = Identifier.tryParse(entry.entityTypeId());
        if (typeId == null) return null;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(typeId);
        if (type == null) return null;

        Entity raw;
        try {
            raw = type.create(minecraft.level, EntitySpawnReason.LOAD);
        } catch (Exception e) {
            IcysBetterHorses.LOGGER.warn("[manage] could not build a preview for {}: {}",
                    entry.entityTypeId(), e.toString());
            return null;
        }
        if (!(raw instanceof AbstractHorse horse)) return null;

        horse.setId(previewIds.decrementAndGet());
        if (horse instanceof Horse coloured && entry.variantOrdinal() >= 0) {
            ((HorseAccessor) coloured).bh_setVariantAndMarkings(
                    byOrdinal(Variant.values(), entry.variantOrdinal(), Variant.WHITE),
                    byOrdinal(Markings.values(), entry.markingsOrdinal(), Markings.NONE));
        }
        if (horse instanceof icy.betterhorses.net.entity.BhBreedHorse breedHorse
                && entry.breedCoat() >= 0) {
            breedHorse.bhSetCoat(entry.breedCoat());
        }
        horse.setBaby(entry.baby());
        horse.setCustomNameVisible(false);
        return horse;
    }

    private static <T> T byOrdinal(T[] values, int ordinal, T fallback) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    public static void clear() {
        cache.clear();
        broken.clear();
    }
}
