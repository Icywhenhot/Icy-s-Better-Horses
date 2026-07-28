package icy.betterhorses.net.item;

import icy.betterhorses.net.client.render.HorseCartItemRenderer;
import net.minecraft.world.item.Item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * The horse-cart gear item, rendered in inventory/hand as the actual 3D cart model via GeckoLib
 * (the item model json uses the {@code geckolib:geckolib} special renderer, which pulls the
 * renderer from {@link #createGeoRenderer}).
 *
 * <p>Client-only rendering classes ({@link GeoItemRenderer}, {@link HorseCartItemRenderer}) are
 * only touched inside {@code createGeoRenderer}, which GeckoLib invokes client-side only — so this
 * item is safe to load on a dedicated server.</p>
 */
public final class HorseCartItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HorseCartItem(Properties properties) {
        super(properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private HorseCartItemRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new HorseCartItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static display in the slot/hand; no animation needed.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
