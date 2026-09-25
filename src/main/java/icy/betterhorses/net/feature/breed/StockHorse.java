package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class StockHorse implements BreedAbility {

    private boolean seeing;

    private static final int VISION_DURATION = 300;
    private static final int VISION_REFRESH = 100;
    private static final int HERD_INTERVAL = 10;
    private static final int FEED_INTERVAL = 100;
    private static final double HERD_RADIUS = 20.0D;
    private static final double FEED_RADIUS = 12.0D;
    private static final double HERD_STOP_SQ = 25.0D;
    private static final double HERD_SPEED = 2.0D;
    private static final TagKey<EntityType<?>> LIVESTOCK = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "livestock"));

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        Player rider = BhBreedAbilities.rider(horse);
        int tier = BhHorseTraits.bondTier(data.bh_getBond());

        boolean dark = rider != null && BhAbility.APPALOOSA_NIGHT.on()
                && BhBreedAbilities.isDarkOutside(horse);
        if (dark && horse.tickCount % VISION_REFRESH == 0) {
            BhBreedAbilities.applyQuietEffect(rider, MobEffects.NIGHT_VISION, VISION_DURATION, 0);
        }
        if (dark && !seeing) {
            BhSurge.pulse(data, 0, 2);
        }
        seeing = dark;

        if (tier >= 1 && rider != null && BhAbility.APPALOOSA_HERD.on()
                && !data.bh_isAbilityPaused() && horse.tickCount % HERD_INTERVAL == 0
                && herd(horse)) {
            BhSurge.pulse(data, 0);
        }
        if (tier >= 2 && BhAbility.APPALOOSA_FEED.on()
                && horse.tickCount % FEED_INTERVAL == 0 && feed(horse, data)) {
            BhSurge.pulse(data, 0, 1);
        }
    }

    private boolean herd(AbstractHorse horse) {
        boolean moved = false;
        for (Animal animal : nearby(horse, HERD_RADIUS)) {
            if (animal.isBaby() || horse.distanceToSqr(animal) < HERD_STOP_SQ) {
                continue;
            }
            animal.getNavigation().moveTo(horse, HERD_SPEED);
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
        for (Animal animal : nearby(horse, FEED_RADIUS)) {
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
                a -> a.is(LIVESTOCK) && a.isAlive());
    }
}
