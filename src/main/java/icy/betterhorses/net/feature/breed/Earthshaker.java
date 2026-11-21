package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhDamageTypes;
import net.neoforged.neoforge.network.PacketDistributor;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.feature.HorseCombat;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.tuning.Tune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class Earthshaker implements BreedAbility {

    private static final Tune GRAZE_EVERY = Tune.of("earthshaker", "graze_every", 100.0D).within(1.0D, 72000.0D);
    private static final Tune GRAZE_HEAL = Tune.of("earthshaker", "graze_heal", 2.0D).within(0.0D, 1024.0D);
    private static final Tune MIN_DROP = Tune.of("earthshaker", "min_drop", 3.0D).within(0.0D, 256.0D);
    private static final Tune RADIUS = Tune.of("earthshaker", "radius", 4.0D).within(0.0D, 32.0D);
    private static final Tune DAMAGE = Tune.of("earthshaker", "damage", 30.0D).within(0.0D, 1024.0D);
    private static final Tune LAUNCH = Tune.of("earthshaker", "launch", 0.9D).within(0.0D, 10.0D);

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.level() instanceof ServerLevel level) || horse.tickCount % GRAZE_EVERY.ticks() != 0) {
            return;
        }
        if (BhAbility.GIGANTEUS_FORAGER.tier(data) < 0 || horse.getHealth() >= horse.getMaxHealth()
                || !horse.onGround() || horse.getKnownMovement().horizontalDistanceSqr() > 0.01D) {
            return;
        }
        BlockPos pos = horse.blockPosition();
        if (!forage(level.getBlockState(pos))) {
            pos = pos.below();
            if (!forage(level.getBlockState(pos))) {
                return;
            }
        }
        horse.heal(GRAZE_HEAL.f());
        level.levelEvent(2001, pos, Block.getId(level.getBlockState(pos)));
        BhSurge.pulse(data, 0, 1);
    }

    private static boolean forage(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW);
    }

    public static void land(AbstractHorse horse, IHorseData data, float distance) {
        if (!(horse.level() instanceof ServerLevel level) || distance < MIN_DROP.f()
                || BhAbility.GIGANTEUS_QUAKE.tier(data, BhContent.GIGANTEUS.getKey()) < 2) {
            return;
        }
        Player rider = BhBreedAbilities.rider(horse);
        double r = RADIUS.get();
        DamageSource src = level.damageSources().source(BhDamageTypes.HORSE_BASH, horse, rider != null ? rider : horse);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, horse.getBoundingBox().inflate(r, 1.5D, r))) {
            if (e == horse || horse.hasIndirectPassenger(e) || !HorseCombat.mayTarget(e) || !foe(e, horse, data, rider)) {
                continue;
            }
            Vec3 away = new Vec3(e.getX() - horse.getX(), 0.0D, e.getZ() - horse.getZ());
            double d = away.length();
            if (d > r + horse.getBbWidth() * 0.5D) {
                continue;
            }
            e.hurt(src, DAMAGE.f());
            Vec3 dir = d < 1.0E-4D ? Vec3.ZERO : away.scale(1.0D / d);
            e.setDeltaMovement(dir.x * LAUNCH.get(), LAUNCH.get(), dir.z * LAUNCH.get());
            e.hurtMarked = true;
        }
        level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.GENERIC_EXPLODE,
                horse.getSoundSource(), 0.7F, 0.55F);
        BlockState ground = horse.getBlockStateOn();
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    horse.getX(), horse.getY() + 0.1D, horse.getZ(), 90, r * 0.5D, 0.1D, r * 0.5D, 0.15D);
        }
        if (rider instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new HorseChargeShakePayload());
        }
        BhSurge.pulse(data, 0, 0);
    }

    private static boolean foe(LivingEntity e, AbstractHorse horse, IHorseData data, Player rider) {
        if (e instanceof Enemy) {
            return true;
        }
        if (e instanceof Player p) {
            return !data.bh_mayHandle(p.getUUID()) && (rider == null || !p.isAlliedTo(rider));
        }
        return e instanceof Mob mob && (mob.getTarget() == horse || rider != null && mob.getTarget() == rider);
    }
}
