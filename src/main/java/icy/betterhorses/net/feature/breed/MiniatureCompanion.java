package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.goal.HorseWanderBoundsGoal;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public final class MiniatureCompanion implements BreedAbility {

    private static final TagKey<Item> PRODUCE = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "farm_produce"));

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.tickCount % 10 != 0) {
            return;
        }
        Predicate<ItemStack> wanted = wants(horse, data);
        if (wanted != null) {
            DonkeyForaging.pickup(horse, data, wanted);
        }
    }

    private static @Nullable Predicate<ItemStack> wants(AbstractHorse horse, IHorseData data) {
        if (!(horse.level() instanceof ServerLevel) || horse.isBaby() || data.bh_isAbilityPaused()
                || !data.bh_hasChestGear()) {
            return null;
        }
        boolean eggs = BhAbility.MINIATURE_PICKUP.on();
        boolean basket = BhAbility.MINIATURE_BASKET.on() && BhHorseTraits.bondTier(data.bh_getBond()) >= 1;
        if (!eggs && !basket) {
            return null;
        }
        return stack -> eggs && stack.is(Tags.Items.EGGS)
                || basket && (stack.is(ItemTags.WOOL) || stack.is(Tags.Items.CROPS) || stack.is(PRODUCE));
    }

    public static final class FetchGoal extends Goal {

        private static final double SCAN = 16.0D;

        private final AbstractHorse horse;
        private final Set<UUID> skipped = new HashSet<>();
        private @Nullable ItemEntity target;
        private int nextScan;
        private int forget;
        private int stuck;

        public FetchGoal(AbstractHorse horse) {
            this.horse = horse;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (horse.tickCount < nextScan || wanted() == null) {
                return false;
            }
            nextScan = horse.tickCount + 40;
            if (horse.tickCount >= forget) {
                skipped.clear();
                forget = horse.tickCount + 1200;
            }
            target = next();
            return target != null;
        }

        private @Nullable Predicate<ItemStack> wanted() {
            if (horse.isVehicle() || horse.isLeashed() || !(horse instanceof IHorseAbilityHost host)
                    || host.bh_allAbilities().stream().noneMatch(ability -> ability instanceof MiniatureCompanion)) {
                return null;
            }
            IHorseData data = IHorseData.of(horse);
            if (!data.bh_isOwned() || data.bh_hasEnderChestGear() || data.bh_getCombatTarget() != null
                    || data.bh_getWanderCenter() == null
                    || !data.bh_getCommand().equals(BhContent.COMMAND_WANDER.getKey())) {
                return null;
            }
            return wants(horse, data);
        }

        private @Nullable ItemEntity next() {
            Predicate<ItemStack> wanted = wanted();
            IHorseData data = IHorseData.of(horse);
            BlockPos home = data.bh_getWanderCenter();
            if (wanted == null || home == null) {
                return null;
            }
            double reach = HorseWanderBoundsGoal.WANDER_HALF_EXTENT;
            ItemEntity best = null;
            double closest = Double.MAX_VALUE;
            for (ItemEntity item : horse.level().getEntitiesOfClass(ItemEntity.class,
                    horse.getBoundingBox().inflate(SCAN, 4.0D, SCAN))) {
                if (!item.isAlive() || skipped.contains(item.getUUID())
                        || Math.abs(item.getX() - home.getX() - 0.5D) > reach
                        || Math.abs(item.getZ() - home.getZ() - 0.5D) > reach
                        || item.getTarget() != null && !Objects.equals(item.getTarget(), data.bh_getOwner())
                        || !wanted.test(item.getItem()) || !DonkeyForaging.fits(data, item.getItem())) {
                    continue;
                }
                double dist = horse.distanceToSqr(item);
                if (dist < closest) {
                    best = item;
                    closest = dist;
                }
            }
            return best;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && wanted() != null;
        }

        @Override
        public void start() {
            stuck = 0;
            walk();
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }
            if (!target.isAlive() || ++stuck > 200) {
                if (target.isAlive()) {
                    skipped.add(target.getUUID());
                }
                target = next();
                stuck = 0;
                walk();
                return;
            }
            horse.getLookControl().setLookAt(target);
            if (horse.tickCount % 10 == 0 || horse.getNavigation().isDone()) {
                walk();
            }
        }

        private void walk() {
            if (target != null) {
                horse.getNavigation().moveTo(target, 1.0D);
            }
        }

        @Override
        public void stop() {
            horse.getNavigation().stop();
            target = null;
        }
    }
}
