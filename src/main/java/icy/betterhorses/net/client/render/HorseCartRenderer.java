package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib entity renderer for the cart. GeckoLib 5's {@link GeoEntityRenderer} already handles the
 * 1.21.11 extract→submit pipeline internally, so unlike the stabilizer wing layer this needs no
 * manual plumbing — just the model. {@code EntityRenderState} is the concrete render-state type
 * (GeckoLib mixes {@code GeoRenderState} into it).
 */
public final class HorseCartRenderer extends GeoEntityRenderer<HorseCartEntity, EntityRenderState> {
    public HorseCartRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseCartGeoModel());
    }

    /**
     * Draw the cart at the horse's interpolated position <i>and</i> yaw rather than its own.
     * {@code LevelRenderer} positions every entity straight from {@code EntityRenderState.x/y/z}, and
     * GeckoLib rotates the model from the {@code ENTITY_YAW}/{@code ENTITY_BODY_YAW} render data — so
     * overriding both here pins the cart to the horse for the exact partial tick being drawn. The
     * yaw override is what removes the turn jitter: GeckoLib's default for a non-living entity is the
     * un-interpolated {@code getYRot()}, which steps once per tick while the position glides.
     */
    @Override
    public void extractRenderState(HorseCartEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        Vec3 glued = entity.gluedRenderPosition(partialTick);
        if (glued != null) {
            state.x = glued.x;
            state.y = glued.y;
            state.z = glued.z;

            float yaw = entity.gluedRenderYaw(partialTick);
            state.addGeckolibData(DataTickets.ENTITY_YAW, yaw);
            state.addGeckolibData(DataTickets.ENTITY_BODY_YAW, yaw);
        }
    }
}
