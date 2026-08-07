package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;

/**
 * GeckoLib entity renderer for the cart. GeckoLib 5's {@link GeoEntityRenderer} already handles the
 * 1.21.11 extract→submit pipeline internally, so unlike the stabilizer wing layer this needs no
 * manual plumbing — just the model. {@code EntityRenderState} is the concrete render-state type
 * (GeckoLib mixes {@code GeoRenderState} into it).
 */
public final class HorseCartRenderer extends GeoEntityRenderer<HorseCartEntity, EntityRenderState> {

    /** Bone holding the cargo chest in {@code horse_cart.geo.json}. */
    private static final String CHEST_BONE = "chest";
    /**
     * Carries the cart's chest state into the render pass. Bone visibility has to be decided from
     * the render state rather than the entity: rendering runs off a snapshot taken during extract,
     * and the entity may have been re-ticked (or gone) by then.
     */
    private static final DataTicket<Boolean> HAS_CHEST =
            DataTicket.create("bh_cart_has_chest", Boolean.class);

    public HorseCartRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseCartGeoModel());
    }

    /**
     * The model always contains the chest; an unloaded cart simply skips that bone. One model with a
     * hidden bone beats two geometry files — the chest sits inside the same bounce animation as the
     * rest of the cart, so it stays glued to the bed while it rolls.
     */
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<EntityRenderState> pass, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(pass, snapshots);

        if (!pass.renderState().getOrDefaultGeckolibData(HAS_CHEST, false)) {
            snapshots.ifPresent(CHEST_BONE, snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
        }
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

        state.addGeckolibData(HAS_CHEST, entity.hasChest());

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
