package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BhHorsePoseTest {
    @Test
    void cachedTackMatchesIndependentAnimation() throws Exception {
        List<Supplier<BhHorseModel<?>>> models = List.of(
                () -> new ShireHorseModel(ShireHorseGeometry.createBodyLayer().bakeRoot()),
                () -> new ShireHorseModel(ShireSaddleGeometry.createBodyLayer().bakeRoot()),
                () -> new ShireHorseModel(ShireArmorGeometry.createBodyLayer().bakeRoot()),
                () -> new ShireFoalModel(PercheronFoalGeometry.createBodyLayer().bakeRoot()),
                () -> new IcelandicHorseModel(IcelandicHorseGeometry.createBodyLayer().bakeRoot()),
                () -> new IcelandicHorseModel(IcelandicArmorGeometry.createBodyLayer().bakeRoot()));
        Random random = new Random(42);
        BhHorseRenderState state = new BhHorseRenderState();
        for (int frame = 0; frame < 40; frame++) {
            for (var field : BhHorseRenderState.class.getFields()) {
                if (field.getType() == float.class) field.setFloat(state, random.nextFloat());
            }
            state.ageInTicks = frame;
            state.poseRevision++;
            for (Supplier<BhHorseModel<?>> factory : models) {
                BhHorseModel<?> first = factory.get();
                BhHorseModel<?> cached = factory.get();
                first.setupState(state);
                cached.setupState(state);
                ModelPart[] actual = cached.posedParts;
                BhRiderMotion rider = BhRiderMotion.get(state.entityId);
                state.poseRevision++;
                BhHorseModel<?> independent = factory.get();
                independent.setupState(state);
                ModelPart[] expected = independent.posedParts;
                assertEquals(expected.length, actual.length);
                for (int part = 0; part < expected.length; part++) {
                    assertEquals(describe(expected[part]), describe(actual[part]));
                    assertEquals(expected[part].visible, actual[part].visible);
                }
                assertEquals(BhRiderMotion.get(state.entityId), rider);
            }
        }
    }

    private static String describe(ModelPart part) {
        return part.x + "," + part.y + "," + part.z + "," + part.xRot + "," + part.yRot + "," + part.zRot
                + "," + part.xScale + "," + part.yScale + "," + part.zScale;
    }
}
