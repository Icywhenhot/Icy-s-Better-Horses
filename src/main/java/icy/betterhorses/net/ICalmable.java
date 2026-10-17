package icy.betterhorses.net;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ICalmable {

    void bh_calm(UUID rider, UUID horse, long until);

    void bh_releaseCalm();

    boolean bh_calmedBy(UUID rider);

    boolean bh_calmedAgainst(@Nullable Entity target);

    static @Nullable ICalmable of(Entity entity) {
        return entity instanceof ICalmable calmable ? calmable : null;
    }
}
