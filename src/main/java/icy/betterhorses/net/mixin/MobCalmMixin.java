package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ICalmable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Mob.class)
public abstract class MobCalmMixin implements ICalmable {

    @Unique private @Nullable UUID bh$calmRider;
    @Unique private @Nullable UUID bh$calmHorse;
    @Unique private long bh$calmUntil;
    @Unique private int bh$calmSince;

    @Override
    public void bh_calm(UUID rider, UUID horse, long until) {
        Mob self = (Mob) (Object) this;
        if (!rider.equals(this.bh$calmRider) || !this.bh$active()) {
            this.bh$calmSince = self.tickCount;
        }
        this.bh$calmRider = rider;
        this.bh$calmHorse = horse;
        this.bh$calmUntil = until;
    }

    @Override
    public void bh_releaseCalm() {
        this.bh$calmRider = null;
        this.bh$calmHorse = null;
        this.bh$calmUntil = 0L;
    }

    @Override
    public boolean bh_calmedBy(UUID rider) {
        return rider.equals(this.bh$calmRider) && this.bh$active();
    }

    @Override
    public boolean bh_calmedAgainst(@Nullable Entity target) {
        return target != null && this.bh$active()
                && (target.getUUID().equals(this.bh$calmRider) || target.getUUID().equals(this.bh$calmHorse));
    }

    @Unique
    private boolean bh$active() {
        if (this.bh$calmRider == null) {
            return false;
        }
        Mob self = (Mob) (Object) this;
        if (self.level().getGameTime() >= this.bh$calmUntil) {
            this.bh_releaseCalm();
            return false;
        }
        LivingEntity attacker = self.getLastHurtByMob();
        if (attacker != null && self.getLastHurtByMobTimestamp() >= this.bh$calmSince
                && (attacker.getUUID().equals(this.bh$calmRider) || attacker.getUUID().equals(this.bh$calmHorse))) {
            this.bh_releaseCalm();
            return false;
        }
        return true;
    }

    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity bh$refuseCalmedTarget(LivingEntity target) {
        return this.bh_calmedAgainst(target) ? null : target;
    }

    @Inject(method = "serverAiStep", at = @At("TAIL"))
    private void bh$dropCalmedTarget(CallbackInfo ci) {
        if (this.bh$calmRider == null || !this.bh$active()) {
            return;
        }
        Mob self = (Mob) (Object) this;
        if (this.bh_calmedAgainst(self.getTarget())) {
            self.setTarget(null);
        }
        Brain<?> brain = self.getBrain();
        if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)
                && this.bh_calmedAgainst(brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null))) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        if (brain.checkMemory(MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED)) {
            UUID grudge = brain.getMemory(MemoryModuleType.ANGRY_AT).orElse(null);
            if (grudge != null && (grudge.equals(this.bh$calmRider) || grudge.equals(this.bh$calmHorse))) {
                brain.eraseMemory(MemoryModuleType.ANGRY_AT);
            }
        }
    }
}
