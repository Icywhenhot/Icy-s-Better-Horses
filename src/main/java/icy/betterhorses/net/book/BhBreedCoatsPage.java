package icy.betterhorses.net.book;

import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.data.BookPageType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BhBreedCoatsPage extends BookPage {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "breed_coats");

    private static final int MAX_ENTITIES = 16;

    public static final MapCodec<BhBreedCoatsPage> CODEC = RecordCodecBuilder.<BhBreedCoatsPage>mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("entity", "").forGetter(page -> ""),
                    Codec.STRING.listOf().optionalFieldOf("entities", List.of()).forGetter(page -> page.entityIds),
                    Codec.STRING.optionalFieldOf("id", "").forGetter(BookPage::getId),
                    BookCondition.CODEC
                            .optionalFieldOf("condition", new BookNoneCondition())
                            .forGetter(BookPage::getCondition)
            ).apply(instance, (entity, entities, id, condition) -> {
                List<String> ids = new ArrayList<>(entities);
                if (ids.isEmpty() && !entity.isEmpty()) ids.add(entity);
                return new BhBreedCoatsPage(ids, id, condition);
            })).validate(page -> page.entityIds.isEmpty() || page.entityIds.size() > MAX_ENTITIES
                    ? DataResult.error(() -> "invalid preview model count") : DataResult.success(page));

    public static final StreamCodec<RegistryFriendlyByteBuf, BhBreedCoatsPage> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(MAX_ENTITIES)), page -> page.entityIds,
                    ByteBufCodecs.STRING_UTF8, BookPage::getId,
                    BookCondition.STREAM_CODEC, BookPage::getCondition,
                    BhBreedCoatsPage::new);

    private final List<String> entityIds;

    public BhBreedCoatsPage(List<String> entityIds, String id, BookCondition condition) {
        super(id, condition);
        this.entityIds = List.copyOf(entityIds);
    }

    public String getEntityId() {
        return entityIds.isEmpty() ? "" : entityIds.getFirst();
    }

    public List<String> getEntityIds() {
        return entityIds;
    }

    @Override
    public BookPageType<?> type() {
        return BhBookPages.BREED_COATS;
    }

    @Override
    public boolean matchesQuery(String query, Level level) {
        return entityIds.stream().anyMatch(id -> id.toLowerCase(Locale.ROOT).contains(query));
    }
}
