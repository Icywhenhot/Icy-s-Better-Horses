package icy.betterhorses.net.book;

import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.GsonHelper;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class BhCartModelsPage extends BookPage {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "cart_models");

    public BhCartModelsPage(String id, BookCondition condition) {
        super(id, condition);
    }

    public static BhCartModelsPage fromJson(Identifier entryId, JsonObject json, HolderLookup.Provider provider) {
        var id = GsonHelper.getAsString(json, "id", GsonHelper.getAsString(json, "anchor", ""));
        var condition = json.has("condition")
                ? BookCondition.fromJson(entryId, json.getAsJsonObject("condition"), provider)
                : new BookNoneCondition();
        return new BhCartModelsPage(id, condition);
    }

    public static BhCartModelsPage fromNetwork(RegistryFriendlyByteBuf buffer) {
        var id = buffer.readUtf();
        return new BhCartModelsPage(id, BookCondition.fromNetwork(buffer));
    }

    @Override
    public Identifier getType() {
        return ID;
    }

    @Override
    public boolean matchesQuery(String query, Level level) {
        return "cart".contains(query);
    }
}
