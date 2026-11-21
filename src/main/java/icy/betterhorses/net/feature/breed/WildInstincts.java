package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModSounds;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class WildInstincts implements BreedAbility {

    private static final int ALERT_INTERVAL = 20;
    private static final Tune ALERT_RADIUS = Tune.of("wild_instincts", "alert_radius", 15.0D).within(0.0D, 128.0D);
    private static final Tune ALERT_COOLDOWN = Tune.of("wild_instincts", "alert_cooldown", 120.0D).within(0.0D, 72000.0D);
    private static final Tune GLOW_TICKS = Tune.of("wild_instincts", "glow_ticks", 60.0D).within(1.0D, 72000.0D);
    private static final Tune SELF_HEAL = Tune.of("wild_instincts", "self_heal_every", 300.0D, 160.0D, 160.0D).within(1.0D, 72000.0D);
    private static final Tune RIDER_HEAL = Tune.of("wild_instincts", "rider_heal_every", 0.0D, 200.0D, 100.0D).within(0.0D, 72000.0D);
    private static final Tune HEAL_AMOUNT = Tune.of("wild_instincts", "heal_amount", 2.0D).within(0.0D, 1024.0D);

    private int alertCooldown;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return;
        }
        if (alertCooldown > 0) {
            alertCooldown--;
        }

        int heal = BhAbility.MUSTANG_SELF_HEAL.tier(data);
        if (heal >= 0
                && horse.tickCount % SELF_HEAL.ticks(heal) == 0 && horse.getHealth() < horse.getMaxHealth()) {
            horse.heal(HEAL_AMOUNT.f());
        }

        int mend = BhAbility.MUSTANG_RIDER_HEAL.tier(data);
        int riderRate = mend >= 0 ? RIDER_HEAL.ticks(mend) : 0;
        if (riderRate > 0 && horse.tickCount % riderRate == 0) {
            Player rider = BhBreedAbilities.rider(horse);
            if (rider != null && rider.getHealth() < rider.getMaxHealth()) {
                rider.heal(HEAL_AMOUNT.f());
                BhSurge.pulse(data, 0, 1);
            }
        }

        if (!BhAbility.MUSTANG_ALERT.on(data) || !data.bh_isOwned()
                || alertCooldown > 0 || horse.tickCount % ALERT_INTERVAL != 0) {
            return;
        }
        List<LivingEntity> hostiles = BhBreedAbilities.hostilesNearby(horse, ALERT_RADIUS.get());
        if (hostiles.isEmpty()) {
            return;
        }

        alertCooldown = ALERT_COOLDOWN.ticks();
        horse.level().playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                ModSounds.HORSE_ANGRY_SNORT, horse.getSoundSource(), 1.0F, 1.0F);

        for (LivingEntity hostile : hostiles) {
            hostile.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.GLOWING, GLOW_TICKS.ticks(), 0, false, false));
        }
    }
}
