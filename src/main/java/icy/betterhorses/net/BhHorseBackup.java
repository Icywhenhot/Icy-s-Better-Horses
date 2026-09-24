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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public final class BhHorseBackup {

    private static final String PREFIX = IcysBetterHorses.MOD_ID + ":backup=";
    private static final String ENTITY_ID = "BH_EntityId";
    private static final String SADDLE = "saddle";
    private static final String VANILLA_HORSE = "minecraft:horse";

    private BhHorseBackup() {}

    public static void write(BhBreedHorse horse, CompoundTag saved) {
        saved.putString(ENTITY_ID, EntityType.getKey(horse.getType()).toString());
        saved.putString("id", VANILLA_HORSE);

        CompoundTag backup = new CompoundTag();
        for (String key : saved.keySet()) {
            if (key.startsWith("BH_")) {
                backup.put(key, saved.get(key).copy());
            }
        }
        saved.getCompound("equipment").flatMap(e -> e.getCompound(SADDLE)).ifPresent(saddle -> {
            if (!saddle.getStringOr("id", "").startsWith("minecraft:")) {
                backup.put(SADDLE, saddle.copy());
            }
        });

        ListTag tags = new ListTag();
        for (Tag t : saved.getListOrEmpty("Tags")) {
            if (!(t instanceof StringTag(String s) && s.startsWith(PREFIX))) {
                tags.add(t);
            }
        }
        tags.add(StringTag.valueOf(PREFIX + backup));
        saved.put("Tags", tags);
    }

    public static @Nullable EntityType<?> savedType(@Nullable String id) {
        Identifier parsed = id == null ? null : Identifier.tryParse(id);
        if (parsed == null) {
            return null;
        }
        for (BreedType breed : BhRegistries.breedTypeRegistry()) {
            if (breed.entityType().identifier().equals(parsed)) {
                return BuiltInRegistries.ENTITY_TYPE.getOptional(parsed).orElse(null);
            }
        }
        return null;
    }

    public static @Nullable CompoundTag find(Entity entity) {
        for (String t : entity.entityTags()) {
            if (t.startsWith(PREFIX)) {
                try {
                    return TagParser.parseCompoundFully(t.substring(PREFIX.length()));
                } catch (CommandSyntaxException e) {
                    IcysBetterHorses.LOGGER.warn("bad horse backup on {}: {}", entity.getUUID(), e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    public static void forget(Entity entity) {
        entity.entityTags().removeIf(t -> t.startsWith(PREFIX));
    }

    public static @Nullable EntityType<?> typeOf(CompoundTag backup) {
        return savedType(backup.getStringOr(ENTITY_ID, ""));
    }

    public static void restoreInto(CompoundTag saved, CompoundTag backup) {
        for (String key : backup.keySet()) {
            if (!key.equals(SADDLE)) {
                saved.put(key, backup.get(key).copy());
            }
        }
        backup.getCompound(SADDLE).ifPresent(saddle -> {
            CompoundTag equipment = saved.getCompoundOrEmpty("equipment");
            if (!equipment.contains(SADDLE)) {
                equipment.put(SADDLE, saddle.copy());
                saved.put("equipment", equipment);
            }
        });
    }
}
