package icy.betterhorses.net.entity;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BhBreedCoats {

    private static final Set<String> MINOR_WORDS = Set.of("and", "of", "the", "with");

    public static final BhBreedCoats ICELANDIC = new BhBreedCoats(
            "icelandic",
            List.of("black", "brown", "brown_and_white", "white"));

    public static final BhBreedCoats FRIESIAN = new BhBreedCoats(
            "friesian",
            List.of("black", "star"));

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
