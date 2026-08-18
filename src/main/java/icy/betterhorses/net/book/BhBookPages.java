package icy.betterhorses.net.book;

import com.klikli_dev.modonomicon.data.BookPageType;
import com.klikli_dev.modonomicon.registry.BookPageTypeRegistry;

public final class BhBookPages {

    public static BookPageType<BhBreedCoatsPage> BREED_COATS;

    private BhBookPages() {}

    public static void init() {
        BREED_COATS = BookPageTypeRegistry.register(
                BhBreedCoatsPage.ID,
                BhBreedCoatsPage.CODEC,
                BhBreedCoatsPage.STREAM_CODEC);
    }
}
