package icy.betterhorses.net.feature;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class CartRig implements HorseFeature {

    private @Nullable HorseCartEntity cart;

    private boolean frozen;
    private float frozenYaw;
    private @Nullable Vec3 frozenPos;

    public @Nullable HorseCartEntity cart() {
        return cart;
    }

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        syncCartEntity(horse, data);
        freezeWhenUnridden(horse, data);
    }

    private void syncCartEntity(AbstractHorse horse, IHorseData data) {
        if (!(horse.level() instanceof ServerLevel)) {
            return;
        }

        boolean wantsCart = data.bh_hasCartGear();
        boolean hasCart = cart != null && cart.isAlive() && !cart.isRemoved();

        if (!wantsCart) {
            data.bh_dropCartChest();
        }

        if (wantsCart && !hasCart) {
            cart = HorseCartEntity.spawnFor(horse);
        } else if (!wantsCart && cart != null) {
            if (hasCart) {
                cart.discard();
            }
            cart = null;
        }
    }

    private void freezeWhenUnridden(AbstractHorse horse, IHorseData data) {
        if (horse.level().isClientSide() || !data.bh_hasCartGear()) {
            frozen = false;
            return;
        }

        for (Entity passenger : horse.getPassengers()) {
            if (passenger instanceof Player) {
                frozen = false;
                return;
            }
        }

        if (!frozen) {
            frozen = true;
            frozenYaw = horse.getYRot();
            frozenPos = horse.position();
        }

        if (frozenPos != null) {
            horse.setPos(frozenPos.x, horse.getY(), frozenPos.z);
        }
        Vec3 motion = horse.getDeltaMovement();
        horse.setDeltaMovement(0.0D, Math.min(motion.y, 0.0D), 0.0D);
        horse.hurtMarked = true;
        horse.getNavigation().stop();
        horse.xxa = 0.0F;
        horse.yya = 0.0F;
        horse.zza = 0.0F;

        float yaw = frozenYaw;
        horse.setYRot(yaw);
        horse.yRotO = yaw;
        horse.setYHeadRot(yaw);
        horse.setYBodyRot(yaw);
        horse.yHeadRotO = yaw;
        horse.yBodyRotO = yaw;
    }
}
