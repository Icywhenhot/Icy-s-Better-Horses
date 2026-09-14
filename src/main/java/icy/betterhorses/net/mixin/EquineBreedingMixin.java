package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Horse.class, Donkey.class})
public abstract class EquineBreedingMixin {

    @Unique private static final int BH_SCOLD_COOLDOWN = 600;

    @Unique private int bh_scoldAt;

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void bh_checkGender(Animal other, CallbackInfoReturnable<Boolean> cir) {
        if (!BhConfig.genderBreedingEnabled() || !(other instanceof AbstractHorse mate)) {
            return;
        }
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (IHorseData.of(self).bh_getGender() != IHorseData.of(mate).bh_getGender()) {
            return;
        }
        cir.setReturnValue(false);

        if (self.level().isClientSide() || !self.isInLove() || !mate.isInLove()
                || self.tickCount < bh_scoldAt) {
            return;
        }
        bh_scoldAt = self.tickCount + BH_SCOLD_COOLDOWN;
        ServerPlayer fedSelf = self.getLoveCause();
        ServerPlayer fedMate = mate.getLoveCause();
        bh_scold(fedSelf);
        if (fedMate != fedSelf) {
            bh_scold(fedMate);
        }
    }

    @Unique
    private static void bh_scold(@Nullable ServerPlayer player) {
        if (player != null) {
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.same_gender_breed"));
        }
    }
}
