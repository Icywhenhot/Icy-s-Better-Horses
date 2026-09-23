package icy.betterhorses.net.registry;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.BhAbilityState;
import icy.betterhorses.net.feature.breed.BreedAbility;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityTypeTest {

    private static final class StubAbility implements BreedAbility {
        @Override
        public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        }
    }

    @Test
    void createInvokesFactory() {
        StubAbility stub = new StubAbility();
        AbilityType type = new AbilityType(() -> stub, true);

        assertSame(stub, type.create());
        assertTrue(type.defaultEnabled());
    }

    @Test
    void defaultEnabledReflectsConstructorArgument() {
        AbilityType type = new AbilityType(StubAbility::new, false);

        assertFalse(type.defaultEnabled());
    }
}
