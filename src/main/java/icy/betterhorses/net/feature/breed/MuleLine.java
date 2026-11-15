package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedMule;
import icy.betterhorses.net.registry.AbilityType;
import icy.betterhorses.net.registry.BhBreeds;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record MuleLine(HorseBreed horse, HorseBreed donkey, int flips) {

    public static final int TIERS = 3;

    private static final Map<HorseBreed, List<List<BhAbility>>> TRAITS = new EnumMap<>(HorseBreed.class);

    static {
        put(HorseBreed.THOROUGHBRED, List.of(), List.of(BhAbility.THOROUGHBRED_TOP_END), List.of());
        put(HorseBreed.ARABIAN, List.of(), List.of(BhAbility.ARABIAN_ENDURANCE), List.of());
        put(HorseBreed.QUARTER, List.of(), List.of(BhAbility.QUARTER_BURST), List.of());
        put(HorseBreed.FRIESIAN, List.of(BhAbility.FRIESIAN_CALM), List.of(), List.of(BhAbility.FRIESIAN_DRESSAGE));
        put(HorseBreed.ANDALUSIAN, List.of(), List.of(BhAbility.ANDALUSIAN_GUARD), List.of(BhAbility.ANDALUSIAN_SAVE));
        put(HorseBreed.PERCHERON, List.of(), List.of(BhAbility.PERCHERON_MOMENTUM), List.of(BhAbility.PERCHERON_CHAIN));
        put(HorseBreed.CLYDESDALE, List.of(BhAbility.CLYDESDALE_ARMOR), List.of(BhAbility.CLYDESDALE_RESIST),
                List.of(BhAbility.CLYDESDALE_DEFLECT));
        put(HorseBreed.SHIRE, List.of(), List.of(BhAbility.SHIRE_INTIMIDATE), List.of(BhAbility.SHIRE_BRICK));
        put(HorseBreed.BELGIAN, List.of(), List.of(), List.of(BhAbility.BELGIAN_BRICK));
        put(HorseBreed.ICELANDIC, List.of(BhAbility.ICELANDIC_MOMENTUM), List.of(BhAbility.ICELANDIC_FREEZE),
                List.of(BhAbility.ICELANDIC_CLEAR));
        put(HorseBreed.MUSTANG, List.of(BhAbility.MUSTANG_ALERT), List.of(BhAbility.MUSTANG_SELF_HEAL),
                List.of(BhAbility.MUSTANG_RIDER_HEAL));
        put(HorseBreed.HAFLINGER, List.of(), List.of(BhAbility.HAFLINGER_HAUL), List.of(BhAbility.HAFLINGER_LIGHT));
        put(HorseBreed.MORGAN, List.of(BhAbility.MORGAN_BOND), List.of(), List.of(BhAbility.MORGAN_ADAPT));
        put(HorseBreed.AMERICAN_PAINT, List.of(BhAbility.PAINT_SCAN), List.of(BhAbility.PAINT_LOOTING),
                List.of(BhAbility.PAINT_STRUCTURES));
        put(HorseBreed.APPALOOSA, List.of(BhAbility.APPALOOSA_NIGHT), List.of(BhAbility.APPALOOSA_HERD),
                List.of(BhAbility.APPALOOSA_FEED));

        put(HorseBreed.ANDALUSIAN_DONKEY, List.of(BhAbility.ANDALUSIAN_FARM), List.of(BhAbility.ANDALUSIAN_FARM),
                List.of(BhAbility.ANDALUSIAN_FARM));
        put(HorseBreed.ASINARA_DONKEY, List.of(BhAbility.ASINARA_WATER), List.of(BhAbility.ASINARA_FISH),
                List.of(BhAbility.ASINARA_TREASURE, BhAbility.ASINARA_BITE));
        put(HorseBreed.MAMMOTH_DONKEY, List.of(BhAbility.MAMMOTH_RUBBLE), List.of(BhAbility.MAMMOTH_WORK),
                List.of(BhAbility.MAMMOTH_BRACE));
        put(HorseBreed.MINIATURE_DONKEY, List.of(BhAbility.MINIATURE_PICKUP, BhAbility.MINIATURE_FARM),
                List.of(BhAbility.MINIATURE_BASKET, BhAbility.MINIATURE_FARM), List.of(BhAbility.MINIATURE_FARM));
        put(HorseBreed.PEGA_DONKEY, List.of(BhAbility.PEGA_SEAT, BhAbility.PEGA_MEDKIT),
                List.of(BhAbility.PEGA_SHARE), List.of(BhAbility.PEGA_RALLY));
        put(HorseBreed.POITOU_DONKEY, List.of(BhAbility.POITOU_HIDE), List.of(BhAbility.POITOU_HIDE),
                List.of(BhAbility.POITOU_CLEAR));
        put(HorseBreed.ZAMORANO_DONKEY, List.of(BhAbility.ZAMORANO_ICE), List.of(BhAbility.ZAMORANO_SNOW),
                List.of(BhAbility.ZAMORANO_SNOW));
    }

    private static void put(HorseBreed breed, List<BhAbility> low, List<BhAbility> mid, List<BhAbility> high) {
        TRAITS.put(breed, List.of(low, mid, high));
    }

    public static @Nullable MuleLine of(IHorseData data) {
        return data instanceof BhBreedMule mule ? mule.bhLine() : null;
    }

    public static MuleLine born(HorseBreed a, HorseBreed b, RandomSource random) {
        HorseBreed horse = a.isDonkey() ? b : a;
        HorseBreed donkey = a.isDonkey() ? a : b;
        return new MuleLine(TRAITS.containsKey(horse) ? horse : pick(random, false),
                TRAITS.containsKey(donkey) ? donkey : pick(random, true), random.nextInt(1 << TIERS));
    }

    public static MuleLine roll(RandomSource random) {
        return new MuleLine(pick(random, false), pick(random, true), random.nextInt(1 << TIERS));
    }

    private static HorseBreed pick(RandomSource random, boolean donkey) {
        List<HorseBreed> pool = new ArrayList<>();
        for (HorseBreed breed : TRAITS.keySet()) {
            if (breed.isDonkey() == donkey) pool.add(breed);
        }
        return pool.get(random.nextInt(pool.size()));
    }

    public static @Nullable MuleLine parse(String raw) {
        String[] parts = raw.split(",");
        if (parts.length != 3) return null;
        HorseBreed horse = HorseBreed.byId(parts[0]);
        HorseBreed donkey = HorseBreed.byId(parts[1]);
        if (!TRAITS.containsKey(horse) || !TRAITS.containsKey(donkey)) return null;
        try {
            return new MuleLine(horse, donkey, Integer.parseInt(parts[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String pack() {
        return horse.id() + "," + donkey.id() + "," + flips;
    }

    public boolean horseSide(int tier) {
        return (flips & (1 << tier)) != 0;
    }

    public HorseBreed from(int tier) {
        HorseBreed first = horseSide(tier) ? horse : donkey;
        HorseBreed other = horseSide(tier) ? donkey : horse;
        return live(first, tier).isEmpty() && !live(other, tier).isEmpty() ? other : first;
    }

    public List<BhAbility> traits(int tier) {
        return live(from(tier), tier);
    }

    private static List<BhAbility> live(HorseBreed breed, int tier) {
        List<BhAbility> out = new ArrayList<>();
        for (BhAbility ability : TRAITS.get(breed).get(tier)) {
            if (ability.on()) out.add(ability);
        }
        return out;
    }

    public int tier(BhAbility ability, int bond) {
        HorseBreed owner = ability.breed();
        if (owner == null || !TRAITS.containsKey(owner)) return -1;
        int spots = 0;
        for (List<BhAbility> row : TRAITS.get(owner)) {
            if (row.contains(ability)) spots++;
        }
        int best = -1;
        for (int k = 0; k <= Math.min(bond, TIERS - 1); k++) {
            if (traits(k).contains(ability)) best = spots > 1 ? k : bond;
        }
        return best;
    }

    public List<ResourceKey<AbilityType>> abilities() {
        List<ResourceKey<AbilityType>> out = new ArrayList<>();
        for (HorseBreed parent : new HorseBreed[]{horse, donkey}) {
            ResourceKey<BreedType> key = BhBreeds.keyOf(parent);
            BreedType type = key == null ? null : BhRegistries.breedTypeRegistry().getValue(key.identifier());
            if (type == null) continue;
            for (ResourceKey<AbilityType> id : type.abilities()) {
                if (!id.equals(BhContent.SKITTISH.key()) && !out.contains(id)) out.add(id);
            }
        }
        return out;
    }

    public static List<ResourceKey<AbilityType>> abilitiesOf(IHorseData data) {
        MuleLine line = of(data);
        if (line != null) return line.abilities();
        ResourceKey<BreedType> key = data.bh_getBreedKey();
        BreedType type = key == null ? null : BhRegistries.breedTypeRegistry().getValue(key.identifier());
        return type == null ? List.of() : type.abilities();
    }
}
