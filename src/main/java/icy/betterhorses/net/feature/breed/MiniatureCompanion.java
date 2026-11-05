package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

public final class MiniatureCompanion implements BreedAbility {

    private static final TagKey<Item> PRODUCE = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "farm_produce"));

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.level() instanceof ServerLevel) || horse.isBaby() || data.bh_isAbilityPaused()
                || !data.bh_hasChestGear() || horse.tickCount % 10 != 0) {
            return;
        }
        boolean eggs = BhAbility.MINIATURE_PICKUP.on();
        boolean basket = BhAbility.MINIATURE_BASKET.on() && BhHorseTraits.bondTier(data.bh_getBond()) >= 1;
        if (!eggs && !basket) {
            return;
        }
        DonkeyForaging.pickup(horse, data, stack -> eggs && stack.is(Tags.Items.EGGS)
                || basket && (stack.is(ItemTags.WOOL) || stack.is(Tags.Items.CROPS) || stack.is(PRODUCE)));
    }
}
