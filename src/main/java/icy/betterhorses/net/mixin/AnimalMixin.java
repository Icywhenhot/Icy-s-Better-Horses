package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseSpawnRules;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public abstract class AnimalMixin {

    // Relax the grass-only ground check so horses can spawn on sand/snow/dirt/gravel/ice/stone.
    @Inject(method = "checkAnimalSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void bh_relaxHorseGroundCheck(EntityType<? extends Animal> type,
                                                 LevelAccessor level,
                                                 MobSpawnType reason,
                                                 BlockPos pos,
                                                 RandomSource random,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (BhHorseSpawnRules.appliesTo(type)) {
            cir.setReturnValue(BhHorseSpawnRules.checkHorseLikeGroundRules(type, level, reason, pos));
        }
    }

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void bh_blockSameGenderBreeding(Animal other, CallbackInfoReturnable<Boolean> cir) {
        Animal self = (Animal) (Object) this;
        if (!(self instanceof AbstractHorse selfHorse) || !(other instanceof AbstractHorse otherHorse)) {
            return;
        }
        HorseGender selfGender = ((IHorseData) selfHorse).bh_getGender();
        HorseGender otherGender = ((IHorseData) otherHorse).bh_getGender();
        if (selfGender == otherGender) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("TAIL"))
    private void bh_finalizeHorseChild(ServerLevel level, Animal partner, AgeableMob child, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        if (!(self instanceof AbstractHorse selfHorse)
                || !(partner instanceof AbstractHorse partnerHorse)
                || !(child instanceof AbstractHorse childHorse)) {
            return;
        }

        IHorseData selfData = (IHorseData) selfHorse;
        IHorseData partnerData = (IHorseData) partnerHorse;
        IHorseData childData = (IHorseData) childHorse;

        childData.bh_setGender(self.getRandom().nextBoolean() ? HorseGender.MALE : HorseGender.FEMALE);

        // Breed inheritance only applies between two real horses (not donkey/mule mixes).
        HorseBreed selfBreed = bh_resolveBreed(selfHorse, selfData);
        HorseBreed partnerBreed = bh_resolveBreed(partnerHorse, partnerData);

        if (childHorse instanceof Horse && selfBreed.isRealBreed() && partnerBreed.isRealBreed()) {
            if (selfBreed == partnerBreed) {
                childData.bh_setBreed(selfBreed);
                childData.bh_setMixedBreed(false);
            } else {
                HorseBreed chosen = self.getRandom().nextBoolean() ? selfBreed : partnerBreed;
                childData.bh_setBreed(chosen);
                childData.bh_setMixedBreed(true);
            }
        } else {
            // Cross-species (e.g. horse + donkey -> mule): use the species placeholder.
            HorseBreed species = HorseBreed.speciesFor(childHorse);
            childData.bh_setBreed(species != null ? species : HorseBreed.UNKNOWN_SPECIES);
            childData.bh_setMixedBreed(false);
        }

        // Stat inheritance: max(parents) + random delta, clamped at the vanilla base ceiling.
        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.MAX_HEALTH, VANILLA_MAX_HEALTH, HEALTH_DISPLAY_PER_RAW);
        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.MOVEMENT_SPEED, VANILLA_MAX_SPEED, SPEED_DISPLAY_PER_RAW);
        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.JUMP_STRENGTH, VANILLA_MAX_JUMP, JUMP_DISPLAY_PER_RAW);

        childHorse.setHealth(childHorse.getMaxHealth());

        // Coat follows the child's assigned breed.
        if (childHorse instanceof Horse childHorseEntity) {
            HorseBreed childBreed = childData.bh_getBreed();
            HorseBreed.Coat coat = childBreed.rollCoat(self.getRandom());
            if (coat != null) {
                ((HorseAccessor) childHorseEntity).bh_setVariantAndMarkings(coat.color(), coat.markings());
            }
        }
    }

    // Vanilla horse base attribute ceilings — these are the caps breeding can never exceed.
    private static final double VANILLA_MAX_HEALTH = 30.0D;
    private static final double VANILLA_MAX_SPEED = 0.3375D;
    private static final double VANILLA_MAX_JUMP = 1.0D;

    // display_value = raw * factor (speed blocks/sec = raw*43.2, jump slope = 6, health = raw).
    private static final double SPEED_DISPLAY_PER_RAW = 43.2D;
    private static final double JUMP_DISPLAY_PER_RAW = 6.0D;
    private static final double HEALTH_DISPLAY_PER_RAW = 1.0D;

    // Variance is uniform in display units: at worst 0.5 worse than the better parent, at best 1.0 better.
    private static final double VARIANCE_DISPLAY_MIN = -0.5D;
    private static final double VARIANCE_DISPLAY_MAX = 1.0D;

    // Stored breed, or (for a real Horse tagged UNKNOWN) one matched from its coat.
    private static HorseBreed bh_resolveBreed(AbstractHorse parent, IHorseData parentData) {
        HorseBreed stored = parentData.bh_getBreed();
        if (stored.isRealBreed() || !(parent instanceof Horse horseParent)) {
            return stored;
        }
        java.util.List<HorseBreed> matches = HorseBreed.breedsMatchingCoat(
                horseParent.getVariant(), horseParent.getMarkings());
        HorseBreed picked = matches.isEmpty()
                ? HorseBreed.MUSTANG
                : matches.get(parent.getRandom().nextInt(matches.size()));
        parentData.bh_setBreed(picked);
        return picked;
    }

    private static void bh_inheritBetterStat(AbstractHorse p1, AbstractHorse p2, AbstractHorse child,
                                             Holder<Attribute> attr, double cap, double displayPerRaw) {
        AttributeInstance p1Attr = p1.getAttribute(attr);
        AttributeInstance p2Attr = p2.getAttribute(attr);
        AttributeInstance childAttr = child.getAttribute(attr);
        if (p1Attr == null || p2Attr == null || childAttr == null) return;

        double best = Math.max(p1Attr.getBaseValue(), p2Attr.getBaseValue());
        double deltaDisplay = VARIANCE_DISPLAY_MIN
                + p1.getRandom().nextDouble() * (VARIANCE_DISPLAY_MAX - VARIANCE_DISPLAY_MIN);
        double deltaRaw = deltaDisplay / displayPerRaw;
        childAttr.setBaseValue(Math.max(0.0D, Math.min(cap, best + deltaRaw)));
    }
}
