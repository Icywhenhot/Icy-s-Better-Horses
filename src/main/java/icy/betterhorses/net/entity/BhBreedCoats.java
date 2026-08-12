package icy.betterhorses.net.entity;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The coat list for one breed.
 *
 * <p>These entities are not vanilla horses, so vanilla's {@code Variant} and {@code Markings}
 * no longer decide anything about how they look. A breed has exactly as many coats as it has
 * textures, and each coat's id <em>is</em> its texture file name:
 *
 * <pre>assets/icys-better-horses/textures/entity/horse/&lt;folder&gt;/&lt;coatId&gt;.png</pre>
 *
 * <p>Display names are derived from the same id ({@code brown_and_white} reads as
 * "Brown and White"), so adding a coat means dropping in a png and adding its name to the
 * list here - no lang entry required. A lang key is still honoured if one exists, so coats
 * can be translated later without changing this code.
 */
public final class BhBreedCoats {

    /** Words that stay lowercase when a coat id is turned into a display name. */
    private static final Set<String> MINOR_WORDS = Set.of("and", "of", "the", "with");

    public static final BhBreedCoats ICELANDIC = new BhBreedCoats(
            "icelandic",
            List.of("black", "brown", "brown_and_white", "white"));

    // Friesians are a solid-black breed, so the two coats differ only by the white star
    // on the face. "Star" is the horseman's term for that marking; a lang key
    // coat.icys-better-horses.friesian.star would override it without touching this list.
    public static final BhBreedCoats FRIESIAN = new BhBreedCoats(
            "friesian",
            List.of("black", "star"));

    // The medium size class. These three share a mesh and a tack set but not a coat list -
    // the coats are the only thing that tells them apart in the world, so each keeps its own
    // folder under textures/entity/horse/.
    public static final BhBreedCoats APPALOOSA = new BhBreedCoats(
            "appaloosa",
            List.of("black", "brown", "gray", "white"));

    public static final BhBreedCoats THOROUGHBRED = new BhBreedCoats(
            "thoroughbred",
            List.of("brown", "dark_brown", "red"));

    public static final BhBreedCoats AMERICAN_PAINT = new BhBreedCoats(
            "american_paint",
            List.of("black", "brown", "chestnut"));

    public static final BhBreedCoats ANDALUSIAN = new BhBreedCoats(
            "andalusian",
            List.of("bay", "gray", "white"));

    public static final BhBreedCoats MUSTANG = new BhBreedCoats(
            "mustang",
            List.of("brown", "chestnut", "white"));

    // "brown_with_socks" reads as "Brown with Socks" - `with` is a minor word and stays
    // lowercase. Icy's file was named `lightbrown`; normalised to `light_brown` on copy so
    // the derived display name comes out "Light Brown" rather than "Lightbrown".
    public static final BhBreedCoats QUARTER = new BhBreedCoats(
            "quarter",
            List.of("brown", "brown_with_socks", "light_brown"));

    private final String folder;
    private final List<String> coatIds;
    private final List<Identifier> textures;

    private BhBreedCoats(String folder, List<String> coatIds) {
        if (coatIds.isEmpty()) {
            throw new IllegalArgumentException("breed " + folder + " needs at least one coat");
        }
        this.folder = folder;
        this.coatIds = List.copyOf(coatIds);
        this.textures = this.coatIds.stream()
                .map(id -> Identifier.fromNamespaceAndPath(
                        IcysBetterHorses.MOD_ID,
                        "textures/entity/horse/" + folder + "/" + id + ".png"))
                .toList();
    }

    public int count() {
        return coatIds.size();
    }

    /** Clamps rather than throwing, so a saved coat index from a build with more coats is safe. */
    public int clamp(int index) {
        return index < 0 || index >= coatIds.size() ? 0 : index;
    }

    public int roll(RandomSource random) {
        return random.nextInt(coatIds.size());
    }

    public String coatId(int index) {
        return coatIds.get(clamp(index));
    }

    public Identifier texture(int index) {
        return textures.get(clamp(index));
    }

    public Component displayName(int index) {
        String id = coatId(index);
        return Component.translatableWithFallback(
                "coat.icys-better-horses." + folder + "." + id, prettify(id));
    }

    /** {@code brown_and_white} -> {@code Brown and White}. */
    private static String prettify(String id) {
        String[] words = id.split("_");
        StringBuilder out = new StringBuilder(id.length());
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }
            if (i > 0) {
                out.append(' ');
            }
            if (i > 0 && MINOR_WORDS.contains(word)) {
                out.append(word);
            } else {
                out.append(Character.toUpperCase(word.charAt(0)))
                   .append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return out.toString();
    }
}
