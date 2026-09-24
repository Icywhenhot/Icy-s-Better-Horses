package icy.betterhorses.net;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public final class BhHorseBackup {

    private static final String PREFIX = IcysBetterHorses.RESOURCE_NAMESPACE + ":backup=";
    private static final String ENTITY_ID = "BH_EntityId";
    private static final String SADDLE = "SaddleItem";
    private static final String VANILLA_HORSE = "minecraft:horse";

    private BhHorseBackup() {}

    public static void write(BhBreedHorse horse, CompoundTag saved) {
        saved.putString(ENTITY_ID, EntityType.getKey(horse.getType()).toString());
        saved.putString("id", VANILLA_HORSE);

        CompoundTag backup = new CompoundTag();
        for (String key : saved.getAllKeys()) {
            if (key.startsWith("BH_")) {
                backup.put(key, saved.get(key).copy());
            }
        }
        if (saved.contains(SADDLE, Tag.TAG_COMPOUND)
                && !saved.getCompound(SADDLE).getString("id").startsWith("minecraft:")) {
            backup.put(SADDLE, saved.getCompound(SADDLE).copy());
        }

        ListTag tags = new ListTag();
        for (Tag t : saved.getList("Tags", Tag.TAG_STRING)) {
            if (!t.getAsString().startsWith(PREFIX)) {
                tags.add(t);
            }
        }
        tags.add(StringTag.valueOf(PREFIX + backup));
        saved.put("Tags", tags);
    }

    public static @Nullable EntityType<?> savedType(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null) {
            return null;
        }
        for (BreedType breed : BhRegistries.breedTypeRegistry()) {
            if (breed.entityType().location().equals(parsed)) {
                return BuiltInRegistries.ENTITY_TYPE.getOptional(parsed).orElse(null);
            }
        }
        return null;
    }

    public static @Nullable CompoundTag find(Entity entity) {
        for (String t : entity.getTags()) {
            if (t.startsWith(PREFIX)) {
                try {
                    return TagParser.parseTag(t.substring(PREFIX.length()));
                } catch (CommandSyntaxException e) {
                    IcysBetterHorses.LOGGER.warn("bad horse backup on {}: {}", entity.getUUID(), e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    public static void forget(Entity entity) {
        entity.getTags().removeIf(t -> t.startsWith(PREFIX));
    }

    public static @Nullable EntityType<?> typeOf(CompoundTag backup) {
        return savedType(backup.getString(ENTITY_ID));
    }

    public static void restoreInto(CompoundTag saved, CompoundTag backup) {
        for (String key : backup.getAllKeys()) {
            if (!key.equals(SADDLE)) {
                saved.put(key, backup.get(key).copy());
            }
        }
        if (backup.contains(SADDLE, Tag.TAG_COMPOUND) && !saved.contains(SADDLE, Tag.TAG_COMPOUND)) {
            saved.put(SADDLE, backup.getCompound(SADDLE).copy());
        }
    }
}
