package icy.betterhorses.net.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

// GeckoLib 5 widened GeoObjectRenderer from one type parameter to three: <T animatable
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
        // the layer already anchors the stabilizer to the horse body
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
