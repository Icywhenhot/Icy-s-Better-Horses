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
import java.util.Locale;

public class BhBreedCoatsPage extends BookPage {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "breed_coats");

    private final String entityId;

    public BhBreedCoatsPage(String entityId, String id, BookCondition condition) {
        super(id, condition);
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }

    public static BhBreedCoatsPage fromJson(Identifier entryId, JsonObject json, HolderLookup.Provider provider) {
        var id = GsonHelper.getAsString(json, "id", GsonHelper.getAsString(json, "anchor", ""));
        var condition = json.has("condition")
                ? BookCondition.fromJson(entryId, json.getAsJsonObject("condition"), provider)
                : new BookNoneCondition();
        return new BhBreedCoatsPage(GsonHelper.getAsString(json, "entity"), id, condition);
    }

    public static BhBreedCoatsPage fromNetwork(RegistryFriendlyByteBuf buffer) {
        var entity = buffer.readUtf();
        var id = buffer.readUtf();
        return new BhBreedCoatsPage(entity, id, BookCondition.fromNetwork(buffer));
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(this.entityId);
        super.toNetwork(buffer);
    }

    @Override
    public Identifier getType() {
        return ID;
    }

    @Override
    public boolean matchesQuery(String query, Level level) {
        return entityId.toLowerCase(Locale.ROOT).contains(query);
    }
}
