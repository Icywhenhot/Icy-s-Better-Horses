package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class StockHorse implements BreedAbility {

    private boolean seeing;

    private static final int VISION_DURATION = 300;
    private static final int VISION_REFRESH = 100;
    private static final int HERD_INTERVAL = 10;
    private static final Tune FEED_INTERVAL = Tune.of("stock_horse", "feed_every", 100.0D).within(1.0D, 72000.0D);
    private static final Tune HERD_RADIUS = Tune.of("stock_horse", "herd_radius", 20.0D).within(0.0D, 128.0D);
    private static final Tune FEED_RADIUS = Tune.of("stock_horse", "feed_radius", 12.0D).within(0.0D, 128.0D);
    private static final Tune HERD_STOP = Tune.of("stock_horse", "herd_stop_distance", 5.0D).within(0.0D, 64.0D);
    private static final Tune HERD_SPEED = Tune.of("stock_horse", "herd_speed", 2.0D).within(0.0D, 10.0D);
    private static final TagKey<EntityType<?>> LIVESTOCK = TagKey.create(Registries.ENTITY_TYPE,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "livestock"));

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        Player rider = BhBreedAbilities.rider(horse);
        boolean dark = rider != null && BhAbility.APPALOOSA_NIGHT.on(data)
                && BhBreedAbilities.isDarkOutside(horse);
        if (dark && horse.tickCount % VISION_REFRESH == 0) {
            BhBreedAbilities.applyQuietEffect(rider, MobEffects.NIGHT_VISION, VISION_DURATION, 0);
        }
        if (dark && !seeing) {
            BhSurge.pulse(data, 0, 2);
        }
        seeing = dark;

        if (BhAbility.APPALOOSA_HERD.tier(data) >= 1 && rider != null
                && !data.bh_isAbilityPaused() && horse.tickCount % HERD_INTERVAL == 0
                && herd(horse)) {
            BhSurge.pulse(data, 0);
        }
        if (BhAbility.APPALOOSA_FEED.tier(data) >= 2
                && horse.tickCount % FEED_INTERVAL.ticks() == 0 && feed(horse, data)) {
            BhSurge.pulse(data, 0, 1);
        }
    }

    private boolean herd(AbstractHorse horse) {
        boolean moved = false;
        for (Animal animal : nearby(horse, HERD_RADIUS.get())) {
            if (animal.isBaby() || horse.distanceToSqr(animal) < HERD_STOP.get() * HERD_STOP.get()) {
                continue;
            }
            animal.getNavigation().moveTo(horse, HERD_SPEED.get());
            moved = true;
        }
        return moved;
    }

    private boolean feed(AbstractHorse horse, IHorseData data) {
        if (!data.bh_hasGear(GearSlot.CHEST)) {
            return false;
        }
        boolean fed = false;
        SimpleContainer chest = data.bh_getChestContainer();
        for (Animal animal : nearby(horse, FEED_RADIUS.get())) {
            if (animal.getAge() != 0 || !animal.canFallInLove()) {
                continue;
            }
            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack stack = chest.getItem(i);
                if (stack.isEmpty() || !animal.isFood(stack)) {
                    continue;
                }
                stack.shrink(1);
                chest.setChanged();
                animal.setInLove(null);
                fed = true;
                break;
            }
        }
        return fed;
    }

    private List<Animal> nearby(AbstractHorse horse, double radius) {
        AABB box = horse.getBoundingBox().inflate(radius);
        return horse.level().getEntitiesOfClass(Animal.class, box,
                a -> a.getType().is(LIVESTOCK) && a.isAlive());
    }
}
