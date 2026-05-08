package icy.betterhorses.net.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

/**
 * GeckoLib 5 widened {@link GeoObjectRenderer} from one type parameter to three:
 * {@code <T animatable, O relatedObject, R renderState>}. We don't need a related object,
 * so we use {@link Void}, and the default {@link GeoRenderState.Impl} suffices for the state.
 */
public final class HorseStabilizerGeoRenderer
        extends GeoObjectRenderer<HorseStabilizerAnimatable, Void, GeoRenderState.Impl> {
    private static final DataTicket<Boolean> WINGS_ACTIVE =
            DataTicket.create("icys_better_horses_stabilizer_wings_active", Boolean.class);

    public HorseStabilizerGeoRenderer() {
        super(new HorseStabilizerGeoModel());
    }

    @Override
    public void addRenderData(
            HorseStabilizerAnimatable animatable,
            Void relatedObject,
            GeoRenderState.Impl renderState,
            float partialTick) {
        renderState.addGeckolibData(WINGS_ACTIVE, animatable.isActive());
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState.Impl> renderPassInfo) {
        // The layer already anchors the stabilizer to the horse body. GeoObjectRenderer's default
        // +0.5/+0.51/+0.5 translation is for standalone objects and pushes the rig off the horse.
    }

    @Override
    public void adjustModelBonesForRender(
            RenderPassInfo<GeoRenderState.Impl> renderPassInfo,
            BoneSnapshots snapshots) {
        boolean showWings = Boolean.TRUE.equals(renderPassInfo.getGeckolibData(WINGS_ACTIVE));

        snapshots.ifPresent("wingsL", snapshot -> snapshot.skipRender(!showWings).skipChildrenRender(!showWings));
        snapshots.ifPresent("wingsL2", snapshot -> snapshot.skipRender(!showWings).skipChildrenRender(!showWings));
    }
}
