package icy.betterhorses.net;

import icy.betterhorses.net.feature.Stabilizer;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import net.minecraft.world.entity.Entity;

public final class BhHorseInteraction {

    private BhHorseInteraction() {}

    public static @Nullable InteractionResult equipGearFromHand(
            AbstractHorse horse, IHorseData data, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean cart = held.is(ModItems.HORSE_CART);
        boolean stabilizer = held.is(ModItems.HORSE_STABILIZER);
        if ((!cart && !stabilizer) || player.isSecondaryUseActive()) {
            return null;
        }
        if (!data.bh_hasUpgradedSaddle()
                || !data.bh_getGearContainer().getItem(GearSlot.STABILIZER.ordinal()).isEmpty()) {
            return null;
        }
        if (stabilizer && !(horse instanceof Horse)) {
            return null;
        }
        if (horse.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (BhConfig.horseExclusivityEnabled() && !data.bh_mayHandle(player.getUUID())) {
            horse.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.icys-better-horses.not_inventory_owner"));
            }
            return InteractionResult.CONSUME;
        }

        data.bh_getGearContainer().setItem(GearSlot.STABILIZER.ordinal(), held.copyWithCount(1));
        held.consume(1, player);
        horse.playSound(SoundEvents.HORSE_SADDLE.value(), 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    public static boolean blockNonOwnerInventoryAccess(AbstractHorse horse, IHorseData data, Player player) {
        if (horse.level().isClientSide() || !BhConfig.horseExclusivityEnabled()) {
            return false;
        }
        if (data.bh_mayHandle(player.getUUID())) {
            return false;
        }
        horse.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.icys-better-horses.not_inventory_owner"));
        }
        return true;
    }

    public static boolean rotateHorseInsteadOfPlayer(AbstractHorse horse, IHorseData data, Player player) {
        if (horse.level().isClientSide()) {
            return false;
        }

        if (BhConfig.horseExclusivityEnabled()
                && !data.bh_maySaddleUp(player.getUUID())
                && !riderMayLeadPillion(horse, data)) {
            return true;
        }
        horse.setYRot(player.getYRot());
        horse.yRotO = horse.getYRot();
        horse.setYHeadRot(player.getYHeadRot());
        horse.setXRot(player.getXRot());

        player.startRiding(horse);

        player.setYRot(horse.getYRot());
        player.yRotO = horse.yRotO;
        player.setXRot(horse.getXRot());
        return true;
    }

    public static boolean riderMayLeadPillion(AbstractHorse horse, IHorseData data) {
        List<Entity> passengers = horse.getPassengers();
        return !passengers.isEmpty() && data.bh_maySaddleUp(passengers.get(0).getUUID());
    }

    public enum StabilizerLanding {
        NOT_EQUIPPED,
        PASS_THROUGH,
        ABSORBED
    }

    public static StabilizerLanding stabilizerLanding(AbstractHorse horse, IHorseData data, double distance) {
        if (!Stabilizer.hasStabilizerGear(data)) {
            return StabilizerLanding.NOT_EQUIPPED;
        }
        HorseStabilizerState landingState = HorseStabilizerLogic.resolveLandingState(
                true, (float) distance, data.bh_getStabilizerState());
        if (landingState == HorseStabilizerState.CLOSED) {
            return StabilizerLanding.PASS_THROUGH;
        }

        if (distance > 1.0D) {
            horse.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
        }
        if (!horse.level().isClientSide()) {
            data.bh_setStabilizerState(landingState);
        }
        horse.fallDistance = 0.0D;
        if (!horse.level().isClientSide()) {
            for (Entity passenger : horse.getIndirectPassengers()) {
                if (passenger instanceof ServerPlayer serverPlayer) {
                    BhCriteria.fire(serverPlayer, BhCriteria.STABILIZER_LANDING, (int) distance);
                }
            }
        }
        return StabilizerLanding.ABSORBED;
    }
}
