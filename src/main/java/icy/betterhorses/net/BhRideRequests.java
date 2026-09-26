package icy.betterhorses.net;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

// Anti-spam for ask-to-ride: server memory only, keyed per (owner, requester) pair.
public final class BhRideRequests {

    public static final long COOLDOWN_MS = 60_000L;
    public static final long MUTE_MS = 10 * 60_000L;

    public enum Outcome { SEND, COOLDOWN, MUTED }

    private static BhRideRequests instance = new BhRideRequests();

    public static BhRideRequests instance() {
        return instance;
    }

    // Cleared on server stop.
    public static void reset() {
        instance = new BhRideRequests();
    }

    private final LongSupplier clock;
    private final Map<Pair, Long> cooldownUntil = new ConcurrentHashMap<>();
    private final Map<Pair, Long> muteUntil = new ConcurrentHashMap<>();

    public BhRideRequests() {
        this(System::currentTimeMillis);
    }

    public BhRideRequests(LongSupplier clock) {
        this.clock = clock;
    }

    // Decides whether a fresh ask should go out, recording it if so.
    public Outcome request(UUID ownerId, UUID requesterId) {
        Pair pair = new Pair(ownerId, requesterId);
        long now = clock.getAsLong();

        Long mutedTill = muteUntil.get(pair);
        if (mutedTill != null) {
            if (mutedTill > now) return Outcome.MUTED;
            muteUntil.remove(pair, mutedTill);
        }

        Long cooldownTill = cooldownUntil.get(pair);
        if (cooldownTill != null && cooldownTill > now) {
            return Outcome.COOLDOWN;
        }

        cooldownUntil.put(pair, now + COOLDOWN_MS);
        return Outcome.SEND;
    }

    // /horse deny: mute a requester for this owner, without touching anyone else's list.
    public void mute(UUID ownerId, UUID requesterId) {
        muteUntil.put(new Pair(ownerId, requesterId), clock.getAsLong() + MUTE_MS);
    }

    // Granting trust wipes any cooldown/mute standing in the way of the new rider.
    public void clear(UUID ownerId, UUID requesterId) {
        Pair pair = new Pair(ownerId, requesterId);
        cooldownUntil.remove(pair);
        muteUntil.remove(pair);
    }

    private record Pair(UUID ownerId, UUID requesterId) {
        private Pair {
            Objects.requireNonNull(ownerId);
            Objects.requireNonNull(requesterId);
        }
    }
}
