package icy.betterhorses.net.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the extractor's {@link GuiRenderState}, which is the only way to submit custom geometry to
 * the GUI: {@code GuiRenderState.addGuiElement} takes any element that can build its own vertices,
 * but {@link GuiGraphicsExtractor} keeps the state private and offers no hook of its own.
 *
 * @see icy.betterhorses.net.client.BhVector
 */
@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {

    @Accessor("guiRenderState")
    GuiRenderState bh_guiRenderState();
}
