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
        List<Supplier<BhHorseModel>> models = List.of(
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
            for (Supplier<BhHorseModel> factory : models) {
                BhHorseModel first = factory.get();
                BhHorseModel cached = factory.get();
                first.setupAnim(state);
                cached.setupAnim(state);
                List<ModelPart> actual = cached.allParts();
                BhRiderMotion rider = BhRiderMotion.get(state.entityId);
                state.poseRevision++;
                BhHorseModel independent = factory.get();
                independent.setupAnim(state);
                List<ModelPart> expected = independent.allParts();
                assertEquals(expected.size(), actual.size());
                for (int part = 0; part < expected.size(); part++) {
                    assertEquals(expected.get(part).storePose(), actual.get(part).storePose());
                    assertEquals(expected.get(part).visible, actual.get(part).visible);
                }
                assertEquals(BhRiderMotion.get(state.entityId), rider);
            }
        }
    }
}
