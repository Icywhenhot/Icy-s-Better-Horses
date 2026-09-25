package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.BhFeature;
import icy.betterhorses.net.api.HoofStepEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class HoofDustLayer<M extends BhHorseModel> extends RenderLayer<BhHorseRenderState, M> {

    private static final float HOOF_LIFT = 0.08F;
    private static final float HOOF_CONTACT = 0.03F;
    private static final int HOOF_DUST = 3;
    private static final double HOOF_DUST_SPREAD = 0.02;

    private final Vector3f[] hooves = {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};

    public HoofDustLayer(RenderLayerParent<BhHorseRenderState, M> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       BhHorseRenderState state, float yRot, float xRot) {
        ClientLevel level = Minecraft.getInstance().level;
        if (!state.kicksDust || state.hoofLifted == null || level == null) return;
        if (!(level.getEntity(state.entityId) instanceof AbstractHorse horse)) return;
        PoseStack local = new PoseStack();
        local.last().pose().set(state.dustOrigin).mul(poseStack.last().pose());
        getParentModel().bhHoofBottoms(local, hooves);
        boolean[] lifted = state.hoofLifted;
        RandomSource random = level.getRandom();
        for (int i = 0; i < hooves.length; i++) {
            Vector3f hoof = hooves[i];
            if (hoof.y > HOOF_LIFT) {
                lifted[i] = true;
                continue;
            }
            if (!lifted[i] || hoof.y > HOOF_CONTACT) continue;
            lifted[i] = false;
            double hx = state.x + hoof.x;
            double hz = state.z + hoof.z;
            BlockState ground = level.getBlockState(BlockPos.containing(hx, state.y - 0.2, hz));
            if (ground.getRenderShape() == RenderShape.INVISIBLE) continue;
            HoofStepEvent step = new HoofStepEvent(horse, i, new Vec3(hx, state.y, hz), ground, ParticleTypes.POOF);
            HoofStepEvent.EVENT.invoker().onHoofStep(step);
            if (step.canceled() || !BhFeature.HOOF_DUST.on()) continue;
            for (int n = 0; n < HOOF_DUST; n++) {
                level.addParticle(step.particle(), hx, state.y + 0.05, hz,
                        random.nextGaussian() * HOOF_DUST_SPREAD, HOOF_DUST_SPREAD,
                        random.nextGaussian() * HOOF_DUST_SPREAD);
            }
        }
    }
}
