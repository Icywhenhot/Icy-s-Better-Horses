package icy.betterhorses.net.book;

import com.klikli_dev.modonomicon.data.LoaderRegistry;

public final class BhBookPages {

    private BhBookPages() {}

    public static void init() {
        LoaderRegistry.registerPageLoader(BhBreedCoatsPage.ID,
                BhBreedCoatsPage::fromJson, BhBreedCoatsPage::fromNetwork);
        LoaderRegistry.registerPageLoader(BhCartModelsPage.ID,
                BhCartModelsPage::fromJson, BhCartModelsPage::fromNetwork);
        LoaderRegistry.registerPageLoader(BhChargeMeterPage.ID,
                BhChargeMeterPage::fromJson, BhChargeMeterPage::fromNetwork);
    }
}
