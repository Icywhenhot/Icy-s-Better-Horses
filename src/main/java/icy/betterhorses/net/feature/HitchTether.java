package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.item.HitchpostBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class HitchTether implements HorseFeature {

    private @Nullable Vec3 anchor;

    public void anchorAt(@Nullable Vec3 pos) {
        this.anchor = pos;
    }

    @Override
    public void onLoad(AbstractHorse horse, IHorseData data) {
        this.anchor = null;
    }

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (data.bh_getHitchpostPos() == null) {
            return;
        }

        if (!BhConfig.hitchpostEnabled()) {
            if (horse.level() instanceof ServerLevel serverLevel) {
                HitchpostBlock.releaseHorse(serverLevel, horse, true);
            }
            return;
        }

        if (horse.level() instanceof ServerLevel serverLevel
                && !HitchpostBlock.isValidTether(serverLevel, horse, data.bh_getHitchpostPos())) {
            HitchpostBlock.releaseHorse(serverLevel, horse, true);
            return;
        }

        if (anchor == null) {
            anchor = horse.position();
        }

        applyConstraint(horse);
    }

    private void applyConstraint(AbstractHorse horse) {
        if (anchor == null) {
            return;
        }

        horse.getNavigation().stop();
        Vec3 currentPos = horse.position();
        double horizontalDistanceSq = (currentPos.x - anchor.x) * (currentPos.x - anchor.x)
                + (currentPos.z - anchor.z) * (currentPos.z - anchor.z);
        if (horizontalDistanceSq > 0.04D || Math.abs(currentPos.y - anchor.y) > 1.25D) {
            horse.teleportTo(anchor.x, anchor.y, anchor.z);
        }

        horse.setDeltaMovement(Vec3.ZERO);
        horse.hurtMarked = true;
    }
}
