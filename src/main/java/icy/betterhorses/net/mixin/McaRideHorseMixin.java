package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.interaction.EntityCommandHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.conczin.mca.entity.interaction.VillagerCommandHandler", remap = false)
public abstract class McaRideHorseMixin extends EntityCommandHandler<VillagerEntityMCA> {

    protected McaRideHorseMixin(VillagerEntityMCA entity) {
        super(entity);
    }

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, require = 0)
    private void bh_rideBehindPlayer(ServerPlayer player, String command, CallbackInfoReturnable<Boolean> cir) {
        if (!"ridehorse".equals(command) || entity.isPassenger()) return;
        if (player.getVehicle() instanceof AbstractHorse horse && BhHorseKind.managed(horse)
                && entity.startRiding(horse, false)) {
            entity.sendChatMessage(player, "interaction.ridehorse.success");
            cir.setReturnValue(true);
        }
    }
}
