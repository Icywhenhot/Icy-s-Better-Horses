package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IcysBetterHorses;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Maps Modonomicon's inventory model lookup to the model loaded through Fabric. */
@Mixin(ModelManager.class)
public abstract class BookModelManagerMixin {
    @Unique
    private static final ModelResourceLocation bh_BOOK_INVENTORY = ModelResourceLocation.inventory(
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_handbook_book"));
    @Unique
    private static final ResourceLocation bh_BOOK_MODEL = ResourceLocation.fromNamespaceAndPath(
            IcysBetterHorses.MOD_ID, "item/stable_handbook_book");

    @Inject(method = "getModel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At("HEAD"), cancellable = true)
    private void bh_bookModel(ModelResourceLocation id, CallbackInfoReturnable<BakedModel> cir) {
        if (bh_BOOK_INVENTORY.equals(id)) {
            cir.setReturnValue(((FabricBakedModelManager) (Object) this).getModel(bh_BOOK_MODEL));
        }
    }
}
