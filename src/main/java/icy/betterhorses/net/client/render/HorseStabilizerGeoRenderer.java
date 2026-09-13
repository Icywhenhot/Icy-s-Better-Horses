package icy.betterhorses.net.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;

public final class HorseStabilizerGeoRenderer
        extends GeoObjectRenderer<HorseStabilizerAnimatable, Void, GeoRenderState.Impl> {
    private static final DataTicket<Boolean> WINGS_ACTIVE =
            DataTicket.create("icys_better_horses_stabilizer_wings_active", Boolean.class);
    private static final DataTicket<Float> OPACITY =
            DataTicket.create("icys_better_horses_stabilizer_opacity", Float.class);

    public HorseStabilizerGeoRenderer() {
        this(new HorseStabilizerGeoModel());
    }

    public HorseStabilizerGeoRenderer(GeoModel<HorseStabilizerAnimatable> model) {
        super(model);
    }

    @Override
    public void addRenderData(
            HorseStabilizerAnimatable animatable,
            Void relatedObject,
            GeoRenderState.Impl renderState,
            float partialTick) {
        renderState.addGeckolibData(WINGS_ACTIVE, animatable.isActive());
        renderState.addGeckolibData(OPACITY, BhRenderContext.currentOpacity());
    }

    @Override
    public int getRenderColor(HorseStabilizerAnimatable animatable, Void relatedObject, float partialTick) {
        return BhMountedHorseVisibility.applyOpacity(
                super.getRenderColor(animatable, relatedObject, partialTick),
                BhRenderContext.currentOpacity());
    }

    @Override
    public RenderType getRenderType(GeoRenderState.Impl renderState, Identifier texture) {
        return renderState.getOrDefaultGeckolibData(OPACITY, 1.0F) < 1.0F
                ? RenderTypes.entityTranslucent(texture)
                : super.getRenderType(renderState, texture);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState.Impl> renderPassInfo) {
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
