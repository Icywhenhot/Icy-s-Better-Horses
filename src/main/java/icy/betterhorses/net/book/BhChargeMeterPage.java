package icy.betterhorses.net.book;

import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.book.page.BookTextPage;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class BhChargeMeterPage extends BookTextPage {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "charge_meter");

    private BhChargeMeterPage(BookTextPage page) {
        super(page.getTitle(), page.getText(), page.useMarkdownInTitle(), page.showTitleSeparator(),
                page.getAnchor(), page.getCondition());
    }

    public static BhChargeMeterPage fromJson(JsonObject json, HolderLookup.Provider registries) {
        JsonObject compatible = json.deepCopy();
        if (!compatible.has("anchor") && compatible.has("id")) {
            compatible.add("anchor", compatible.get("id"));
        }
        if (!compatible.has("use_markdown_title") && compatible.has("use_markdown_in_title")) {
            compatible.add("use_markdown_title", compatible.get("use_markdown_in_title"));
        }
        return new BhChargeMeterPage(BookTextPage.fromJson(ID, compatible, registries));
    }

    public static BhChargeMeterPage fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new BhChargeMeterPage(BookTextPage.fromNetwork(buffer));
    }

    @Override
    public ResourceLocation getType() {
        return ID;
    }
}
