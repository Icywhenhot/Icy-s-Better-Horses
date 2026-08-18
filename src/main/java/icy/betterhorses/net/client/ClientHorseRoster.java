package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseManageAction;
import icy.betterhorses.net.network.HorseRosterEntry;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class ClientHorseRoster {

    public static final long FLASH_MS = 2600L;

    private static List<HorseRosterEntry> entries = List.of();

    private static @Nullable UUID flashHorseId;
    private static @Nullable HorseManageAction flashAction;
    private static String flashMessageKey = "";
    private static long flashExpiresAt;

    private static @Nullable UUID successHorseId;
    private static @Nullable HorseManageAction successAction;

    private ClientHorseRoster() {}

    public static void setEntries(List<HorseRosterEntry> newEntries) {
        entries = List.copyOf(newEntries);
        HorsePreviewCache.clear();
    }

    public static @Nullable HorseRosterEntry find(@Nullable UUID horseId) {
        if (horseId == null) return null;
        for (HorseRosterEntry entry : entries) {
            if (entry.horseId().equals(horseId)) return entry;
        }
        return null;
    }

    public static @Nullable UUID activeHorseId() {
        for (HorseRosterEntry entry : entries) {
            if (entry.active()) return entry.horseId();
        }
        return null;
    }

    public static List<HorseRosterEntry> entries() {
        return entries;
    }

    public static void onActionResult(UUID horseId, HorseManageAction action, boolean success, String messageKey) {
        if (success) {
            clearFlash();
            successHorseId = horseId;
            successAction = action;
            return;
        }
        successHorseId = null;
        successAction = null;
        flashHorseId = horseId;
        flashAction = action;
        flashMessageKey = messageKey;
        flashExpiresAt = System.currentTimeMillis() + FLASH_MS;
    }

    public static void clearFlash() {
        flashHorseId = null;
        flashAction = null;
        flashMessageKey = "";
        flashExpiresAt = 0L;
    }

    public static boolean isFlashing(UUID horseId, HorseManageAction action) {
        return isFlashing() && action == flashAction && horseId.equals(flashHorseId);
    }

    public static boolean isFlashing() {
        if (flashExpiresAt == 0L) return false;
        if (System.currentTimeMillis() >= flashExpiresAt) {
            clearFlash();
            return false;
        }
        return true;
    }

    public static @Nullable UUID flashHorseId() {
        return isFlashing() ? flashHorseId : null;
    }

    public static String flashMessageKey() {
        return isFlashing() ? flashMessageKey : "";
    }

    public static long flashElapsedMs() {
        return isFlashing() ? FLASH_MS - (flashExpiresAt - System.currentTimeMillis()) : -1L;
    }

    public static boolean consumeSuccess(UUID horseId, HorseManageAction action) {
        if (action != successAction || !horseId.equals(successHorseId)) {
            return false;
        }
        successHorseId = null;
        successAction = null;
        return true;
    }

    public static void reset() {
        entries = List.of();
        HorsePreviewCache.clear();
        successHorseId = null;
        successAction = null;
        clearFlash();
    }
}
