package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.feature.breed.ArchetypePerks;
import icy.betterhorses.net.feature.breed.HardyNorthern;
import icy.betterhorses.net.feature.breed.PegaCompanion;
import icy.betterhorses.net.feature.breed.ThickHide;
import icy.betterhorses.net.feature.breed.SlowBlockImmunity;
import icy.betterhorses.net.IHorseData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    @Inject(method = "lerpTo", at = @At("HEAD"))
    private void bh_snapFarHorseTeleport(double x, double y, double z, float yRot, float xRot, int steps, CallbackInfo ci) {
        if (this.level().isClientSide() && (Object) this instanceof AbstractHorse
                && this.distanceToSqr(x, y, z) > 4096.0D) {
            this.moveTo(x, y, z, yRot, xRot);
        }
    }

    @Unique private boolean bh_triggerHorseMedkitAfterDamage = false;

    protected LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected abstract float getDamageAfterArmorAbsorb(DamageSource source, float amount);

    @Shadow
    protected abstract float getDamageAfterMagicAbsorb(DamageSource source, float amount);

    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void bh_refuseBadEffects(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect.getEffect().value().isBeneficial()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL && ThickHide.immune(self)) {
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

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void bh_shrugOffSlowBlocks(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (SlowBlockImmunity.shrugsOffSlowBlockDamage((LivingEntity) (Object) this, source)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void bh_queueHorseMedkit(DamageSource source, float amount, CallbackInfo ci) {
        this.bh_triggerHorseMedkitAfterDamage = false;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!BhHorseKind.managed(self) || !(self instanceof IHorseData data)) {
            return;
        }

        if (self.isInvulnerableTo(source) || !ArchetypePerks.hasMedkit(data) || PegaCompanion.holdingKit(self)) {
            return;
        }

        float damageToHealth = this.bh_calculateHealthDamage(self, source, amount);
        float healthAfterDamage = self.getHealth() - damageToHealth;
        if (healthAfterDamage < self.getMaxHealth() * ArchetypePerks.MEDKIT_THRESHOLD) {
            this.bh_triggerHorseMedkitAfterDamage = true;
        }
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void bh_afterDamage(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!this.bh_triggerHorseMedkitAfterDamage) {
            return;
        }

        this.bh_triggerHorseMedkitAfterDamage = false;

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
}
