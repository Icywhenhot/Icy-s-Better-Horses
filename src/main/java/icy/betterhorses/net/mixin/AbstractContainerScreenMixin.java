package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.client.BhSlotFlash;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// turns clicks away from the horse GUI slots that a hitched cart holds shut, and says why
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    // the horse menu keeps the saddle at slot 0
    @Unique private static final int BH_SADDLE_SLOT = 0;

    @Shadow public abstract AbstractContainerMenu getMenu();

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void bh_refuseLockedSlot(
            Slot slot, int slotId, int mouseButton, ContainerInput input, CallbackInfo ci) {
        if (!(this.getMenu() instanceof HorseInventoryLayoutAccess layoutAccess)) {
            return;
        }

        int gearStartIndex = layoutAccess.bh_getGearStartIndex();
        // -1 until the gear slots have been appended, so the offset below would land on another slot
        boolean cartSlot = gearStartIndex >= 0 && slotId == gearStartIndex + GearSlot.STABILIZER.ordinal();

        if (cartSlot && layoutAccess.bh_isCartSlotLocked()) {
            bh_refuse(slotId, "message.icys-better-horses.cart_chest_locked");
            ci.cancel();
            return;
        }
        if (slotId == BH_SADDLE_SLOT && layoutAccess.bh_isSaddleSlotLocked()) {
            bh_refuse(slotId, "message.icys-better-horses.saddle_cart_attached");
            ci.cancel();
        }
    }

    // flash the slot red and put the reason in chat. chat rather than the action bar because the hud
    // isn't drawn while a container screen is open
    @Unique
    private static void bh_refuse(int slotId, String messageKey) {
        BhSlotFlash.trigger(slotId);
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.translatable(messageKey));
        }
    }
}
