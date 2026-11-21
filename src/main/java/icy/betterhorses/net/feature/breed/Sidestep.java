package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.tuning.Tune;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.jetbrains.annotations.Nullable;

public final class Sidestep implements BreedAbility {

    private static final Tune CHANCE = Tune.of("sidestep", "dodge_chance", 0.25D).within(0.0D, 1.0D);
    private static final Tune GUARD_TICKS = Tune.of("sidestep", "untouchable_ticks", 40.0D).within(0.0D, 511.0D);

    private long guardUntil;
    private long rolledAt = -1L;
    private int rolledFor;
    private boolean rolled;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.level().isClientSide()) {
            return;
        }
        long left = guardUntil - horse.level().getGameTime();
        if (left > 0) {
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, (int) left, GUARD_TICKS.ticks(), 0, 1));
            return;
        }
        SlowBlockImmunity.show(data, SlowBlockImmunity.wading(horse) && SlowBlockImmunity.ignoresSlowBlocks(data), 2);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        guardUntil = 0L;
        data.bh_setSurge(0);
    }

    public static void attack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        AbstractHorse horse = target instanceof AbstractHorse h ? h
                : target.getVehicle() instanceof AbstractHorse h ? h : null;
        Sidestep ability = horse == null ? null : of(horse);
        if (ability == null) {
            return;
        }
        DamageSource src = event.getSource();
        long now = horse.level().getGameTime();
        if (now < ability.guardUntil && !src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
            return;
        }
        IHorseData data = IHorseData.of(horse);
        if (BhAbility.PRZEWALSKI_SIDESTEP.tier(data, BhContent.PRZEWALSKI.getKey()) < 1 || !melee(src, horse)) {
            return;
        }
        if (ability.rolledAt != now || ability.rolledFor != target.getId()) {
            ability.rolledAt = now;
            ability.rolledFor = target.getId();
            ability.rolled = horse.getRandom().nextDouble() < CHANCE.get();
        }
        if (!ability.rolled) {
            return;
        }
        event.setCanceled(true);
        horse.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.NEUTRAL, 1.0F, 1.3F);
        if (BhAbility.PRZEWALSKI_UNTOUCHABLE.tier(data, BhContent.PRZEWALSKI.getKey()) >= 2) {
            ability.guardUntil = now + GUARD_TICKS.ticks();
        } else {
            BhSurge.pulse(data, 0, 0);
        }
    }

    private static boolean melee(DamageSource src, AbstractHorse horse) {
        Entity by = src.getEntity();
        return by != null && src.getDirectEntity() == by && by != horse && !horse.hasIndirectPassenger(by)
                && !src.is(DamageTypeTags.IS_PROJECTILE) && !src.is(DamageTypeTags.IS_EXPLOSION);
    }

    private static @Nullable Sidestep of(AbstractHorse horse) {
        if (horse instanceof IHorseAbilityHost host) {
            for (BreedAbility ability : host.bh_allAbilities()) {
                if (ability instanceof Sidestep s) {
                    return s;
                }
            }
        }
        return null;
    }
}
