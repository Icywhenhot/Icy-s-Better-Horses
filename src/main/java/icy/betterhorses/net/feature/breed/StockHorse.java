package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class StockHorse implements BreedAbility {

    private static final int VISION_DURATION = 300;
    private static final int VISION_REFRESH = 100;
    private static final int HERD_INTERVAL = 20;
    private static final int FEED_INTERVAL = 100;
    private static final double HERD_RADIUS = 12.0D;
    private static final double HERD_STOP_SQ = 25.0D;
    private static final double HERD_SPEED = 1.15D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        Player rider = BhBreedAbilities.rider(horse);
        int tier = BhHorseTraits.bondTier(data.bh_getBond());

        if (rider != null && horse.tickCount % VISION_REFRESH == 0
                && BhBreedAbilities.isDarkOutside(horse)) {
            BhBreedAbilities.applyQuietEffect(rider, MobEffects.NIGHT_VISION, VISION_DURATION, 0);
        }

        if (tier >= 1 && data.bh_isAbilityToggled() && horse.tickCount % HERD_INTERVAL == 0) {
            herd(horse);
        }
        if (tier >= 2 && horse.tickCount % FEED_INTERVAL == 0) {
            feed(horse, data);
        }
    }

    private void herd(AbstractHorse horse) {
        for (Animal animal : nearby(horse)) {
            if (animal.isBaby() || horse.distanceToSqr(animal) < HERD_STOP_SQ) {
                continue;
            }
            animal.getNavigation().moveTo(horse, HERD_SPEED);
        }
    }

    private void feed(AbstractHorse horse, IHorseData data) {
        if (!data.bh_hasGear(GearSlot.CHEST)) {
            return;
        }
        SimpleContainer chest = data.bh_getChestContainer();
        for (Animal animal : nearby(horse)) {
            if (animal.isBaby() || animal.isInLove() || !animal.canFallInLove()) {
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
                break;
            }
        }
    }

    private List<Animal> nearby(AbstractHorse horse) {
        AABB box = horse.getBoundingBox().inflate(HERD_RADIUS);
        return horse.level().getEntitiesOfClass(Animal.class, box,
                a -> !(a instanceof AbstractHorse) && a.isAlive());
    }
}
