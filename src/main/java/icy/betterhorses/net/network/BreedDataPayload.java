package icy.betterhorses.net.network;

import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.tuning.Ranges;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BreedDataPayload(List<Entry> entries) {


    private static final int MAX_ENTRIES = 128;
    private static final int MAX_NAME = 64;

    public record Entry(String breed, String archetype, int chestRows, int midChestRows, int bondedChestRows, int spawnWeight,
                        Ranges ranges) {}

    public static BreedDataPayload current() {
        List<Entry> out = new ArrayList<>();
        IForgeRegistry<ArchetypeType> archetypes = BhRegistries.archetypeTypeRegistry();
        for (Map.Entry<ResourceLocation, BhBreedData> entry : BhBreedData.all().entrySet()) {
            BhBreedData data = entry.getValue();
            ResourceLocation archetypeId = archetypes.getKey(data.archetype());
            out.add(new Entry(entry.getKey().getPath(),
                    archetypeId != null ? archetypeId.toString() : "",
                    data.classRows() ? BhBreedData.CLASS_ROWS : data.chestRows(),
                    data.classMidRows() ? BhBreedData.CLASS_ROWS : data.midChestRows(),
                    data.classBondedRows() ? BhBreedData.CLASS_ROWS : data.bondedChestRows(),
                    data.spawnWeight(), data.ranges()));
        }
        return new BreedDataPayload(out);
    }

    public Map<ResourceLocation, BhBreedData> toMap() {
        Map<ResourceLocation, BhBreedData> map = new HashMap<>();
        for (Entry entry : entries) {
            ResourceLocation breedId = new ResourceLocation(IcysBetterHorses.MOD_ID, entry.breed());
            BhBreedData fallback = BhBreedData.builtIn(breedId);
            ResourceLocation archetypeId = ResourceLocation.tryParse(entry.archetype());
            ArchetypeType arch = archetypeId != null
                    ? BhRegistries.archetypeTypeRegistry().getValue(archetypeId)
                    : null;
            if (arch == null) {
                arch = fallback != null ? fallback.archetype() : BhContent.NONE.get();
            }
            map.put(breedId, new BhBreedData(arch, entry.chestRows(), entry.midChestRows(), entry.bondedChestRows(), entry.spawnWeight(),
                    entry.ranges()));
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
            buf.writeVarInt(entry.midChestRows());
            buf.writeVarInt(entry.bondedChestRows());
            buf.writeVarInt(entry.spawnWeight());
            Ranges r = entry.ranges();
            buf.writeDouble(r.lowSpeed());
            buf.writeDouble(r.highSpeed());
            buf.writeDouble(r.lowHealth());
            buf.writeDouble(r.highHealth());
            buf.writeDouble(r.lowJump());
            buf.writeDouble(r.highJump());
        }
    }

    public static BreedDataPayload decode(FriendlyByteBuf buf) {
        int count = Math.min(buf.readVarInt(), MAX_ENTRIES);
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(
                    buf.readUtf(MAX_NAME),
                    buf.readUtf(MAX_NAME),
                    Math.max(BhBreedData.CLASS_ROWS, Math.min(6, buf.readVarInt())),
                    Math.max(BhBreedData.CLASS_ROWS, Math.min(6, buf.readVarInt())),
                    Math.max(BhBreedData.CLASS_ROWS, Math.min(6, buf.readVarInt())),
                    buf.readVarInt(),
                    new Ranges(buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readDouble(), buf.readDouble(), buf.readDouble())));
        }
        return new BreedDataPayload(List.copyOf(entries));
    }
}
