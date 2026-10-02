package icy.betterhorses.net.book;

import com.klikli_dev.modonomicon.book.BookTextHolder;
import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.page.BookTextPage;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.GsonHelper;
import com.klikli_dev.modonomicon.util.BookGsonHelper;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class BhChargeMeterPage extends BookTextPage {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "charge_meter");

    public BhChargeMeterPage(BookTextHolder title, BookTextHolder text, boolean useMarkdownInTitle,
                             boolean showTitleSeparator, String id, BookCondition condition) {
        super(title, text, useMarkdownInTitle, showTitleSeparator, id, condition);
    }

    public static BhChargeMeterPage fromJson(Identifier entryId, JsonObject json, HolderLookup.Provider provider) {
        var id = GsonHelper.getAsString(json, "id", GsonHelper.getAsString(json, "anchor", ""));
        var condition = json.has("condition")
                ? BookCondition.fromJson(entryId, json.getAsJsonObject("condition"), provider)
                : new BookNoneCondition();
        return new BhChargeMeterPage(BookGsonHelper.getAsBookTextHolder(json, "title", BookTextHolder.EMPTY, provider),
                BookGsonHelper.getAsBookTextHolder(json, "text", BookTextHolder.EMPTY, provider),
                GsonHelper.getAsBoolean(json, "use_markdown_in_title", false),
                GsonHelper.getAsBoolean(json, "show_title_separator", true), id, condition);
    }

    public static BhChargeMeterPage fromNetwork(RegistryFriendlyByteBuf buffer) {
        var title = BookTextHolder.fromNetwork(buffer);
        var markdown = buffer.readBoolean();
        var separator = buffer.readBoolean();
        var text = BookTextHolder.fromNetwork(buffer);
        var id = buffer.readUtf();
        return new BhChargeMeterPage(title, text, markdown, separator, id, BookCondition.fromNetwork(buffer));
    }

    @Override
    public Identifier getType() {
        return ID;
    }
}
