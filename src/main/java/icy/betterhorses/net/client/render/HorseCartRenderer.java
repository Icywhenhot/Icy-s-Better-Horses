package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.entity.CartType;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

public final class HorseCartRenderer extends GeoEntityRenderer<HorseCartEntity, EntityRenderState> {

    private static final DataTicket<Boolean> HAS_CHEST =
            DataTicket.create("bh_cart_has_chest", Boolean.class);
    private static final DataTicket<Boolean> HAS_PLOW =
            DataTicket.create("bh_cart_has_plow", Boolean.class);
    private static final DataTicket<Boolean> IS_PLACED =
            DataTicket.create("bh_cart_is_placed", Boolean.class);
    private static final DataTicket<Integer> TYPE =
            DataTicket.create("bh_cart_type", Integer.class);
    private static final DataTicket<Integer> HIDDEN =
            DataTicket.create("bh_cart_hidden", Integer.class);
    private static final DataTicket<Integer> CART_ID =
            DataTicket.create("bh_cart_id", Integer.class);
    private static final DataTicket<Float> TILT =
            DataTicket.create("bh_cart_tilt", Float.class);

    private static final String PLOW_BONE = "plow";
    private static final String PROP_BONE = "bone3";

    public static CartType typeOf(GeoRenderState renderState) {
        return CartType.byOrdinal(renderState.getOrDefaultGeckolibData(TYPE, 0));
    }

    public HorseCartRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseCartGeoModel());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<EntityRenderState> pass, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(pass, snapshots);

        if (!pass.renderState().getOrDefaultGeckolibData(HAS_CHEST, false)) {
            snapshots.ifPresent(typeOf(pass.renderState()).chestBone(),
                    snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
        }
        if (!pass.renderState().getOrDefaultGeckolibData(IS_PLACED, false)) {
            snapshots.ifPresent(PROP_BONE,
                    snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
        }
        if (!pass.renderState().getOrDefaultGeckolibData(HAS_PLOW, false)) {
            snapshots.ifPresent(PLOW_BONE,
                    snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
        }
        CartType type = typeOf(pass.renderState());
        int hidden = pass.renderState().getOrDefaultGeckolibData(HIDDEN, 0);
        for (int i = 0; i < type.parts().size(); i++) {
            if ((hidden & (1 << i)) != 0) {
                snapshots.ifPresent(type.parts().get(i).bone(),
                        snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
            }
        }
        recordBed(pass.renderState().getOrDefaultGeckolibData(CART_ID, -1), type, snapshots);
    }

    private static void recordBed(int id, CartType type, BoneSnapshots snapshots) {
        if (id < 0 || Minecraft.getInstance().level == null
                || !(Minecraft.getInstance().level.getEntity(id) instanceof HorseCartEntity cart)) {
            return;
        }
        float bounce = snapshots.get("cart").map(bone -> bone.getTranslateY()).orElse(0.0F);
        float rock = 0.0F;
        var bed = snapshots.get(type.bedBone());
        if (bed.isPresent()) {
            bounce += bed.get().getTranslateY();
            rock = bed.get().getRotX();
        }
        cart.recordBed(bounce / 16.0F, (float) Math.toDegrees(rock));
    }

    @Override
    protected void applyRotations(RenderPassInfo<EntityRenderState> pass, PoseStack poseStack, float nativeScale) {
        super.applyRotations(pass, poseStack, nativeScale);
        poseStack.mulPose(Axis.XP.rotationDegrees(pass.getOrDefaultGeckolibData(TILT, 0.0F)));
    }

    @Override
    public void extractRenderState(HorseCartEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.addGeckolibData(HAS_CHEST, entity.hasChest());
        state.addGeckolibData(HAS_PLOW, entity.hasPlough());
        state.addGeckolibData(IS_PLACED, entity.isPlaced());
        state.addGeckolibData(TYPE, entity.type().ordinal());
        int hidden = 0;
        for (int i = 0; i < entity.type().parts().size(); i++) {
            if (entity.partHidden(i)) {
                hidden |= 1 << i;
            }
        }
        state.addGeckolibData(HIDDEN, hidden);
        state.addGeckolibData(CART_ID, entity.getId());
        state.addGeckolibData(TILT, entity.renderTilt(partialTick));

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
