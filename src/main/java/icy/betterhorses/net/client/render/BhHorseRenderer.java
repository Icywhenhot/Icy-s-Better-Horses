package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.BhFeature;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.api.HoofStepEvent;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.DyeableLeatherItem;

import java.util.function.Function;

public class BhHorseRenderer<T extends BhBreedHorse> extends MobRenderer<T, BhHorseModel<T>> {

    private final BhHorseModel<T> adultModel;
    private final BhHorseModel<T> babyModel;
    private final Matrix4f originInverse = new Matrix4f();
    private final Vector3f[] hooves = {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};

    public BhHorseRenderer(EntityRendererProvider.Context context,
                           BhHorseModel<T> adultModel,
                           BhHorseModel<T> babyModel) {
        super(context, adultModel, 0.75F);
        this.adultModel = adultModel;
        this.babyModel = babyModel;
        addLayer(new RenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                               float limbSwing, float limbSwingAmount, float partialTick,
                               float ageInTicks, float netHeadYaw, float headPitch) {
                kickHoofDust(entity, poseStack, partialTick);
            }
        });
    }

    protected BhHorseRenderer(EntityRendererProvider.Context context,
                              BhHorseModel<T> adultModel,
                              BhHorseModel<T> babyModel,
                              Function<ModelLayerLocation, BhHorseModel<T>> models,
                              ModelLayerLocation saddle,
                              ModelLayerLocation saddleBaby,
                              ModelLayerLocation armor,
                              ModelLayerLocation armorBaby,
                              ModelLayerLocation chest,
                              ModelLayerLocation chestBaby,
                              BhTackTextures textures) {
        this(context, adultModel, babyModel);
        addLayer(new BhTackLayer<>(this, models.apply(saddle), models.apply(saddleBaby), entity ->
                entity.isSaddled()
                        ? textures.saddle(IHorseData.of(entity).bh_hasUpgradedSaddle())
                        : null));
        addLayer(new BhTackLayer<>(this, models.apply(armor), models.apply(armorBaby), entity -> {
            ItemStack stack = barding(entity);
            return stack.isEmpty() ? null : textures.armor(stack);
        }, entity -> {
            ItemStack stack = barding(entity);
            if (!(stack.getItem() instanceof DyeableLeatherItem dyed)) {
                return -1;
            }
            if (dyed.hasCustomColor(stack)) {
                return 0xFF000000 | dyed.getColor(stack);
            }
            return stack.is(Items.LEATHER_HORSE_ARMOR) ? 0xFF000000 | UNDYED_BARDING : -1;
        }));
        addLayer(new BhTackLayer<>(this, models.apply(chest), models.apply(chestBaby), entity -> {
            IHorseData data = IHorseData.of(entity);
            return data.bh_hasGear(GearSlot.CHEST) ? textures.chest(data.bh_hasEnderChestGear()) : null;
        }));
        addLayer(new HorseStabilizerLayer<>(this));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        model = entity.isBaby() ? babyModel : adultModel;
        originInverse.set(poseStack.last().pose()).invert();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        if (IHorseData.of(entity).bh_hasCartGear() && entity.getControllingPassenger() != null) {
            BhCartReins.render(entity, partialTick, poseStack, buffer);
        }
    }

    private void kickHoofDust(T entity, PoseStack poseStack, float partialTick) {
        Level level = entity.level();
        int id = BhHorseRenderState.renderId(entity.getId());
        if (id != entity.getId() || level.getEntity(id) != entity || entity.isInWater()) return;
        PoseStack local = new PoseStack();
        local.last().pose().set(originInverse).mul(poseStack.last().pose());
        getModel().bhHoofBottoms(local, hooves);
        boolean[] lifted = BhHorseRenderState.forEntity(id).hoofLifted;
        double x = Mth.lerp(partialTick, entity.xo, entity.getX());
        double y = Mth.lerp(partialTick, entity.yo, entity.getY());
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
        RandomSource random = level.getRandom();
        for (int i = 0; i < hooves.length; i++) {
            Vector3f hoof = hooves[i];
            if (hoof.y > HOOF_LIFT) {
                lifted[i] = true;
                continue;
            }
            if (!lifted[i] || hoof.y > HOOF_CONTACT) continue;
            lifted[i] = false;
            double hx = x + hoof.x;
            double hz = z + hoof.z;
            BlockPos pos = BlockPos.containing(hx, y - 0.2, hz);
            BlockState ground = level.getBlockState(pos);
            if (ground.getRenderShape() == RenderShape.INVISIBLE) continue;
            HoofStepEvent step = new HoofStepEvent(entity, i, new Vec3(hx, y, hz), ground, ParticleTypes.POOF);
            if (MinecraftForge.EVENT_BUS.post(step) || !BhFeature.HOOF_DUST.on()) continue;
            for (int n = 0; n < HOOF_DUST; n++) {
                level.addParticle(step.particle(), hx, y + 0.05, hz,
                        random.nextGaussian() * HOOF_DUST_SPREAD, HOOF_DUST_SPREAD,
                        random.nextGaussian() * HOOF_DUST_SPREAD);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return BhNamedCoats.coat(entity);
    }

    private static final float HOOF_LIFT = 0.08F;
    private static final float HOOF_CONTACT = 0.03F;
    private static final int HOOF_DUST = 3;
    private static final double HOOF_DUST_SPREAD = 0.02;

    private static final int UNDYED_BARDING = 0xBB744F;

    private static ItemStack barding(AbstractHorse horse) {
        return IHorseData.of(horse).bh_getBarding();
    }
}
