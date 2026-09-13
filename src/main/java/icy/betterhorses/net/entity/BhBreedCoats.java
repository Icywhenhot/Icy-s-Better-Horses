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
            List.of("black", "brown", "brown_and_white", "white",
                    "palomino", "silver_dapple", "blue_dun", "black_pinto"),
            true);

    public static final BhBreedCoats FRIESIAN = new BhBreedCoats(
            "friesian",
            List.of("black", "star"),
            true);

    public static final BhBreedCoats APPALOOSA = new BhBreedCoats(
            "appaloosa",
            List.of("black", "brown", "gray", "white"),
            true);

    public static final BhBreedCoats THOROUGHBRED = new BhBreedCoats(
            "thoroughbred",
            List.of("brown", "dark_brown", "red",
                    "blood_bay", "jet_black", "chestnut_chrome",
                    "steel_grey"),
            true);

    public static final BhBreedCoats AMERICAN_PAINT = new BhBreedCoats(
            "american_paint",
            List.of("black", "brown", "chestnut",
                    "liver_chestnut", "buckskin", "blue_roan", "red_roan",
                    "blood_bay"),
            true);

    public static final BhBreedCoats ANDALUSIAN = new BhBreedCoats(
            "andalusian",
            List.of("bay", "gray", "white",
                    "azabache", "dark_bay", "flaxen_chestnut", "rose_grey",
                    "pearl"),
            true);

    public static final BhBreedCoats MUSTANG = new BhBreedCoats(
            "mustang",
            List.of("brown", "chestnut", "white",
                    "black", "mealy_bay", "strawberry_roan", "kiger_dun",
                    "iron_grey"),
            true);

    public static final BhBreedCoats QUARTER = new BhBreedCoats(
            "quarter",
            List.of("brown", "brown_with_socks", "light_brown",
                    "sorrel", "dapple_grey", "red_dun", "liver_chestnut",
                    "grullo"),
            true);

    public static final BhBreedCoats PERCHERON = new BhBreedCoats(
            "percheron",
            List.of("gray", "black", "white",
                    "dapple_grey", "bay", "chestnut", "liver_chestnut",
                    "blue_roan"),
            true);

    public static final BhBreedCoats SHIRE = new BhBreedCoats(
            "shire",
            List.of("black", "bay", "brown", "dark_brown",
                    "grey", "chestnut", "blue_roan", "bay_blaze",

                    "black_feather", "brown_feather", "roan_feather"),
            true);

    public static final BhBreedCoats BELGIAN = new BhBreedCoats(
            "belgian",
            List.of("black", "flaxen_sorrel", "dark_sorrel", "mahogany_bay",
                    "mealy_sorrel", "bay_roan", "sabino", "dapple_sorrel",
                    "smoky_black"),
            true);

    public static final BhBreedCoats ARABIAN = new BhBreedCoats(
            "arabian",
            List.of("brown", "black", "chestnut", "silver_grey", "rabicano",
                    "sabino_chestnut", "wild_bay", "bloody_shoulder"),
            true);

    public static final BhBreedCoats MORGAN = new BhBreedCoats(
            "morgan",
            List.of("bay", "black", "red_chestnut", "chocolate_silver", "roan",
                    "dark_buckskin", "silver_bay"),
            true);

    public static final BhBreedCoats HAFLINGER = new BhBreedCoats(
            "haflinger",
            List.of("golden_chestnut", "light_chestnut", "dark_chestnut",
                    "liver_chestnut", "kohlfuchs"),
            true);

    public static final BhBreedCoats CLYDESDALE = new BhBreedCoats(
            "clydesdale",
            List.of("bay_sabino", "seal_brown", "black_sabino", "red_bay",
                    "chestnut_roan", "dapple_bay", "sooty_bay", "rose_roan"),
            true);

    private final String folder;
    private final List<String> coatIds;
    private final List<Identifier> textures;
    private final List<Identifier> foalTextures;

    private BhBreedCoats(String folder, List<String> coatIds) {
        this(folder, coatIds, false);
    }

    private BhBreedCoats(String folder, List<String> coatIds, boolean hasFoalCoats) {
        if (coatIds.isEmpty()) {
            throw new IllegalArgumentException("breed " + folder + " needs at least one coat");
        }
        this.folder = folder;
        this.coatIds = List.copyOf(coatIds);
        this.textures = texturesIn(folder, this.coatIds);
        this.foalTextures = hasFoalCoats ? texturesIn(folder + "/baby", this.coatIds) : null;
    }

    private static List<Identifier> texturesIn(String path, List<String> ids) {
        return ids.stream()
                .map(id -> Identifier.fromNamespaceAndPath(
                        IcysBetterHorses.MOD_ID,
                        "textures/entity/horse/" + path + "/" + id + ".png"))
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

    public int rollOther(RandomSource random, int a, int b) {
        a = clamp(a);
        b = clamp(b);
        int spare = coatIds.size() - (a == b ? 1 : 2);
        if (spare < 1) {
            return -1;
        }
        int pick = random.nextInt(spare);
        for (int i = 0; i < coatIds.size(); i++) {
            if (i == a || i == b) {
                continue;
            }
            if (pick-- == 0) {
                return i;
            }
        }
        return -1;
    }

    public String coatId(int index) {
        return coatIds.get(clamp(index));
    }

    public Identifier texture(int index) {
        return textures.get(clamp(index));
    }

    public Identifier texture(int index, boolean baby) {
        return baby && foalTextures != null
                ? foalTextures.get(clamp(index))
                : textures.get(clamp(index));
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
