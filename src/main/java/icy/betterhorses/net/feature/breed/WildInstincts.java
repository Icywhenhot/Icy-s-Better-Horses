package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModSounds;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class WildInstincts implements BreedAbility {

    private static final double ALERT_RADIUS = 15.0D;
    private static final int ALERT_INTERVAL = 20;
    private static final int ALERT_COOLDOWN = 120;
    private static final int GLOW_TICKS = 60;
    private static final int[] SELF_HEAL = {600, 400, 400};
    private static final int[] RIDER_HEAL = {0, 120, 60};
    private static final float RIDER_HEAL_AMOUNT = 2.0F;

    private int alertCooldown;
    private int glowExpiry;
    private final List<UUID> sensed = new ArrayList<>();

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return;
        }
        int tier = BhHorseTraits.bondTier(data.bh_getBond());

        if (glowExpiry != 0 && horse.tickCount >= glowExpiry) {
            clearGlow(level);
        }
        if (alertCooldown > 0) {
            alertCooldown--;
        }

        if (horse.tickCount % SELF_HEAL[tier] == 0 && horse.getHealth() < horse.getMaxHealth()) {
            horse.heal(1.0F);
        }

        int riderRate = RIDER_HEAL[tier];
        if (riderRate > 0 && horse.tickCount % riderRate == 0) {
            Player rider = BhBreedAbilities.rider(horse);
            if (rider != null && rider.getHealth() < rider.getMaxHealth()) {
                rider.heal(RIDER_HEAL_AMOUNT);
                BhSurge.pulse(data, 0);
            }
        }

        if (alertCooldown > 0 || horse.tickCount % ALERT_INTERVAL != 0) {
            return;
        }
        List<LivingEntity> hostiles = BhBreedAbilities.hostilesNearby(horse, ALERT_RADIUS);
        if (hostiles.isEmpty()) {
            return;
        }

        alertCooldown = ALERT_COOLDOWN;
        horse.level().playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                ModSounds.HORSE_ANGRY_SNORT, horse.getSoundSource(), 1.0F, 1.0F);

        for (LivingEntity hostile : hostiles) {
            if (BhBreedAbilities.startGlowing(hostile)) {
                sensed.add(hostile.getUUID());
            }
        }
        if (!sensed.isEmpty()) {
            glowExpiry = horse.tickCount + GLOW_TICKS;
        }
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        if (horse.level() instanceof ServerLevel level) {
            clearGlow(level);
        }
    }

    private void clearGlow(ServerLevel level) {
        for (UUID id : sensed) {
            Entity e = level.getEntity(id);
            if (e != null) {
                e.setGlowingTag(false);
            }
        }
        sensed.clear();
        glowExpiry = 0;
    }
}
