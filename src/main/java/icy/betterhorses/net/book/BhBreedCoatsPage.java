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
import java.util.Locale;

public class BhBreedCoatsPage extends BookPage {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "breed_coats");

    public static final MapCodec<BhBreedCoatsPage> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("entity").forGetter(page -> page.entityId),
                    Codec.STRING.optionalFieldOf("id", "").forGetter(BookPage::getId),
                    BookCondition.CODEC
                            .optionalFieldOf("condition", new BookNoneCondition())
                            .forGetter(BookPage::getCondition)
            ).apply(instance, BhBreedCoatsPage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BhBreedCoatsPage> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, page -> page.entityId,
                    ByteBufCodecs.STRING_UTF8, BookPage::getId,
                    BookCondition.STREAM_CODEC, BookPage::getCondition,
                    BhBreedCoatsPage::new);

    private final String entityId;

    public BhBreedCoatsPage(String entityId, String id, BookCondition condition) {
        super(id, condition);
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }

    @Override
    public BookPageType<?> type() {
        return BhBookPages.BREED_COATS;
    }

    @Override
    public boolean matchesQuery(String query, Level level) {
        return entityId.toLowerCase(Locale.ROOT).contains(query);
    }
}
