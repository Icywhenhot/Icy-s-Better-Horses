package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.entity.BhBreedDonkey;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class FarmCompanion {

    private static final double RADIUS = 16.0D;
    private static final float TWIN_CHANCE = 0.25F;
    private static final TagKey<EntityType<?>> LIVESTOCK = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "livestock"));

    private FarmCompanion() {}

    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (event.isCanceled() || event.getChild() == null || !BhAbility.MINIATURE_FARM.on()
                || !(event.getParentA() instanceof Animal a) || !(event.getParentB() instanceof Animal b)
                || !a.getType().is(LIVESTOCK)
                || !(a.level() instanceof ServerLevel level)
                || level.random.nextFloat() >= TWIN_CHANCE || !nearMiniature(level, a, 2)) {
            return;
        }
        AgeableMob twin = a.getBreedOffspring(level, b);
        if (twin == null) {
            return;
        }
        twin.setBaby(true);
        twin.moveTo(a.getX(), a.getY(), a.getZ(), a.getYRot(), 0.0F);
        level.addFreshEntityWithPassengers(twin);
        level.sendParticles(ParticleTypes.HEART, a.getX(), a.getY() + 0.8D, a.getZ(), 5, 0.4D, 0.3D, 0.4D, 0.0D);
    }

    public static void onAnimalTick(EntityTickEvent.Post event) {
        if (!BhAbility.MINIATURE_FARM.on() || !(event.getEntity() instanceof Animal animal)
                || !(animal.level() instanceof ServerLevel level) || !animal.isAlive()
                || animal.tickCount % 20 != 0 || !animal.getType().is(LIVESTOCK)) {
            return;
        }
        int age = animal.getAge();
        if (age < 0 && nearMiniature(level, animal, 0)) {
            animal.setAge(Math.min(0, age + 3));
        } else if (age > 0 && nearMiniature(level, animal, 1)) {
            animal.setAge(Math.max(0, age - 4));
        }
    }

    private static boolean nearMiniature(ServerLevel level, Animal parent, int tier) {
        return !level.getEntitiesOfClass(BhBreedDonkey.class, parent.getBoundingBox().inflate(RADIUS), d -> {
            IHorseData data = IHorseData.of(d);
            return d.isAlive() && !d.isBaby() && d.bhFixedBreed().equals(BhContent.MINIATURE_DONKEY.getKey())
                    && d.distanceToSqr(parent) <= RADIUS * RADIUS && !data.bh_isAbilityPaused()
                    && BhHorseTraits.bondTier(data.bh_getBond()) >= tier;
        }).isEmpty();
    }
}
