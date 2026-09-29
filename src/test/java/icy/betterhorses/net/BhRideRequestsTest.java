package icy.betterhorses.net;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BhRideRequestsTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final UUID REQUESTER = UUID.randomUUID();

    @Test
    void firstRequestSends() {
        BhRideRequests requests = new BhRideRequests(new AtomicLong(0)::get);
        assertEquals(BhRideRequests.Outcome.SEND, requests.request(OWNER, REQUESTER));
    }

    @Test
    void secondRequestWithinSixtySecondsCoolsDown() {
        AtomicLong now = new AtomicLong(0);
        BhRideRequests requests = new BhRideRequests(now::get);

        assertEquals(BhRideRequests.Outcome.SEND, requests.request(OWNER, REQUESTER));
        now.set(BhRideRequests.COOLDOWN_MS - 1);
        assertEquals(BhRideRequests.Outcome.COOLDOWN, requests.request(OWNER, REQUESTER));
    }

    @Test
    void requestAfterCooldownExpirySendsAgain() {
        AtomicLong now = new AtomicLong(0);
        BhRideRequests requests = new BhRideRequests(now::get);

        assertEquals(BhRideRequests.Outcome.SEND, requests.request(OWNER, REQUESTER));
        now.set(BhRideRequests.COOLDOWN_MS);
        assertEquals(BhRideRequests.Outcome.SEND, requests.request(OWNER, REQUESTER));
    }

    @Test
    void muteBlocksRequestsForTenMinutes() {
        AtomicLong now = new AtomicLong(0);
        BhRideRequests requests = new BhRideRequests(now::get);

        requests.mute(OWNER, REQUESTER);
        assertEquals(BhRideRequests.Outcome.MUTED, requests.request(OWNER, REQUESTER));
        now.set(BhRideRequests.MUTE_MS - 1);
        assertEquals(BhRideRequests.Outcome.MUTED, requests.request(OWNER, REQUESTER));
    }

    @Test
    void muteExpiresAndFallsBackToSend() {
        AtomicLong now = new AtomicLong(0);
        BhRideRequests requests = new BhRideRequests(now::get);

        requests.mute(OWNER, REQUESTER);
        now.set(BhRideRequests.MUTE_MS);
        assertEquals(BhRideRequests.Outcome.SEND, requests.request(OWNER, REQUESTER));
    }

    @Test
    void clearWipesBothCooldownAndMute() {
        AtomicLong now = new AtomicLong(0);
        BhRideRequests requests = new BhRideRequests(now::get);

        requests.request(OWNER, REQUESTER);
        requests.mute(OWNER, REQUESTER);
        requests.clear(OWNER, REQUESTER);

        assertEquals(BhRideRequests.Outcome.SEND, requests.request(OWNER, REQUESTER));
    }

    @Test
    void muteAndCooldownAreIsolatedPerOwner() {
        AtomicLong now = new AtomicLong(0);
        BhRideRequests requests = new BhRideRequests(now::get);
        UUID otherOwner = UUID.randomUUID();

        requests.mute(OWNER, REQUESTER);
        assertEquals(BhRideRequests.Outcome.MUTED, requests.request(OWNER, REQUESTER));
        assertEquals(BhRideRequests.Outcome.SEND, requests.request(otherOwner, REQUESTER));
    }
}
