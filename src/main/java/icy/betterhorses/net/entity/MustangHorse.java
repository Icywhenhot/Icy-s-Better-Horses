package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

public class MustangHorse extends MediumHorse {

    private static final double ALERT_RADIUS = 15.0D;
    private static final int ALERT_INTERVAL_TICKS = 20;
    private static final int ALERT_COOLDOWN_TICKS = 6 * 20;
    private static final int REGEN_INTERVAL_TICKS = 30 * 20;

    private static final int ALERT_GLOW_TICKS = 3 * 20;

    private int alertCooldownTicks;
    private int glowExpiryTick;
    private final java.util.List<java.util.UUID> glowingSensed = new java.util.ArrayList<>();

    public MustangHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        if (glowExpiryTick != 0 && this.tickCount >= glowExpiryTick) {
            clearAlertGlow(serverLevel);
        }
        if (alertCooldownTicks > 0) {
            alertCooldownTicks--;
        }

        if (this.tickCount % REGEN_INTERVAL_TICKS == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0F);
        }

        if (alertCooldownTicks > 0 || this.tickCount % ALERT_INTERVAL_TICKS != 0) {
            return;
        }
        java.util.List<net.minecraft.world.entity.LivingEntity> hostiles =
                BhBreedAbilities.hostilesNearby(this, ALERT_RADIUS);
        if (hostiles.isEmpty()) {
            return;
        }

        alertCooldownTicks = ALERT_COOLDOWN_TICKS;
        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                icy.betterhorses.net.ModSounds.HORSE_ANGRY_SNORT,
                this.getSoundSource(),
                1.0F,
                1.0F);

        for (net.minecraft.world.entity.LivingEntity hostile : hostiles) {
            if (BhBreedAbilities.startGlowing(hostile)) {
                glowingSensed.add(hostile.getUUID());
            }
        }
        if (!glowingSensed.isEmpty()) {
            glowExpiryTick = this.tickCount + ALERT_GLOW_TICKS;
        }
    }

    private void clearAlertGlow(net.minecraft.server.level.ServerLevel level) {
        for (java.util.UUID id : glowingSensed) {
            if (level.getEntity(id) instanceof net.minecraft.world.entity.Entity entity) {
                entity.setGlowingTag(false);
            }
        }
        glowingSensed.clear();
        glowExpiryTick = 0;
    }

    @Override
    public void remove(net.minecraft.world.entity.Entity.RemovalReason reason) {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel && glowExpiryTick != 0) {
            clearAlertGlow(serverLevel);
        }
        super.remove(reason);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.MUSTANG;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.MUSTANG;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2625D)
                .add(Attributes.JUMP_STRENGTH, 0.68D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
