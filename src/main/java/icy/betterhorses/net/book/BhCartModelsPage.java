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

public class BhCartModelsPage extends BookPage {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "cart_models");

    public BhCartModelsPage(String anchor, BookCondition condition) {
        super(anchor, condition);
    }

    public static BhCartModelsPage fromJson(JsonObject json, HolderLookup.Provider registries) {
        String anchor = GsonHelper.getAsString(json, "anchor", GsonHelper.getAsString(json, "id", ""));
        BookCondition condition = json.has("condition")
                ? BookCondition.fromJson(ID, json.getAsJsonObject("condition"), registries)
                : new BookNoneCondition();
        return new BhCartModelsPage(anchor, condition);
    }

    public static BhCartModelsPage fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new BhCartModelsPage(buffer.readUtf(), BookCondition.fromNetwork(buffer));
    }

    @Override
    public ResourceLocation getType() {
        return ID;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        super.toNetwork(buffer);
    }

    @Override
    public boolean matchesQuery(String query) {
        return "cart".contains(query);
    }
}
