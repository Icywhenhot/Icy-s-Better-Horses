package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedMule;
import icy.betterhorses.net.entity.BhUndeadHorse;
import icy.betterhorses.net.feature.Stabilizer;
import icy.betterhorses.net.inventory.GearSlot;
import icy.betterhorses.net.registry.GenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.Entity;

public final class BhHorseInteraction {

    private static final int GELD_CONFIRM_TICKS = 60;
    private static final String MSG = "message.icys-better-horses.geld.";

    private static final Map<UUID, UUID> geldAsked = new HashMap<>();
    private static final Map<UUID, Long> geldAskedAt = new HashMap<>();

    private BhHorseInteraction() {}

    public static @Nullable InteractionResult geld(
            AbstractHorse horse, IHorseData data, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.SHEARS) || !player.isSecondaryUseActive() || !horse.isTamed()
                || !BhFeature.GELDING.on()) {
            return null;
        }
        if (horse.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        UUID owner = data.bh_getOwner() != null ? data.bh_getOwner() : horse.getOwnerUUID();
        String why = null;
        if (!player.getUUID().equals(owner)) {
            why = "not_owner";
        } else if (horse instanceof Mule || horse instanceof BhBreedMule || horse instanceof BhUndeadHorse) {
            why = "cant";
        } else if (data.bh_isGelded()) {
            why = "already";
        } else if (horse.isBaby()) {
            why = "baby";
        }
        if (why != null) {
            player.displayClientMessage(Component.translatable(MSG + why, horse.getDisplayName()), true);
            return InteractionResult.CONSUME;
        }

        long now = horse.level().getGameTime();
        UUID id = player.getUUID();
        Long at = geldAskedAt.get(id);
        if (!horse.getUUID().equals(geldAsked.get(id)) || at == null || now - at > GELD_CONFIRM_TICKS) {
            geldAsked.put(id, horse.getUUID());
            geldAskedAt.put(id, now);
            player.displayClientMessage(Component.translatable(MSG + "confirm", horse.getDisplayName()), true);
            return InteractionResult.CONSUME;
        }
        geldAsked.remove(id);
        geldAskedAt.remove(id);

        data.bh_setGelded(true);
        held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        horse.level().playSound(null, horse, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
        horse.playSound(SoundEvents.HORSE_ANGRY, 0.8F, 1.0F);
        player.displayClientMessage(Component.translatable(MSG + "done", horse.getDisplayName(),
                GenderType.displayName(data.bh_getGender(), true)), false);
        return InteractionResult.CONSUME;
    }

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
        if (cart && !data.bh_mayPullCart()) {
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
        horse.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
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
        if (BhHorseSteering.IMMERSIVE_RIDING) {
            return false;
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
        horse.fallDistance = 0.0F;
        return StabilizerLanding.ABSORBED;
    }
}
