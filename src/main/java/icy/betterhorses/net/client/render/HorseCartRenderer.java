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

public final class HorseCartRenderer extends GeoEntityRenderer<HorseCartEntity, EntityRenderState> {

    private static final String CHEST_BONE = "chest";
    private static final DataTicket<Boolean> HAS_CHEST =
            DataTicket.create("bh_cart_has_chest", Boolean.class);

    public HorseCartRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseCartGeoModel());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<EntityRenderState> pass, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(pass, snapshots);

        if (!pass.renderState().getOrDefaultGeckolibData(HAS_CHEST, false)) {
            snapshots.ifPresent(CHEST_BONE, snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
        }
    }

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
