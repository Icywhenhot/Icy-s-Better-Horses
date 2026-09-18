package icy.betterhorses.net.network;

import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.HorseBreed;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record BreedDataPayload(List<Entry> entries) {


    private static final int MAX_ENTRIES = 128;
    private static final int MAX_NAME = 64;

    public record Entry(String breed, String archetype, int chestRows, int bondedChestRows, int spawnWeight) {}

    public static BreedDataPayload current() {
        List<Entry> out = new ArrayList<>();
        for (Map.Entry<HorseBreed, BhBreedData> entry : BhBreedData.all().entrySet()) {
            BhBreedData data = entry.getValue();
            out.add(new Entry(entry.getKey().id(),
                    data.archetype().name().toLowerCase(Locale.ROOT),
                    data.chestRows(), data.bondedChestRows(), data.spawnWeight()));
        }
        return new BreedDataPayload(out);
    }

    public Map<HorseBreed, BhBreedData> toMap() {
        EnumMap<HorseBreed, BhBreedData> map = new EnumMap<>(HorseBreed.class);
        for (Entry entry : entries) {
            HorseBreed breed = HorseBreed.byId(entry.breed());
            BhBreedData fallback = BhBreedData.builtIn(breed);
            BreedArchetype arch;
            try {
                arch = BreedArchetype.valueOf(entry.archetype().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                arch = fallback.archetype();
            }
            map.put(breed, new BhBreedData(arch, entry.chestRows(), entry.bondedChestRows(), entry.spawnWeight()));
        }
        return map;
    }


    public static void encode(BreedDataPayload payload, FriendlyByteBuf buf) {
        List<Entry> entries = payload.entries();
        int count = Math.min(entries.size(), MAX_ENTRIES);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            Entry entry = entries.get(i);
            buf.writeUtf(entry.breed(), MAX_NAME);
            buf.writeUtf(entry.archetype(), MAX_NAME);
            buf.writeVarInt(entry.chestRows());
            buf.writeVarInt(entry.bondedChestRows());
            buf.writeVarInt(entry.spawnWeight());
        }
    }

    public static BreedDataPayload decode(FriendlyByteBuf buf) {
        int count = Math.min(buf.readVarInt(), MAX_ENTRIES);
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(
                    buf.readUtf(MAX_NAME),
                    buf.readUtf(MAX_NAME),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()));
        }
        return new BreedDataPayload(List.copyOf(entries));
    }
}
