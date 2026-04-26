package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (!(self instanceof AbstractHorse) || !(self instanceof IHorseData data)) {
            return;
        }

        if (self.level().isClientSide() || self.isInvulnerableTo(source) || !this.bh_hasEquippedMedkit(data)) {
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
        if (!(self instanceof AbstractHorse) || !(self instanceof IHorseData data)) {
            return;
        }

        this.bh_consumeMedkitAndApplyEffects(self, data);
    }

    @Unique
    private boolean bh_hasEquippedMedkit(IHorseData data) {
        return data.bh_getGearContainer().getItem(GearSlot.MEDKIT.ordinal()).is(ModItems.HORSE_MEDKIT);
    }

    @Unique
    private float bh_calculateHealthDamage(LivingEntity self, DamageSource source, float amount) {
        float mitigatedDamage = this.getDamageAfterArmorAbsorb(source, amount);
        mitigatedDamage = this.getDamageAfterMagicAbsorb(source, mitigatedDamage);
        return Math.max(mitigatedDamage - self.getAbsorptionAmount(), 0.0F);
    }

    @Unique
    private void bh_consumeMedkitAndApplyEffects(LivingEntity self, IHorseData data) {
        if (self.level().isClientSide() || !this.bh_hasEquippedMedkit(data)) {
            return;
        }

        SimpleContainer gear = data.bh_getGearContainer();
        gear.setItem(GearSlot.MEDKIT.ordinal(), ItemStack.EMPTY);
        gear.setChanged();

        self.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BH_MEDKIT_EFFECT_DURATION, 0));
        self.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 0));
        self.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BH_MEDKIT_EFFECT_DURATION, 0));
        self.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, BH_MEDKIT_EFFECT_DURATION, 0));
    }
}
