package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.client.mixin.AbstractEquineModelAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class HorseStabilizerLayer<S extends EquineRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {
    private static final HorseStabilizerGeoRenderer GEO_RENDERER = new HorseStabilizerGeoRenderer();
    private static final double TORSO_X_OFFSET = 8.0D / 16.0D;
    private static final double FEET_Y_IN_FLIPPED_FRAME = 1.85D;
    private static final double TORSO_Z_OFFSET = -8.0D / 16.0D;
    private static final float MODEL_ROLL_DEGREES = 180.0F;

    public HorseStabilizerLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       S state, float yRot, float xRot) {
        if (state.isInvisible || state.isBaby) {
            return;
        }
        if (!(state instanceof BhStabilizerStateAccess access) || !access.bh_hasStabilizer()) {
            return;
        }

        HorseStabilizerState stabilizerState = access.bh_getStabilizerState();
        HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.get(access.bh_getHorseId());
        animatable.syncFromState(stabilizerState, state.ageInTicks);

        EntityModel<? super S> parent = this.getParentModel();
        if (!(parent instanceof AbstractEquineModel)) {
            return;
        }
        ModelPart body = ((AbstractEquineModelAccessor) parent).bh_getBody();

        poseStack.pushPose();
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -body.x / 16.0F + TORSO_X_OFFSET,
                FEET_Y_IN_FLIPPED_FRAME - body.y / 16.0F,
                -body.z / 16.0F + TORSO_Z_OFFSET);
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        GEO_RENDERER.submit(
                poseStack,
                animatable,
                null,
                collector,
                bh_getCameraState(),
                packedLight,
                partialTick,
                null);

        poseStack.popPose();
    }

    private static CameraRenderState bh_getCameraState() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level != null) {
            return minecraft.gameRenderer.getLevelRenderState().cameraRenderState;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        CameraRenderState cameraState = new CameraRenderState();
        cameraState.blockPos = camera.getBlockPosition();
        cameraState.pos = camera.getPosition();
        cameraState.initialized = camera.isInitialized();
        cameraState.entityPos = camera.getEntity() != null ? camera.getEntity().position() : cameraState.pos;
        cameraState.orientation = new Quaternionf(camera.rotation());
        return cameraState;
    }
}
