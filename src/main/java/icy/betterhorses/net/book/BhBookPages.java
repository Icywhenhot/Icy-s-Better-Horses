package icy.betterhorses.net.book;

import com.klikli_dev.modonomicon.data.BookPageType;
import com.klikli_dev.modonomicon.registry.BookPageTypeRegistry;

public final class BhBookPages {

    public static BookPageType<BhBreedCoatsPage> BREED_COATS;
    public static BookPageType<BhCartModelsPage> CART_MODELS;
    public static BookPageType<BhChargeMeterPage> CHARGE_METER;

    private BhBookPages() {}

    public static void init() {
        BREED_COATS = BookPageTypeRegistry.register(
                BhBreedCoatsPage.ID,
                BhBreedCoatsPage.CODEC,
                BhBreedCoatsPage.STREAM_CODEC);
        CART_MODELS = BookPageTypeRegistry.register(
                BhCartModelsPage.ID,
                BhCartModelsPage.CODEC,
                BhCartModelsPage.STREAM_CODEC);
        CHARGE_METER = BookPageTypeRegistry.register(
                BhChargeMeterPage.ID,
                BhChargeMeterPage.CODEC,
                BhChargeMeterPage.STREAM_CODEC);
    }
}
