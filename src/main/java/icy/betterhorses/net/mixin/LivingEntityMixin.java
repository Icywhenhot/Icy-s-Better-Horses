package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.feature.breed.ArchetypePerks;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.HardyNorthern;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Unique private static final float BH_MEDKIT_HEALTH_THRESHOLD_FRACTION = 0.5F;
    @Unique private static final int BH_MEDKIT_EFFECT_DURATION = 20 * 30;
    @Unique private boolean bh_triggerHorseMedkitAfterDamage = false;

    protected LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected abstract float getDamageAfterArmorAbsorb(DamageSource source, float amount);

    @Shadow
    protected abstract float getDamageAfterMagicAbsorb(DamageSource source, float amount);

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void bh_queueHorseMedkit(DamageSource source, float amount, CallbackInfo ci) {
        this.bh_triggerHorseMedkitAfterDamage = false;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!BhHorseKind.managed(self) || !(self instanceof IHorseData data)) {
            return;
        }

        if (self.isInvulnerableTo(source) || !ArchetypePerks.hasMedkit(data) || icy.betterhorses.net.feature.breed.PegaCompanion.holdingKit(self)) {
            return;
        }

        float damageToHealth = this.bh_calculateHealthDamage(self, source, amount);
        float healthAfterDamage = self.getHealth() - damageToHealth;
        if (healthAfterDamage < self.getMaxHealth() * BH_MEDKIT_HEALTH_THRESHOLD_FRACTION) {
            this.bh_triggerHorseMedkitAfterDamage = true;
        }
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void bh_useHorseMedkit(DamageSource source, float amount, CallbackInfo ci) {
        if (!this.bh_triggerHorseMedkitAfterDamage) {
            return;
        }

        this.bh_triggerHorseMedkitAfterDamage = false;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!BhHorseKind.managed(self) || !(self instanceof IHorseData data)) {
            return;
        }

        ArchetypePerks.useMedkit(self, data);
    }


    @Unique
    private float bh_calculateHealthDamage(LivingEntity self, DamageSource source, float amount) {
        float mitigatedDamage = this.getDamageAfterArmorAbsorb(source, amount);
        mitigatedDamage = this.getDamageAfterMagicAbsorb(source, mitigatedDamage);
        return Math.max(mitigatedDamage - self.getAbsorptionAmount(), 0.0F);
    }


    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void bh_refuseBadEffects(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect.getEffect().isBeneficial()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (effect.getEffect().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL
                && icy.betterhorses.net.feature.breed.ThickHide.immune(self)) {
            cir.setReturnValue(false);
            return;
        }
        AbstractHorse warden = HardyNorthern.warden(self);
        if (warden == null) {
            return;
        }
        if (!warden.level().isClientSide()) {
            BhSurge.pulse(IHorseData.of(warden), 0, 0);
        }
        cir.setReturnValue(false);
    }
}
