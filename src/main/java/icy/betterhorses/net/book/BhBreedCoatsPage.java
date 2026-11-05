package icy.betterhorses.net.book;

import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.page.BookPage;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class BhBreedCoatsPage extends BookPage {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "breed_coats");

    private static final int MAX_ENTITIES = 16;

    private final List<String> entityIds;

    public BhBreedCoatsPage(String entityId, String anchor, BookCondition condition) {
        this(List.of(entityId), anchor, condition);
    }

    public BhBreedCoatsPage(List<String> entityIds, String anchor, BookCondition condition) {
        super(anchor, condition);
        if (entityIds.isEmpty() || entityIds.size() > MAX_ENTITIES) {
            throw new IllegalArgumentException("invalid preview model count");
        }
        this.entityIds = List.copyOf(entityIds);
    }

    public static BhBreedCoatsPage fromJson(JsonObject json, HolderLookup.Provider registries) {
        List<String> entityIds = new ArrayList<>();
        if (json.has("entities")) {
            var entities = GsonHelper.getAsJsonArray(json, "entities");
            if (entities.isEmpty() || entities.size() > MAX_ENTITIES) {
                throw new IllegalArgumentException("invalid preview model count");
            }
            for (var entity : entities) {
                entityIds.add(GsonHelper.convertToString(entity, "entity"));
            }
        } else {
            entityIds.add(GsonHelper.getAsString(json, "entity"));
        }
        String anchor = GsonHelper.getAsString(json, "anchor", GsonHelper.getAsString(json, "id", ""));
        BookCondition condition = json.has("condition")
                ? BookCondition.fromJson(ID, json.getAsJsonObject("condition"), registries)
                : new BookNoneCondition();
        return new BhBreedCoatsPage(entityIds, anchor, condition);
    }

    public static BhBreedCoatsPage fromNetwork(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 1 || count > MAX_ENTITIES) {
            throw new IllegalArgumentException("invalid preview model count");
        }
        List<String> entityIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entityIds.add(buffer.readUtf());
        }
        return new BhBreedCoatsPage(entityIds, buffer.readUtf(), BookCondition.fromNetwork(buffer));
    }

    public String getEntityId() {
        return entityIds.getFirst();
    }

    public List<String> getEntityIds() {
        return entityIds;
    }

    @Override
    public ResourceLocation getType() {
        return ID;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(entityIds.size());
        for (String entityId : entityIds) {
            buffer.writeUtf(entityId);
        }
        super.toNetwork(buffer);
    }

    @Override
    public boolean matchesQuery(String query) {
        return entityIds.stream().anyMatch(id -> id.toLowerCase(Locale.ROOT).contains(query));
    }
}
