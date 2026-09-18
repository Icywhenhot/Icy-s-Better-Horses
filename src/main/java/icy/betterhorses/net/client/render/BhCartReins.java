package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BhCartReins {

    private static final double SIDE = 0.15D;
    private static final double HEAD_HEIGHT = 1.45D;
    private static final double HEAD_FORWARD = 0.75D;
    private static final double HAND_HEIGHT = 0.4D;

    private static final int SEGMENTS = 24;
    private static final float THICKNESS = 0.025F;

    private BhCartReins() {}

    public static void render(AbstractHorse horse, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        float bodyYaw = Mth.rotLerp(partialTick, horse.yBodyRotO, horse.yBodyRot);
        float radians = -bodyYaw * ((float) Math.PI / 180.0F);
        Vec3 horsePos = horse.getPosition(partialTick);
        Vec3 driverPos = horsePos.add(HorseCartEntity.benchSeatOffset(horse, 0, bodyYaw));
        Level level = horse.level();

        VertexConsumer consumer = buffer.getBuffer(RenderType.leash());

        for (int i = 0; i < 2; i++) {
            double side = i == 0 ? -SIDE : SIDE;
            Vec3 head = new Vec3(side, HEAD_HEIGHT, HEAD_FORWARD).yRot(radians);
            Vec3 hand = new Vec3(side, HAND_HEIGHT, 0.0D).yRot(radians);
            Vec3 start = horsePos.add(head);
            Vec3 end = driverPos.add(hand);

            BlockPos startBlock = BlockPos.containing(start);
            BlockPos endBlock = BlockPos.containing(end);

            poseStack.pushPose();
            poseStack.translate(head.x, head.y, head.z);
            strand(consumer, poseStack.last().pose(),
                    (float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z),
                    level.getBrightness(LightLayer.BLOCK, startBlock),
                    level.getBrightness(LightLayer.BLOCK, endBlock),
                    level.getBrightness(LightLayer.SKY, startBlock),
                    level.getBrightness(LightLayer.SKY, endBlock));
            poseStack.popPose();
        }
    }

    private static void strand(VertexConsumer consumer, Matrix4f pose, float dx, float dy, float dz,
                               int startBlockLight, int endBlockLight, int startSkyLight, int endSkyLight) {
        float spread = Mth.invSqrt(dx * dx + dz * dz) * THICKNESS / 2.0F;
        float offX = dz * spread;
        float offZ = dx * spread;

        for (int i = 0; i <= SEGMENTS; i++) {
            pair(consumer, pose, dx, dy, dz, startBlockLight, endBlockLight, startSkyLight, endSkyLight,
                    THICKNESS, offX, offZ, i, false);
        }
        for (int i = SEGMENTS; i >= 0; i--) {
            pair(consumer, pose, dx, dy, dz, startBlockLight, endBlockLight, startSkyLight, endSkyLight,
                    0.0F, offX, offZ, i, true);
        }
    }

    private static void pair(VertexConsumer consumer, Matrix4f pose, float dx, float dy, float dz,
                             int startBlockLight, int endBlockLight, int startSkyLight, int endSkyLight,
                             float rise, float offX, float offZ, int index, boolean reverse) {
        float t = (float) index / SEGMENTS;
        int light = LightTexture.pack(
                (int) Mth.lerp(t, startBlockLight, endBlockLight),
                (int) Mth.lerp(t, startSkyLight, endSkyLight));
        float shade = index % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;
        float r = 0.5F * shade;
        float g = 0.4F * shade;
        float b = 0.3F * shade;
        float x = dx * t;
        float y = dy > 0.0F ? dy * t * t : dy - dy * (1.0F - t) * (1.0F - t);
        float z = dz * t;
        consumer.vertex(pose, x - offX, y + rise, z + offZ).color(r, g, b, 1.0F).uv2(light).endVertex();
        consumer.vertex(pose, x + offX, y + THICKNESS - rise, z - offZ).color(r, g, b, 1.0F).uv2(light).endVertex();
    }
}
