package icy.betterhorses.net;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public final class BhWagonHitch {

    public static final String TAG = IcysBetterHorses.RESOURCE_NAMESPACE + ":wagon_hitched";
    private static final String WAGON_TAG = IcysBetterHorses.RESOURCE_NAMESPACE + ":wagon=";
    private static final int CHECK_TICKS = 20;

    private static @Nullable Field horseUuids;
    private static boolean lookedUp;

    private BhWagonHitch() {}

    public static boolean hitched(Mob horse) {
        if (!horse.getTags().contains(TAG)) {
            return false;
        }
        if (horse.isNoAi()) {
            return true;
        }
        forget(horse);
        return false;
    }

    public static void mark(Mob horse, Entity wagon) {
        forget(horse);
        horse.addTag(TAG);
        horse.addTag(WAGON_TAG + wagon.getUUID());
    }

    public static void tick(Mob horse) {
        if (horse.tickCount % CHECK_TICKS != 0 || !(horse.level() instanceof ServerLevel level)
                || !horse.getTags().contains(TAG)) {
            return;
        }
        if (!horse.isNoAi()) {
            forget(horse);
            return;
        }
        UUID id = wagonId(horse);
        Entity wagon = id == null ? null : level.getEntity(id);
        if (wagon == null || wagon.isRemoved() || !holds(wagon, horse)) {
            horse.setNoAi(false);
            forget(horse);
        }
    }

    private static void forget(Mob horse) {
        for (String tag : List.copyOf(horse.getTags())) {
            if (tag.equals(TAG) || tag.startsWith(WAGON_TAG)) {
                horse.removeTag(tag);
            }
        }
    }

    private static @Nullable UUID wagonId(Mob horse) {
        for (String tag : horse.getTags()) {
            if (tag.startsWith(WAGON_TAG)) {
                try {
                    return UUID.fromString(tag.substring(WAGON_TAG.length()));
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static boolean holds(Entity wagon, Mob horse) {
        Field field = horseUuidsField(wagon.getClass());
        if (field == null) {
            return true;
        }
        try {
            for (Object uuid : (Object[]) field.get(wagon)) {
                if (horse.getUUID().equals(uuid)) {
                    return true;
                }
            }
            return false;
        } catch (IllegalAccessException | ClassCastException e) {
            return true;
        }
    }

    private static @Nullable Field horseUuidsField(Class<?> type) {
        if (lookedUp) {
            return horseUuids;
        }
        lookedUp = true;
        for (Class<?> c = type; c != null && c != Entity.class; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField("horseUuids");
                field.setAccessible(true);
                horseUuids = field;
                break;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return horseUuids;
    }
}
