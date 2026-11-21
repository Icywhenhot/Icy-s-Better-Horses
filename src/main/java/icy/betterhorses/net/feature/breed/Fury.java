package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhDamageTypes;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.ICalmable;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.tuning.Tune;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class Fury implements BreedAbility {

    private static final Tune FURY_TICKS = Tune.of("fury", "fury_ticks", 100.0D).within(0.0D, 511.0D);
    private static final Tune FURY_MULTIPLIER = Tune.of("fury", "fury_multiplier", 2.0D).within(0.0D, 100.0D);
    private static final Tune RUSH_TICKS = Tune.of("fury", "rush_ticks", 100.0D).within(0.0D, 72000.0D);
    private static final Tune RUSH_LEVEL = Tune.of("fury", "rush_level", 1.0D, 2.0D, 2.0D).within(0.0D, 10.0D);
    private static final Tune VANISH_HEALTH = Tune.of("fury", "vanish_health", 0.25D).within(0.0D, 1.0D);
    private static final Tune VANISH_TICKS = Tune.of("fury", "vanish_ticks", 100.0D).within(1.0D, 511.0D);
    private static final Tune VANISH_COOLDOWN = Tune.of("fury", "vanish_cooldown", 1200.0D).within(0.0D, 72000.0D);
    private static final Tune VANISH_RADIUS = Tune.of("fury", "vanish_radius", 32.0D).within(0.0D, 128.0D);

    private long furyUntil;
    private long vanishUntil;
    private long vanishReady;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        if (now >= vanishReady && horse.isAlive() && BhAbility.TARPAN_VANISH.tier(data) >= 2
                && horse.getHealth() < horse.getMaxHealth() * VANISH_HEALTH.f()) {
            vanish(level, horse, now);
        }
        if (now < vanishUntil) {
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, (int) (vanishUntil - now), VANISH_TICKS.ticks(), 0, 2));
        } else if (now < furyUntil) {
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, (int) (furyUntil - now), FURY_TICKS.ticks(), 0, 0));
        } else {
            data.bh_setSurge(0);
        }
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        furyUntil = 0L;
        vanishUntil = 0L;
        data.bh_setSurge(0);
    }

    private void vanish(ServerLevel level, AbstractHorse horse, long now) {
        int ticks = VANISH_TICKS.ticks();
        Player rider = BhBreedAbilities.rider(horse);
        horse.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ticks, 0, false, false));
        if (rider != null) {
            rider.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ticks, 0, false, false));
        }
        UUID calmer = rider != null ? rider.getUUID() : horse.getUUID();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, horse.getBoundingBox().inflate(VANISH_RADIUS.get()))) {
            LivingEntity aim = mob.getTarget();
            LivingEntity grudge = mob.getLastHurtByMob();
            boolean after = aim == horse || grudge == horse || rider != null && (aim == rider || grudge == rider);
            if (after && mob instanceof ICalmable calm) {
                calm.bh_calm(calmer, horse.getUUID(), now + ticks);
                mob.setTarget(null);
            }
        }
        level.sendParticles(ParticleTypes.POOF, horse.getX(), horse.getY() + horse.getBbHeight() * 0.5D, horse.getZ(),
                30, horse.getBbWidth() * 0.5D, horse.getBbHeight() * 0.4D, horse.getBbWidth() * 0.5D, 0.02D);
        level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE,
                horse.getSoundSource(), 1.0F, 1.0F);
        vanishUntil = now + ticks;
        vanishReady = now + VANISH_COOLDOWN.ticks();
    }

    public static void hurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        if (victim instanceof AbstractHorse self && event.getAmount() > 0.0F) {
            Fury fury = of(self);
            if (fury != null && BhAbility.TARPAN_FURY.tier(IHorseData.of(self)) >= 0) {
                fury.furyUntil = now + FURY_TICKS.ticks();
            }
        }
        DamageSource src = event.getSource();
        if (!(src.getDirectEntity() instanceof AbstractHorse horse) || victim == horse
                || !(src.is(BhDamageTypes.HORSE_BASH) || src.is(BhDamageTypes.HORSE_KICK))) {
            return;
        }
        Fury fury = of(horse);
        if (fury == null) {
            return;
        }
        IHorseData data = IHorseData.of(horse);
        if (now < fury.furyUntil && BhAbility.TARPAN_FURY.tier(data) >= 0) {
            event.setAmount(event.getAmount() * FURY_MULTIPLIER.f());
        }
        int rush = BhAbility.TARPAN_RUSH.tier(data);
        if (rush >= 0 && src.is(BhDamageTypes.HORSE_BASH) && RUSH_LEVEL.ticks(rush) > 0) {
            horse.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, RUSH_TICKS.ticks(),
                    RUSH_LEVEL.ticks(rush) - 1, false, true));
            BhSurge.pulse(data, 0, 1);
        }
    }

    private static @Nullable Fury of(AbstractHorse horse) {
        if (horse instanceof IHorseAbilityHost host) {
            for (BreedAbility ability : host.bh_allAbilities()) {
                if (ability instanceof Fury f) {
                    return f;
                }
            }
        }
        return null;
    }
}
