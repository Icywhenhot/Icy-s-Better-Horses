package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhDamageTypes;
import icy.betterhorses.net.ModSounds;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class HorseCombat implements HorseFeature {

    private static final double MIN_CHARGE_SPEED = 0.22D;
    private static final double FULL_CHARGE_SPEED = 0.34D;
    private static final double PER_ARMOR = 0.25D;
    private static final double ARMOR_CAP = 3.0D;
    private static final double IRONCLAD_CAP = 5.0D;
    private static final double BASE_DAMAGE = 7.0D;
    private static final double DAMAGE_CAP = 30.0D;
    private static final double FACING_DOT = 0.3D;
    private static final double BASH_SIDE = 0.3D;
    private static final double BASH_LIFT = 0.3D;
    private static final double BASH_AHEAD = 0.5D;
    private static final int CHAIN_VARIANT = 1;
    private static final int COOLDOWN = 100;
    private static final int KICK_TICKS = 8;
    private static final int BOLT_TICKS = 60;
    private static final double LOOSE_CHARGE = 0.8D;
    private static final String SLOW_KEY = "charge_slow";
    private static final int SLOW_TICKS = 30;
    private static final int NEIGH_TICKS = 50;
    private static final double SLOW_AMOUNT = -0.6D;

    private static final int FULL_WIND = 60;
    private static final int MIN_WIND = 20;
    private static final double WIND_FLOOR = 0.7D;
    private static final double WIND_GAIN = 1.05D;
    private static final double TURN_TOLERANCE = 6.0D;

    private int cooldown;
    private int straight;
    private int slowed;
    private int neighing;
    private float lastYaw = Float.NaN;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return;
        }
        if (!BhConfig.horseCombatEnabled()) {
            if (slowed > 0) BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED, BhHorseAttributes.Source.ABILITY, SLOW_KEY);
            slowed = 0;
            cooldown = 0;
            straight = 0;
            lastYaw = Float.NaN;
            data.bh_setKickTicks(0);
            data.bh_setCharge(BhSurge.HIDDEN);
            data.bh_setCombatTarget(null);
            return;
        }
        ResourceKey<BreedType> breedKey = data.bh_getBreedKey();
        trackStraightLine(horse);
        int kicking = data.bh_getKickTicks();
        if (kicking > 0) {
            data.bh_setKickTicks(kicking - 1);
        }
        if (neighing > 0) {
            neighing--;
        }
        if (slowed > 0) {
            slowed--;
            if (slowed == 0) {
                BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED,
                        BhHorseAttributes.Source.ABILITY, SLOW_KEY);
            }
        }
        Vec3 motion = horse.getKnownMovement();
        Vec3 flat = new Vec3(motion.x, 0.0D, motion.z);
        publishCharge(horse, data, flat.length());

        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (breedKey == null || !horse.onGround()) {
            return;
        }
        if (!(horse.getControllingPassenger() instanceof Player rider)) {
            return;
        }
        if (!galloping(data, flat.length()) || straight < MIN_WIND) {
            return;
        }

        List<LivingEntity> hit = targets(horse, data, rider, flat);
        if (hit.isEmpty()) {
            return;
        }

        ArchetypeType arch = BhBreedData.of(breedKey).archetype();
        float dmg = damage(horse, arch.bashDamage(),
                charge(flat.length()) * wind() * momentum(breedKey, data));
        DamageSource src = level.damageSources().source(BhDamageTypes.HORSE_BASH, horse, rider);
        Vec3 dir = flat.normalize();

        boolean killed = false;
        for (LivingEntity target : hit) {
            target.hurt(src, dmg);
            shove(target, dir, arch.bashKnockback());
            killed |= target.isDeadOrDying();
        }
        horse.playSound(ModSounds.HORSE_CHARGE_THUD, 0.5F, 1.0F);
        if (neighing == 0) {
            horse.playSound(ModSounds.HORSE_NEIGH, 1.0F, 1.0F);
            neighing = NEIGH_TICKS;
        }
        if (rider instanceof ServerPlayer serverRider) {
            PacketDistributor.sendToPlayer(serverRider, new HorseChargeShakePayload());
        }
        if (killed && chains(breedKey, data)) {
            BhSurge.pulse(data, 0, CHAIN_VARIANT);
            return;
        }
        BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ABILITY, SLOW_KEY,
                SLOW_AMOUNT, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        slowed = SLOW_TICKS;
        cooldown = COOLDOWN;
    }

    private void publishCharge(AbstractHorse horse, IHorseData data, double speed) {
        if (!galloping(data, speed) || !(horse.getControllingPassenger() instanceof Player)) {
            data.bh_setCharge(BhSurge.HIDDEN);
            return;
        }
        double ready = cooldown > 0
                ? 1.0D - (double) cooldown / COOLDOWN
                : (double) straight / MIN_WIND;
        data.bh_setCharge((int) Math.round(Mth.clamp(ready, 0.0D, 1.0D) * 100.0D));
    }

    private static boolean galloping(IHorseData data, double speed) {
        int gear = data.bh_getGear();
        return (gear == 0 || gear == BhGears.GALLOP_GEAR) && speed >= MIN_CHARGE_SPEED;
    }

    private static double barding(AbstractHorse horse) {
        double cap = Objects.equals(IHorseData.of(horse).bh_getBreedKey(), BhContent.CLYDESDALE.getKey())
                ? IRONCLAD_CAP : ARMOR_CAP;
        return Math.min(cap, horse.getAttributeValue(Attributes.ARMOR) * PER_ARMOR);
    }

    private static double charge(double speed) {
        double t = (speed - MIN_CHARGE_SPEED) / (FULL_CHARGE_SPEED - MIN_CHARGE_SPEED);
        return 0.6D + 0.4D * Mth.clamp(t, 0.0D, 1.0D);
    }

    private double wind() {
        return WIND_FLOOR + WIND_GAIN * (double) straight / FULL_WIND;
    }


    private void trackStraightLine(AbstractHorse horse) {
        float yaw = horse.getYRot();
        boolean rolling = !Float.isNaN(lastYaw)
                && Math.abs(Mth.degreesDifference(lastYaw, yaw)) < TURN_TOLERANCE
                && horse.getKnownMovement().horizontalDistanceSqr() > 0.001D;
        lastYaw = yaw;
        straight = rolling ? Math.min(FULL_WIND, straight + 1) : 0;
    }

    private double momentum(ResourceKey<BreedType> breedKey, IHorseData data) {
        if (!Objects.equals(breedKey, BhContent.PERCHERON.getKey())
                || !BhAbility.PERCHERON_MOMENTUM.on() || BhHorseTraits.bondTier(data.bh_getBond()) < 1) {
            return 1.0D;
        }
        return 1.0D + 0.5D * (double) straight / FULL_WIND;
    }

    private static boolean chains(ResourceKey<BreedType> breedKey, IHorseData data) {
        return Objects.equals(breedKey, BhContent.PERCHERON.getKey())
                && BhHorseTraits.bondTier(data.bh_getBond()) >= 2
                && BhAbility.PERCHERON_CHAIN.on();
    }

    public static boolean mayTarget(LivingEntity target) {
        if (BhConfig.horsePvpEnabled()) {
            return true;
        }
        if (target instanceof Player) {
            return false;
        }
        return !(target instanceof OwnableEntity owned) || owned.getOwnerUUID() == null;
    }

    private List<LivingEntity> targets(AbstractHorse horse, IHorseData data, Player rider, Vec3 flat) {
        Vec3 dir = flat.normalize();
        AABB box = horse.getBoundingBox().inflate(BASH_SIDE, BASH_LIFT, BASH_SIDE)
                .expandTowards(dir.x * BASH_AHEAD, 0.0D, dir.z * BASH_AHEAD);
        UUID owner = data.bh_getOwner();
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : horse.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == horse || horse.hasIndirectPassenger(e)) {
                continue;
            }
            if (e instanceof Player p && data.bh_mayHandle(p.getUUID())) {
                continue;
            }
            if (!mayTarget(e)) {
                continue;
            }
            if (e instanceof AbstractHorse other
                    && owner != null && owner.equals(IHorseData.of(other).bh_getOwner())) {
                continue;
            }
            if (e.isAlliedTo(horse) || e.isAlliedTo(rider)) {
                continue;
            }
            Vec3 to = e.position().subtract(horse.position());
            if (to.lengthSqr() < 1.0E-4D || dir.dot(to.normalize()) < FACING_DOT) {
                continue;
            }
            out.add(e);
        }
        return out;
    }

    public void onHurt(AbstractHorse horse, IHorseData data, DamageSource source) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return;
        }
        if (data.bh_getBreedKey() == null || !(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (horse.hasIndirectPassenger(attacker)
                || (data.bh_isOwned() && data.bh_mayHandle(attacker.getUUID()))
                || !mayTarget(attacker)) {
            return;
        }
        Vec3 to = attacker.position().subtract(horse.position());
        if (to.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 look = horse.getLookAngle();
        if (new Vec3(look.x, 0.0D, look.z).normalize().dot(to.normalize()) > -FACING_DOT) {
            return;
        }

        strike(level, horse, data, attacker);
        data.bh_clearStanding();
        if (data.bh_getCombatTarget() == null) {
            data.bh_setSpookTicks(BOLT_TICKS);
        }
    }

    public static void strike(ServerLevel level, AbstractHorse horse, IHorseData data,
                              LivingEntity target) {
        ArchetypeType arch = BhBreedData.of(data.bh_getBreedKey()).archetype();
        data.bh_setKickTicks(KICK_TICKS);
        hit(level, horse, target, BhDamageTypes.HORSE_KICK,
                damage(horse, arch.kickDamage(), 1.0D), arch.bashKnockback() * 0.5D);
    }

    public static void chargeStrike(ServerLevel level, AbstractHorse horse, IHorseData data,
                                    LivingEntity target) {
        ArchetypeType arch = BhBreedData.of(data.bh_getBreedKey()).archetype();
        hit(level, horse, target, BhDamageTypes.HORSE_BASH,
                damage(horse, arch.bashDamage(), LOOSE_CHARGE), arch.bashKnockback());
    }

    private static float damage(AbstractHorse horse, double mult, double scale) {
        return (float) Math.min(DAMAGE_CAP, (BASE_DAMAGE * mult + barding(horse)) * scale);
    }

    private static void hit(ServerLevel level, AbstractHorse horse, LivingEntity target,
                            ResourceKey<DamageType> type, float damage, double knockback) {
        target.hurt(level.damageSources().source(type, horse, horse), damage);
        Vec3 away = target.position().subtract(horse.position());
        if (away.lengthSqr() > 1.0E-4D) {
            shove(target, new Vec3(away.x, 0.0D, away.z).normalize(), knockback);
        }
    }

    private static void shove(LivingEntity target, Vec3 dir, double strength) {
        Vec3 push = dir.scale(strength * 0.5D);
        target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.15D, push.z));
        target.hurtMarked = true;
    }
}
