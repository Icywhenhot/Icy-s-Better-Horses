package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.client.BhSlotFlash;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns clicks away from the horse GUI's cart slot while a chest is fitted to that cart, and flashes
 * the slot red so the refusal is visible rather than silent.
 *
 * <p>Targets {@link AbstractContainerScreen} rather than the horse screen only because
 * {@code AbstractMountInventoryScreen} doesn't override {@code slotClicked} — there is nothing on
 * the narrower class to inject into. Every other screen leaves here on the first check.</p>
 *
 * <p>The menu-side slot refuses the same click on its own (see {@code HorseInventoryMenuMixin}), so
 * this is feedback and packet-avoidance, not the actual guard.</p>
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow public abstract AbstractContainerMenu getMenu();

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void bh_refuseLockedCartSlot(
            Slot slot, int slotId, int mouseButton, ContainerInput input, CallbackInfo ci) {
        if (!(this.getMenu() instanceof HorseInventoryLayoutAccess layoutAccess)
                || !layoutAccess.bh_isCartSlotLocked()) {
            return;
        }

        int gearStartIndex = layoutAccess.bh_getGearStartIndex();
        // -1 until the gear slots have been appended; without this guard the offset below would
        // land on an unrelated slot index.
        if (gearStartIndex < 0 || slotId != gearStartIndex + GearSlot.STABILIZER.ordinal()) {
            return;
        }

        BhSlotFlash.trigger();
        ci.cancel();
    }
}
