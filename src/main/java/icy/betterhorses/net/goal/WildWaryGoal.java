package icy.betterhorses.net.goal;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.feature.breed.WildTraits;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public final class WildWaryGoal extends AvoidEntityGoal<Player> {

    private final AbstractHorse horse;
    private final TargetingConditions spotting;
    private final Predicate<LivingEntity> scary;

    private WildWaryGoal(AbstractHorse horse, float radius, double walk, double run, boolean armed) {
        super(horse, Player.class, radius, walk, run, e -> false);
        this.horse = horse;
        this.scary = e -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && (armed ? armed(e) : !e.isShiftKeyDown());
        this.spotting = TargetingConditions.forNonCombat().range(radius).selector(scary);
    }

    public static WildWaryGoal wary(AbstractHorse horse) {
        return new WildWaryGoal(horse, WildTraits.WARY_RADIUS.f(), 1.0D, 1.3D, false);
    }

    public static WildWaryGoal armed(AbstractHorse horse) {
        return new WildWaryGoal(horse, WildTraits.WEAPON_RADIUS.f(), 1.5D, 1.9D, true);
    }

    private static boolean armed(LivingEntity e) {
        Item item = e.getMainHandItem().getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof BowItem
                || item instanceof CrossbowItem || item instanceof TridentItem;
    }

    @Override
    public boolean canUse() {
        if (horse.isTamed() || horse.isVehicle() || horse.isLeashed() || !WildTraits.wild(horse)
                || !BhAbility.WILD_SKITTISH.on()) {
            return false;
        }
        toAvoid = horse.level().getNearestEntity(
                horse.level().getEntitiesOfClass(Player.class, horse.getBoundingBox().inflate(maxDist, 3.0D, maxDist), scary),
                spotting, horse, horse.getX(), horse.getY(), horse.getZ());
        if (toAvoid == null) {
            return false;
        }
        Vec3 away = DefaultRandomPos.getPosAway(horse, 16, 7, toAvoid.position());
        if (away == null || toAvoid.distanceToSqr(away.x, away.y, away.z) < toAvoid.distanceToSqr(horse)) {
            return false;
        }
        path = pathNav.createPath(away.x, away.y, away.z, 0);
        return path != null;
    }
}
