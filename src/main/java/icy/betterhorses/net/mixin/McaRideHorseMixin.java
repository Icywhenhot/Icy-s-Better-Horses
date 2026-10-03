package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "forge.net.conczin.mca.entity.interaction.VillagerCommandHandler", remap = false)
public abstract class McaRideHorseMixin {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, require = 0)
    private void bh_rideBehindPlayer(ServerPlayer player, String command, CallbackInfoReturnable<Boolean> cir) throws ReflectiveOperationException {
        if (!"ridehorse".equals(command)) return;
        Field field = getClass().getSuperclass().getDeclaredField("entity");
        field.setAccessible(true);
        Entity villager = (Entity) field.get(this);
        if (villager.isPassenger()) return;
        if (player.getVehicle() instanceof AbstractHorse horse && BhHorseKind.managed(horse)
                && villager.startRiding(horse, false)) {
            Method say = villager.getClass().getMethod("sendChatMessage", Player.class, String.class, Object[].class);
            say.invoke(villager, player, "interaction.ridehorse.success", new Object[0]);
            cir.setReturnValue(true);
        }
    }
}
