package icy.betterhorses.net.gametest;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class BhTestPlayers {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private BhTestPlayers() {}

    public static ServerPlayer at(GameTestHelper helper, Vec3 relativePos) {
        return at(helper, relativePos, UUID.randomUUID());
    }

    public static ServerPlayer owner(GameTestHelper helper, Vec3 relativePos, UUID ownerUuid) {
        return at(helper, relativePos, ownerUuid);
    }

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
