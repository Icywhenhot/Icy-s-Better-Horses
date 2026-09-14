package icy.betterhorses.net.network;

import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.HorseBreed;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record BreedDataPayload(List<Entry> entries) implements CustomPacketPayload {

    public static final Type<BreedDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("icys-better-horses", "breed_data"));

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

    @Override
    public Type<BreedDataPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, BreedDataPayload> {
        @Override
        public BreedDataPayload decode(FriendlyByteBuf buf) {
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

        @Override
        public void encode(FriendlyByteBuf buf, BreedDataPayload value) {
            List<Entry> entries = value.entries();
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
    }
}
