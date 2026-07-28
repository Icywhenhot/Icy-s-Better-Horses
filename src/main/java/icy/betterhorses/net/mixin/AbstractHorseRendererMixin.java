package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.client.render.BhMountedHorseVisibility;
import icy.betterhorses.net.client.render.HorseChestLayer;
import icy.betterhorses.net.client.render.HorseStabilizerAnimatable;
import icy.betterhorses.net.client.render.HorseStabilizerLayer;
import icy.betterhorses.net.client.render.IBhEquineStabilizerState;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the stabilizer wing layer to every {@link AbstractHorseRenderer}, and forwards the
 * relevant entity state into {@link IBhEquineStabilizerState} so the layer can access it
 * during the new state-based submit phase.
 *
 * 1.21.5+ rebuild:
 *  - {@link AbstractHorseRenderer} now has three type parameters {@code <T, S, M>} where {@code S}
 *    is an {@link EquineRenderState} and {@code M} an {@link EntityModel} parameterised by it.
 *  - The constructor signature is {@code (Context, M, M)} (two models — adult/baby), no longer
 *    {@code (Context, M, float)}, so the {@code @Inject} parameter list must match.
 *  - It extends {@link AgeableMobRenderer} (which itself extends {@code MobRenderer}); we extend
 *    that here so the mixin can call inherited methods like {@code addLayer}.
 *  - Per-frame entity data has to be captured in {@code extractRenderState}: by the time
 *    {@code submit} runs the entity is no longer reachable, only the render state is.
 */
@Mixin(AbstractHorseRenderer.class)
public abstract class AbstractHorseRendererMixin<
        T extends AbstractHorse,
        S extends EquineRenderState,
        M extends EntityModel<? super S>>
        extends AgeableMobRenderer<T, S, M> {

    protected AbstractHorseRendererMixin(EntityRendererProvider.Context context, M adultModel, M babyModel, float shadowRadius) {
        super(context, adultModel, babyModel, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_addStabilizerLayer(EntityRendererProvider.Context context, M adultModel, M babyModel, CallbackInfo ci) {
        this.addLayer(new HorseStabilizerLayer<>(this));
        this.addLayer(new HorseChestLayer<>(this));
    }

    /**
     * Capture the current stabilizer flag/state from the live horse onto the render state, and
     * keep the per-horse {@link HorseStabilizerAnimatable} ticking in sync (so its
     * {@code AnimationController} fires deploy / glide transitions at the right moments).
     */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void bh_captureStabilizerState(T entity, S state, float partialTick, CallbackInfo ci) {
        IBhEquineStabilizerState extState = (IBhEquineStabilizerState) state;
        Minecraft minecraft = Minecraft.getInstance();
        boolean riddenByPlayerInFirstPerson = minecraft.options.getCameraType() == CameraType.FIRST_PERSON
                && minecraft.player != null
                && entity.hasPassenger(minecraft.player);
        extState.bh_setMountedViewData(riddenByPlayerInFirstPerson, BhMountedHorseVisibility.getOpacity(entity));

        if (!(entity instanceof IHorseData data)) {
            extState.bh_setStabilizerData(false, HorseStabilizerState.CLOSED, entity.getId(), partialTick);
            extState.bh_setChestGear(false);
            return;
        }

        extState.bh_setChestGear(data.bh_hasChestGear());

        boolean hasStabilizer = data.bh_hasStabilizerItem();
        extState.bh_setStabilizerData(
                hasStabilizer, data.bh_getStabilizerState(), entity.getId(), partialTick);

        if (hasStabilizer) {
            HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.get(entity);
            animatable.syncFromHorse(entity, data.bh_getStabilizerState());
        }
    }

    @Unique private static final double BH_REIN_SIDE = 0.15D;
    @Unique private static final double BH_REIN_HEAD_HEIGHT = 1.45D;
    @Unique private static final double BH_REIN_HEAD_FORWARD = 0.75D;
    @Unique private static final double BH_REIN_HAND_HEIGHT = 0.9D;

    /**
     * Draws a pair of reins from the horse's head back to the driver's hands whenever a cart is
     * hitched and someone is in the driver's seat.
     *
     * <p>Reuses vanilla's leash rope rendering: {@code EntityRenderer.submit} draws every entry in
     * {@code EntityRenderState.leashStates}, so we just append two. The list is <b>reassigned</b>
     * rather than mutated because vanilla assigns it (often an immutable empty list when the entity
     * isn't leashed) and only ever grows it — appending in place would throw, or leak a stale rope
     * every frame.</p>
     */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void bh_extractCartReins(T entity, S state, float partialTick, CallbackInfo ci) {
        if (!(entity instanceof IHorseData data) || !data.bh_hasCartGear()) {
            return;
        }
        Entity driver = entity.getFirstPassenger();
        if (driver == null) {
            return;
        }

        float radians = -entity.getYRot(partialTick) * ((float) Math.PI / 180.0F);
        Vec3 horsePos = entity.getPosition(partialTick);
        Vec3 driverPos = driver.getPosition(partialTick);
        Level level = entity.level();

        // Null (not an empty list) when the entity isn't leashed.
        List<EntityRenderState.LeashState> reins = state.leashStates == null
                ? new ArrayList<>()
                : new ArrayList<>(state.leashStates);
        for (int i = 0; i < 2; i++) {
            double side = i == 0 ? -BH_REIN_SIDE : BH_REIN_SIDE;
            Vec3 headOffset = new Vec3(side, BH_REIN_HEAD_HEIGHT, BH_REIN_HEAD_FORWARD).yRot(radians);
            Vec3 handOffset = new Vec3(side, BH_REIN_HAND_HEIGHT, 0.0D).yRot(radians);

            EntityRenderState.LeashState rein = new EntityRenderState.LeashState();
            rein.offset = headOffset;
            rein.start = horsePos.add(headOffset);
            rein.end = driverPos.add(handOffset);
            rein.slack = true;

            BlockPos startBlock = BlockPos.containing(rein.start);
            BlockPos endBlock = BlockPos.containing(rein.end);
            rein.startBlockLight = level.getBrightness(LightLayer.BLOCK, startBlock);
            rein.startSkyLight = level.getBrightness(LightLayer.SKY, startBlock);
            rein.endBlockLight = level.getBrightness(LightLayer.BLOCK, endBlock);
            rein.endSkyLight = level.getBrightness(LightLayer.SKY, endBlock);

            reins.add(rein);
        }
        state.leashStates = reins;
    }
}
