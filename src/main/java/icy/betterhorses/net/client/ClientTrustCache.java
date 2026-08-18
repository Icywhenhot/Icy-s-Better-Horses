package icy.betterhorses.net.client;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientTrustCache {

    private static final Set<UUID> TRUSTING_OWNERS = ConcurrentHashMap.newKeySet();

    private ClientTrustCache() {}

    public static void set(List<UUID> owners) {
        TRUSTING_OWNERS.clear();
        TRUSTING_OWNERS.addAll(owners);
    }

    public static boolean isTrustedBy(UUID ownerId) {
        return ownerId != null && TRUSTING_OWNERS.contains(ownerId);
    }

    public static void reset() {
        TRUSTING_OWNERS.clear();
    }
}
