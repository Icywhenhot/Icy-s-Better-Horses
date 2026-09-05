package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.entity.HorseCartEntity;
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
    private static final DataTicket<Boolean> IS_LARGE =
            DataTicket.create("bh_cart_is_large", Boolean.class);

    private static final String PLOW_BONE = "plow";
    private static final String PROP_BONE = "bone3";

    public static CartSize sizeOf(GeoRenderState renderState) {
        return CartSize.byLarge(renderState.getOrDefaultGeckolibData(IS_LARGE, false));
    }

    public HorseCartRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseCartGeoModel());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<EntityRenderState> pass, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(pass, snapshots);

        if (!pass.renderState().getOrDefaultGeckolibData(HAS_CHEST, false)) {
            snapshots.ifPresent(sizeOf(pass.renderState()).chestBone(),
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
    }

    @Override
    public void extractRenderState(HorseCartEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.addGeckolibData(HAS_CHEST, entity.hasChest());
        state.addGeckolibData(HAS_PLOW, entity.hasPlough());
        state.addGeckolibData(IS_PLACED, entity.isPlaced());
        state.addGeckolibData(IS_LARGE, entity.size().isLarge());

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
