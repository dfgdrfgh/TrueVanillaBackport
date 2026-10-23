package com.evandev.tiny_takeover_backport.registry;

import com.evandev.tiny_takeover_backport.mixin.ItemsForEmeraldsAccessor;
import com.evandev.tiny_takeover_backport.mixin.VillagerTradesAccessor;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.ItemCost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ModTrades {
    private ModTrades() {}

    public static void init() {
        var librarian = VillagerTrades.TRADES.get(VillagerProfession.LIBRARIAN);
        librarian.put(5, replaceNameTags(librarian.get(5)));
        var experimental = VillagerTrades.EXPERIMENTAL_TRADES.get(VillagerProfession.LIBRARIAN);
        experimental.put(5, replaceNameTags(experimental.get(5)));
        List<VillagerTrades.ItemListing> common = new ArrayList<>(Arrays.asList(VillagerTrades.WANDERING_TRADER_TRADES.get(1)));
        common.add(sell(Items.NAME_TAG, 1, 5, 0));
        common.add(sell(ModRegistry.GOLDEN_DANDELION_ITEM, 2, 12, 0));
        VillagerTrades.WANDERING_TRADER_TRADES.put(1, common.toArray(VillagerTrades.ItemListing[]::new));
        var experimentalTrader = new ArrayList<>(VillagerTrades.EXPERIMENTAL_WANDERING_TRADER_TRADES);
        Pair<VillagerTrades.ItemListing[], Integer> commonPool = experimentalTrader.get(2);
        List<VillagerTrades.ItemListing> experimentalCommon = new ArrayList<>(Arrays.asList(commonPool.getLeft()));
        experimentalCommon.add(sell(Items.NAME_TAG, 1, 5, 0));
        experimentalCommon.add(sell(ModRegistry.GOLDEN_DANDELION_ITEM, 2, 12, 0));
        experimentalTrader.set(2, Pair.of(experimentalCommon.toArray(VillagerTrades.ItemListing[]::new), commonPool.getRight()));
        VillagerTradesAccessor.tiny_takeover_backport$setExperimentalTraderTrades(experimentalTrader);
    }

    private static VillagerTrades.ItemListing[] replaceNameTags(VillagerTrades.ItemListing[] listings) {
        List<VillagerTrades.ItemListing> updated = new ArrayList<>();
        for (VillagerTrades.ItemListing listing : listings) {
            if (listing instanceof ItemsForEmeraldsAccessor accessor && accessor.tiny_takeover_backport$getItemStack().is(Items.NAME_TAG)) {
                updated.add(sell(Items.RED_CANDLE, 3, 12, 30));
                updated.add(sell(Items.YELLOW_CANDLE, 3, 12, 30));
            } else {
                updated.add(listing);
            }
        }
        return updated.toArray(VillagerTrades.ItemListing[]::new);
    }

    private static VillagerTrades.ItemListing sell(Item item, int emeralds, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, emeralds),
                new ItemStack(item), maxUses, xp, 0.05F);
    }
}
