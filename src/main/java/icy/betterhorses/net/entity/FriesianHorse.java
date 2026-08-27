package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class FriesianHorse extends BhBreedHorse {

    public static final float WIDTH = 1.3964844F;
    public static final float HEIGHT = 1.8F;

    private static final double PRESENCE_RADIUS = 12.0D;
    private static final int PRESENCE_INTERVAL_TICKS = 10;
    private static final double MOUNT_CALM_RADIUS = 24.0D;

    private boolean hadRider;

    public FriesianHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.FRIESIAN;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.FRIESIAN;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        Player rider = BhBreedAbilities.rider(this);

        if (rider != null && !hadRider) {
            calmNeutralMobs(rider, MOUNT_CALM_RADIUS, true);
        }
        hadRider = rider != null;

        if (rider != null && this.tickCount % PRESENCE_INTERVAL_TICKS == 0) {
            calmNeutralMobs(rider, PRESENCE_RADIUS, false);
        }
    }

    private void calmNeutralMobs(
            Player rider, double radius, boolean includeProvoked) {
        AABB box = this.getBoundingBox().inflate(radius);
        for (Mob mob
                : this.level().getEntitiesOfClass(Mob.class, box)) {
            if (!(mob instanceof NeutralMob neutral)) {
                continue;
            }
            LivingEntity target = mob.getTarget();
            if (target != rider && target != this) {
                continue;
            }
            if (!includeProvoked) {
                LivingEntity attacker = mob.getLastHurtByMob();
                if (attacker == rider || attacker == this) {
                    continue;
                }
            }
            mob.setTarget(null);
            neutral.stopBeingAngry();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.225D)
                .add(Attributes.JUMP_STRENGTH, 0.65D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

}
