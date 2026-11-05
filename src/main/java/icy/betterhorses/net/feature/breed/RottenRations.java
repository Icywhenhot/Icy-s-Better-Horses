package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhDamageTypes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class RottenRations implements BreedAbility {

    private static final int FUEL_TICKS = 400;

    private int fuel;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!BhAbility.ZOMBIE_FUEL.on() || BhHorseTraits.bondTier(data.bh_getBond()) < 1) {
            fuel = 0;
        } else if (fuel > 0) {
            fuel--;
        }
        data.bh_setSurge(fuel > 0 ? BhSurge.pack(BhSurge.ACTIVE, fuel, FUEL_TICKS, 15, 1) : 0);
    }

    public static void feed(AbstractHorse horse, IHorseData data) {
        if (!data.bh_getBreed().isZombie() || BhHorseTraits.bondTier(data.bh_getBond()) < 1
                || !BhAbility.ZOMBIE_FUEL.on()) {
            return;
        }
        RottenRations ability = of(horse);
        if (ability != null) {
            ability.fuel = FUEL_TICKS;
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, FUEL_TICKS, FUEL_TICKS, 15, 1));
        }
    }

    public static void damage(LivingIncomingDamageEvent event) {
        AbstractHorse horse = attacker(event.getSource(), event.getEntity());
        if (horse == null || !BhAbility.ZOMBIE_FUEL.on()
                || BhHorseTraits.bondTier(IHorseData.of(horse).bh_getBond()) < 1) {
            return;
        }
        RottenRations ability = of(horse);
        if (ability != null && ability.fuel > 0) {
            event.setAmount(event.getAmount() * 1.15F);
        }
    }

    public static void hit(LivingDamageEvent.Post event) {
        AbstractHorse horse = attacker(event.getSource(), event.getEntity());
        if (horse == null || event.getNewDamage() <= 0.0F || !BhAbility.ZOMBIE_ROT.on()) {
            return;
        }
        IHorseData data = IHorseData.of(horse);
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        if (tier < 1 || !event.getEntity().isAlive()) {
            return;
        }
        BhBreedAbilities.applyQuietEffect(event.getEntity(), MobEffects.WEAKNESS, tier >= 2 ? 100 : 80, tier >= 2 ? 1 : 0);
        if (tier >= 2) {
            BhBreedAbilities.applyQuietEffect(event.getEntity(), MobEffects.HUNGER, 100, 0);
        }
        BhSurge.pulse(data, 0, tier >= 2 ? 2 : 0);
    }

    private static AbstractHorse attacker(DamageSource source, LivingEntity target) {
        if (!(source.getDirectEntity() instanceof AbstractHorse horse)
                || !IHorseData.of(horse).bh_getBreed().isZombie()
                || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.BYPASSES_ARMOR)
                || !(source.is(BhDamageTypes.HORSE_BASH) || source.is(BhDamageTypes.HORSE_KICK)
                || source.getEntity() == horse)
                || target == horse || horse.hasIndirectPassenger(target) || horse.isAlliedTo(target)) {
            return null;
        }
        return horse;
    }

    private static RottenRations of(AbstractHorse horse) {
        if (horse instanceof IHorseAbilityHost host) {
            for (BreedAbility ability : host.bh_allAbilities()) {
                if (ability instanceof RottenRations rations) {
                    return rations;
                }
            }
        }
        return null;
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        fuel = 0;
        data.bh_setSurge(0);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("fuel", fuel);
    }

    @Override
    public void load(CompoundTag tag) {
        fuel = Math.clamp(tag.getInt("fuel"), 0, FUEL_TICKS);
    }
}
