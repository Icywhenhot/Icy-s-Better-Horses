package icy.betterhorses.net.registry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BreedCoatSetTest {
    @Test
    void storesNamespaceFolderCoatIdsAndFoalFlag() {
        BreedCoatSet coats = new BreedCoatSet("examplemod", "highland_pony", List.of("dun", "grey", "black"), true);

        assertEquals("examplemod", coats.resourceNamespace());
        assertEquals("highland_pony", coats.folder());
        assertEquals(List.of("dun", "grey", "black"), coats.coatIds());
        assertTrue(coats.hasFoalVariant());
    }

    @Test
    void rejectsEmptyCoatList() {
        assertThrows(IllegalArgumentException.class,
                () -> new BreedCoatSet("examplemod", "highland_pony", List.of(), true));
    }

    @Test
    void resolvesTextureUnderOwnNamespaceAndFolder() {
        BreedCoatSet coats = new BreedCoatSet("examplemod", "highland_pony", List.of("dun", "grey"), false);

        assertEquals("examplemod", coats.texture(0, false).getNamespace());
        assertEquals("textures/entity/horse/highland_pony/dun.png", coats.texture(0, false).getPath());
        assertEquals("textures/entity/horse/highland_pony/grey.png", coats.texture(1, false).getPath());
    }

    @Test
    void fallsBackToAdultTextureWhenNoFoalVariant() {
        BreedCoatSet coats = new BreedCoatSet("examplemod", "highland_pony", List.of("dun"), false);

        assertEquals(coats.texture(0, false), coats.texture(0, true));
    }

    @Test
    void usesFoalFolderWhenFoalVariantPresent() {
        BreedCoatSet coats = new BreedCoatSet("examplemod", "highland_pony", List.of("dun"), true);

        assertEquals("textures/entity/horse/highland_pony/baby/dun.png", coats.texture(0, true).getPath());
    }

    @Test
    void clampHandlesOutOfRangeIndices() {
        BreedCoatSet coats = new BreedCoatSet("examplemod", "highland_pony", List.of("dun", "grey"), false);

        assertEquals(0, coats.clamp(-1));
        assertEquals(0, coats.clamp(5));
        assertEquals(1, coats.clamp(1));
    }
}
