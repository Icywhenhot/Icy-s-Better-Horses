package icy.betterhorses.net.book;

import com.klikli_dev.modonomicon.book.BookTextHolder;
import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookTextPage;
import com.klikli_dev.modonomicon.data.BookPageType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class BhChargeMeterPage extends BookTextPage {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "charge_meter");

    public static final MapCodec<BhChargeMeterPage> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BookTextHolder.CODEC
                            .optionalFieldOf("title", BookTextHolder.EMPTY)
                            .forGetter(BookTextPage::getTitle),
                    BookTextHolder.CODEC
                            .optionalFieldOf("text", BookTextHolder.EMPTY)
                            .forGetter(BookTextPage::getText),
                    Codec.BOOL.optionalFieldOf("use_markdown_in_title", false)
                            .forGetter(BookTextPage::useMarkdownInTitle),
                    Codec.BOOL.optionalFieldOf("show_title_separator", true)
                            .forGetter(BookTextPage::showTitleSeparator),
                    Codec.STRING.optionalFieldOf("id", "").forGetter(BookPage::getId),
                    BookCondition.CODEC
                            .optionalFieldOf("condition", new BookNoneCondition())
                            .forGetter(BookPage::getCondition)
            ).apply(instance, BhChargeMeterPage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BhChargeMeterPage> STREAM_CODEC =
            StreamCodec.composite(
                    BookTextHolder.STREAM_CODEC, BookTextPage::getTitle,
                    BookTextHolder.STREAM_CODEC, BookTextPage::getText,
                    ByteBufCodecs.BOOL, BookTextPage::useMarkdownInTitle,
                    ByteBufCodecs.BOOL, BookTextPage::showTitleSeparator,
                    ByteBufCodecs.STRING_UTF8, BookPage::getId,
                    BookCondition.STREAM_CODEC, BookPage::getCondition,
                    BhChargeMeterPage::new);

    public BhChargeMeterPage(BookTextHolder title, BookTextHolder text, boolean useMarkdownInTitle,
                             boolean showTitleSeparator, String id, BookCondition condition) {
        super(title, text, useMarkdownInTitle, showTitleSeparator, id, condition);
    }

    @Override
    public BookPageType<?> type() {
        return BhBookPages.CHARGE_METER;
    }
}
