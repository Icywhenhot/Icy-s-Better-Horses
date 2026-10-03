package icy.betterhorses.net.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BreedCoatSet {

    private static final Set<String> MINOR_WORDS = Set.of("and", "of", "the", "with");

    private final String resourceNamespace;
    private final String folder;
    private final List<String> coatIds;
    private final boolean hasFoalVariant;
    private final List<ResourceLocation> textures;
    private final List<ResourceLocation> foalTextures;

    public BreedCoatSet(String resourceNamespace, String folder, List<String> coatIds, boolean hasFoalVariant) {
        if (coatIds.isEmpty()) {
            throw new IllegalArgumentException("breed " + folder + " needs at least one coat");
        }
        this.resourceNamespace = resourceNamespace;
        this.folder = folder;
        this.coatIds = List.copyOf(coatIds);
        this.hasFoalVariant = hasFoalVariant;
        this.textures = texturesIn(folder, this.coatIds);
        this.foalTextures = hasFoalVariant ? texturesIn(folder + "/baby", this.coatIds) : null;
    }

    private List<ResourceLocation> texturesIn(String path, List<String> ids) {
        return ids.stream()
                .map(id -> ResourceLocation.fromNamespaceAndPath(resourceNamespace, "textures/entity/horse/" + path + "/" + id + ".png"))
                .toList();
    }

    public String resourceNamespace() {
        return resourceNamespace;
    }

    public String folder() {
        return folder;
    }

    public List<String> coatIds() {
        return coatIds;
    }

    public boolean hasFoalVariant() {
        return hasFoalVariant;
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

    public ResourceLocation texture(int index, boolean baby) {
        return baby && foalTextures != null
                ? foalTextures.get(clamp(index))
                : textures.get(clamp(index));
    }

    public Component displayName(int index) {
        String id = coatId(index);
        return Component.translatableWithFallback(
                "coat." + resourceNamespace + "." + folder + "." + id, prettify(id));
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
