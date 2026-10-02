package icy.betterhorses.net.mixin;

import com.klikli_dev.modonomicon.book.page.BookRecipePage;
import com.klikli_dev.modonomicon.book.page.BookSpotlightPage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {BookRecipePage.class, BookSpotlightPage.class}, remap = false)
public abstract class BookItemNameMixin {

    @Redirect(method = "build", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;", remap = true))
    private Component bh_copyItemName(ItemStack stack) {
        return stack.getHoverName().copy();
    }
}
