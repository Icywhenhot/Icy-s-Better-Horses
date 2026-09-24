package icy.betterhorses.net.gametest;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

// Real ServerPlayers (via Fabric's FakePlayer) for driving server-side C2S handlers directly, the
// way the network receiver would. GameTestHelper's own makeMockPlayer() is NOT a real ServerPlayer
// and can't stand in for handler methods that require one (player.server, player.getUUID() owner
// checks, etc). Each call gets a fresh random UUID/name so distinct "players" never alias each
// other, and so FakePlayer's own (world, profile) instance cache never hands back a stale player
// left over from an earlier test on the same world.
public final class BhTestPlayers {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private BhTestPlayers() {}

    public static ServerPlayer at(GameTestHelper helper, Vec3 relativePos) {
        return at(helper, relativePos, UUID.randomUUID());
    }

    public static ServerPlayer owner(GameTestHelper helper, Vec3 relativePos, UUID ownerUuid) {
        return at(helper, relativePos, ownerUuid);
    }

    // A player whose FakePlayer lives directly in the given level (e.g. a different dimension
    // than the horse under test), at an absolute position in that level.
    public static ServerPlayer atLevel(ServerLevel level, UUID uuid, Vec3 absolutePos) {
        GameProfile profile = new GameProfile(uuid, "bh-test-" + COUNTER.incrementAndGet());
        ServerPlayer player = FakePlayer.get(level, profile);
        player.setPos(absolutePos);
        return player;
    }

    private static ServerPlayer at(GameTestHelper helper, Vec3 relativePos, UUID uuid) {
        ServerLevel level = helper.getLevel();
        GameProfile profile = new GameProfile(uuid, "bh-test-" + COUNTER.incrementAndGet());
        ServerPlayer player = FakePlayer.get(level, profile);
        player.setPos(helper.absoluteVec(relativePos));
        return player;
    }
}
