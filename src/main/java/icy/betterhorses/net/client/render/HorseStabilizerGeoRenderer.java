package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorsesClient;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.BoneSnapshots;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * GeckoLib 5 widened {@link GeoObjectRenderer} from one type parameter to three:
 * {@code <T animatable, O relatedObject, R renderState>}. We don't need a related object,
 * so we use {@link Void}, and the default {@link GeoRenderState.Impl} suffices for the state.
 */
public final class HorseStabilizerGeoRenderer
        extends GeoObjectRenderer<HorseStabilizerAnimatable, Void, GeoRenderState.Impl> {
    private static final DataTicket<Boolean> WINGS_ACTIVE =
            DataTicket.create("icys_better_horses_stabilizer_wings_active", Boolean.class);

    // Throttled-log state for debugging
    private static long bh_addRenderDataLastLogMs = 0L;
    private static boolean bh_addRenderDataLastWingsActive = false;
    private static long bh_adjustLastLogMs = 0L;
    private static boolean bh_adjustLastShowWings = false;
    private static int bh_adjustLastWingsLPresent = -1;
    private static int bh_adjustLastWingsL2Present = -1;

    public HorseStabilizerGeoRenderer() {
        super(new HorseStabilizerGeoModel());
    }

    @Override
    public void addRenderData(
            HorseStabilizerAnimatable animatable,
            Void relatedObject,
            GeoRenderState.Impl renderState,
            float partialTick) {
        boolean wingsActive = animatable.isActive();
        renderState.addGeckolibData(WINGS_ACTIVE, wingsActive);

        long now = System.currentTimeMillis();
        if (wingsActive != bh_addRenderDataLastWingsActive || now - bh_addRenderDataLastLogMs > 1000L) {
            bh_addRenderDataLastWingsActive = wingsActive;
            bh_addRenderDataLastLogMs = now;
            IcysBetterHorsesClient.LOGGER.info(
                    "[STAB-DEBUG][RENDERER] addRenderData wingsActive={} state={}",
                    wingsActive, animatable.getState());
        }
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

        boolean[] wingsLPresent = {false};
        boolean[] wingsL2Present = {false};
        float[] wingsLScale = {Float.NaN, Float.NaN, Float.NaN};
        float[] wingsL2Scale = {Float.NaN, Float.NaN, Float.NaN};
        float[] wingsLRot = {Float.NaN, Float.NaN, Float.NaN};

        snapshots.ifPresent("wingsL", snapshot -> {
            wingsLPresent[0] = true;
            wingsLScale[0] = snapshot.getScaleX();
            wingsLScale[1] = snapshot.getScaleY();
            wingsLScale[2] = snapshot.getScaleZ();
            wingsLRot[0] = snapshot.getRotX();
            wingsLRot[1] = snapshot.getRotY();
            wingsLRot[2] = snapshot.getRotZ();
            snapshot.skipRender(!showWings).skipChildrenRender(!showWings);
        });
        snapshots.ifPresent("wingsL2", snapshot -> {
            wingsL2Present[0] = true;
            wingsL2Scale[0] = snapshot.getScaleX();
            wingsL2Scale[1] = snapshot.getScaleY();
            wingsL2Scale[2] = snapshot.getScaleZ();
            snapshot.skipRender(!showWings).skipChildrenRender(!showWings);
        });

        int wingsLPresentInt = wingsLPresent[0] ? 1 : 0;
        int wingsL2PresentInt = wingsL2Present[0] ? 1 : 0;
        long now = System.currentTimeMillis();
        boolean valuesChanged = showWings != bh_adjustLastShowWings
                || wingsLPresentInt != bh_adjustLastWingsLPresent
                || wingsL2PresentInt != bh_adjustLastWingsL2Present;
        if (valuesChanged || now - bh_adjustLastLogMs > 1000L) {
            bh_adjustLastShowWings = showWings;
            bh_adjustLastWingsLPresent = wingsLPresentInt;
            bh_adjustLastWingsL2Present = wingsL2PresentInt;
            bh_adjustLastLogMs = now;
            IcysBetterHorsesClient.LOGGER.info(
                    "[STAB-DEBUG][RENDERER] adjustModelBones showWings={} wingsL.present={} wingsL2.present={} wingsL.scale=[{},{},{}] wingsL.rot=[{},{},{}] wingsL2.scale=[{},{},{}]",
                    showWings, wingsLPresent[0], wingsL2Present[0],
                    wingsLScale[0], wingsLScale[1], wingsLScale[2],
                    wingsLRot[0], wingsLRot[1], wingsLRot[2],
                    wingsL2Scale[0], wingsL2Scale[1], wingsL2Scale[2]);
        }
    }
}
