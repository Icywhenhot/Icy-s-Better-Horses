package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhCriteria;
import icy.betterhorses.net.BhHorseSpawnRules;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public abstract class AnimalMixin {

    @Inject(method = "checkAnimalSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void bh_relaxHorseGroundCheck(EntityType<? extends Animal> type,
                                                 LevelAccessor level,
                                                 EntitySpawnReason reason,
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
        if (!BhConfig.genderBreedingEnabled()
                || !(self instanceof AbstractHorse selfHorse) || !(other instanceof AbstractHorse otherHorse)) {
            return;
        }
        HorseGender selfGender = IHorseData.of(selfHorse).bh_getGender();
        HorseGender otherGender = IHorseData.of(otherHorse).bh_getGender();
        if (selfGender == otherGender) {
            cir.setReturnValue(false);
        }
    }

    @Unique private @Nullable ServerPlayer bh_breeder = null;

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
    private void bh_captureBreeder(ServerLevel level, Animal partner, AgeableMob child, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        ServerPlayer breeder = self.getLoveCause();
        this.bh_breeder = breeder != null ? breeder : partner.getLoveCause();
    }

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("TAIL"))
    private void bh_finalizeHorseChild(ServerLevel level, Animal partner, AgeableMob child, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        if (!(self instanceof AbstractHorse selfHorse)
                || !(partner instanceof AbstractHorse partnerHorse)
                || !(child instanceof AbstractHorse childHorse)) {
            return;
        }

        IHorseData selfData = IHorseData.of(selfHorse);
        IHorseData partnerData = IHorseData.of(partnerHorse);
        IHorseData childData = IHorseData.of(childHorse);

        childData.bh_setGender(self.getRandom().nextBoolean() ? HorseGender.MALE : HorseGender.FEMALE);

        HorseBreed selfBreed = selfData.bh_getBreed();
        HorseBreed partnerBreed = partnerData.bh_getBreed();
        if (selfBreed.isRealBreed() && partnerBreed.isRealBreed()) {
            if (selfBreed == partnerBreed) {
                childData.bh_setBreed(selfBreed);
                childData.bh_setMixedBreed(false);
            } else {
                HorseBreed chosen = self.getRandom().nextBoolean() ? selfBreed : partnerBreed;
                childData.bh_setBreed(chosen);
                childData.bh_setMixedBreed(true);
            }
        } else {
            HorseBreed species = HorseBreed.speciesFor(childHorse);
            childData.bh_setBreed(species != null ? species : HorseBreed.UNKNOWN_SPECIES);
            childData.bh_setMixedBreed(false);
        }

        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.MAX_HEALTH, VANILLA_MAX_HEALTH, HEALTH_DISPLAY_PER_RAW);
        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.MOVEMENT_SPEED, VANILLA_MAX_SPEED, SPEED_DISPLAY_PER_RAW);
        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.JUMP_STRENGTH, VANILLA_MAX_JUMP, JUMP_DISPLAY_PER_RAW);

        childHorse.setHealth(childHorse.getMaxHealth());

        if (childHorse instanceof Horse childHorseEntity) {
            HorseBreed childBreed = childData.bh_getBreed();
            HorseBreed.Coat coat = childBreed.rollCoat(self.getRandom());
            if (coat != null) {
                ((HorseAccessor) childHorseEntity).bh_setVariantAndMarkings(coat.color(), coat.markings());
            }
        }

        BhCriteria.fire(this.bh_breeder, BhCriteria.FOAL);
        if (childData.bh_isMixedBreed()) {
            BhCriteria.fire(this.bh_breeder, BhCriteria.MIXED_FOAL);
        }
        this.bh_breeder = null;
    }

    private static final double VANILLA_MAX_HEALTH = 30.0D;
    private static final double VANILLA_MAX_SPEED = 0.3375D;
    private static final double VANILLA_MAX_JUMP = 1.0D;

    private static final double SPEED_DISPLAY_PER_RAW = 43.2D;
    private static final double JUMP_DISPLAY_PER_RAW = 6.0D;
    private static final double HEALTH_DISPLAY_PER_RAW = 1.0D;

    private static final double VARIANCE_DISPLAY_MIN = -0.5D;
    private static final double VARIANCE_DISPLAY_MAX = 1.0D;

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
