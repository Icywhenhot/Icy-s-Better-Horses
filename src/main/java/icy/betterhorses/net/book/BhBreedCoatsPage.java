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

public class BhBreedCoatsPage extends BookPage {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "breed_coats");

    private final String entityId;

    public BhBreedCoatsPage(String entityId, String anchor, BookCondition condition) {
        super(anchor, condition);
        this.entityId = entityId;
    }

    public static BhBreedCoatsPage fromJson(JsonObject json, HolderLookup.Provider registries) {
        String entityId = GsonHelper.getAsString(json, "entity");
        String anchor = GsonHelper.getAsString(json, "anchor", GsonHelper.getAsString(json, "id", ""));
        BookCondition condition = json.has("condition")
                ? BookCondition.fromJson(ID, json.getAsJsonObject("condition"), registries)
                : new BookNoneCondition();
        return new BhBreedCoatsPage(entityId, anchor, condition);
    }

    public static BhBreedCoatsPage fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new BhBreedCoatsPage(buffer.readUtf(), buffer.readUtf(), BookCondition.fromNetwork(buffer));
    }

    public String getEntityId() {
        return entityId;
    }

    @Override
    public ResourceLocation getType() {
        return ID;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(entityId);
        super.toNetwork(buffer);
    }

    @Override
    public boolean matchesQuery(String query) {
        return entityId.toLowerCase(Locale.ROOT).contains(query);
    }
}
