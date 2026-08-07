package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseManageAction;
import icy.betterhorses.net.network.HorseRosterEntry;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Client-side mirror of the player's horse roster, plus the most recent action result.
 *
 * <p>Failures are what the screens actually render: the button that was pressed flashes red and its
 * message shows for {@link #FLASH_MS}. Success just clears the flash — the follow-up roster sync is
 * the visible feedback.</p>
 */
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
        // Coats, ages and the roster itself can all have changed; rebuild previews on demand.
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

    /** True while this specific button should be drawn in the error colour. */
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

    /** Milliseconds since the current flash started, or -1 when nothing is flashing. Drives the shake. */
    public static long flashElapsedMs() {
        return isFlashing() ? FLASH_MS - (flashExpiresAt - System.currentTimeMillis()) : -1L;
    }

    /**
     * True exactly once, for the screen that is waiting on this specific action to go through — the
     * horse info screen uses it to close itself after the horse it was describing has been let go.
     */
    public static boolean consumeSuccess(UUID horseId, HorseManageAction action) {
        if (action != successAction || !horseId.equals(successHorseId)) {
            return false;
        }
        successHorseId = null;
        successAction = null;
        return true;
    }

    /** Called on disconnect so a new world doesn't inherit the previous one's roster. */
    public static void reset() {
        entries = List.of();
        HorsePreviewCache.clear();
        successHorseId = null;
        successAction = null;
        clearFlash();
    }
}
