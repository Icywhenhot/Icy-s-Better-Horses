package icy.betterhorses.net.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public final class BreedType {

    private final ResourceKey<ArchetypeType> archetype;
    private final BreedCoatSet coats;
    private final ResourceKey<? extends EntityType<?>> entityType;
    private final Integer chestRowsOverride;
    private final Integer bondedChestRowsOverride;
    private final List<ResourceKey<AbilityType>> abilities;
    private final StabilizerBody stabilizerBody;

    private BreedType(Builder builder) {
        this.archetype = builder.archetype;
        this.coats = builder.coats;
        this.entityType = builder.entityType;
        this.chestRowsOverride = builder.chestRowsOverride;
        this.bondedChestRowsOverride = builder.bondedChestRowsOverride;
        this.abilities = new ArrayList<>(builder.abilities);
        this.stabilizerBody = builder.stabilizerBody;
    }

    public ResourceKey<ArchetypeType> archetype() {
        return archetype;
    }

    public BreedCoatSet coats() {
        return coats;
    }

    public ResourceKey<? extends EntityType<?>> entityType() {
        return entityType;
    }

    public Integer chestRowsOverride() {
        return chestRowsOverride;
    }

    public Integer bondedChestRowsOverride() {
        return bondedChestRowsOverride;
    }

    public List<ResourceKey<AbilityType>> abilities() {
        return List.copyOf(abilities);
    }

    public void addAbility(ResourceKey<AbilityType> ability) {
        java.util.Objects.requireNonNull(ability);
        if (!abilities.contains(ability)) abilities.add(ability);
    }

    public StabilizerBody stabilizerBody() {
        return stabilizerBody;
    }

    public static Builder builder(ResourceKey<ArchetypeType> archetype) {
        return new Builder(archetype);
    }

    public static Component displayName(ResourceKey<BreedType> key, boolean mixed) {
        String namespace = key.identifier().getNamespace();
        Component base = Component.translatable("breed." + namespace + "." + key.identifier().getPath());
        if (!mixed) {
            return base;
        }
        return Component.translatable("breed." + namespace + ".mix_format", base);
    }

    public static final class Builder {
        private final ResourceKey<ArchetypeType> archetype;
        private BreedCoatSet coats;
        private ResourceKey<? extends EntityType<?>> entityType;
        private Integer chestRowsOverride;
        private Integer bondedChestRowsOverride;
        private final List<ResourceKey<AbilityType>> abilities = new ArrayList<>();
        private StabilizerBody stabilizerBody = StabilizerBody.GENERIC;

        private Builder(ResourceKey<ArchetypeType> archetype) {
            this.archetype = archetype;
        }

        public Builder coats(String resourceNamespace, String folder, List<String> coatIds, boolean hasFoalVariant) {
            this.coats = new BreedCoatSet(resourceNamespace, folder, coatIds, hasFoalVariant);
            return this;
        }

        public Builder entityType(ResourceKey<? extends EntityType<?>> key) {
            this.entityType = key;
            return this;
        }

        public Builder chestRows(int rows) {
            this.chestRowsOverride = rows;
            return this;
        }

        public Builder bondedChestRows(int rows) {
            this.bondedChestRowsOverride = rows;
            return this;
        }

        public Builder ability(ResourceKey<AbilityType> key) {
            this.abilities.add(key);
            return this;
        }

        public Builder stabilizerBody(StabilizerBody body) {
            this.stabilizerBody = body;
            return this;
        }

        public BreedType build() {
            if (coats == null) {
                throw new IllegalStateException("BreedType requires coats() to be set before build()");
            }
            if (entityType == null) {
                throw new IllegalStateException("BreedType requires entityType() to be set before build()");
            }
            return new BreedType(this);
        }
    }
}
