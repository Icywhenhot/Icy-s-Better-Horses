package icy.betterhorses.net.book;

import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.data.BookPageType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class BhCartModelsPage extends BookPage {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "cart_models");

    public static final MapCodec<BhCartModelsPage> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(BookPage::getId),
                    BookCondition.CODEC
                            .optionalFieldOf("condition", new BookNoneCondition())
                            .forGetter(BookPage::getCondition)
            ).apply(instance, BhCartModelsPage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BhCartModelsPage> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, BookPage::getId,
                    BookCondition.STREAM_CODEC, BookPage::getCondition,
                    BhCartModelsPage::new);

    public BhCartModelsPage(String id, BookCondition condition) {
        super(id, condition);
    }

    @Override
    public BookPageType<?> type() {
        return BhBookPages.CART_MODELS;
    }

    @Override
    public boolean matchesQuery(String query, Level level) {
        return "cart".contains(query);
    }
}
