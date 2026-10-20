package com.evandev.tiny_takeover_backport.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import java.util.ArrayList;
import java.util.Arrays;

public final class ModTradeSelection {
    private ModTradeSelection() {}

    public static RandomSource wanderingSequence(ServerLevel level, String pool) {
        return level.getRandomSequence(sequenceId("minecraft", "trade_set/wandering_trader/" + pool));
    }

    public static boolean addOffers(AbstractVillager merchant, MerchantOffers offers, VillagerTrades.ItemListing[] listings, int count) {
        if (!(merchant.level() instanceof ServerLevel level)) return false;
        RandomSource random;
        if (merchant instanceof Villager villager) {
            ResourceLocation profession = BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession());
            random = level.getRandomSequence(sequenceId(profession.getNamespace(),
                    "trade_set/" + profession.getPath() + "/level_" + villager.getVillagerData().getLevel()));
        } else if (merchant instanceof WanderingTrader) {
            String pool = listings == VillagerTrades.WANDERING_TRADER_TRADES.get(1) ? "common" : null;
            for (int i = 0; i < VillagerTrades.EXPERIMENTAL_WANDERING_TRADER_TRADES.size(); i++) {
                if (listings == VillagerTrades.EXPERIMENTAL_WANDERING_TRADER_TRADES.get(i).getLeft()) {
                    pool = switch (i) {
                        case 0 -> "buying";
                        case 1 -> "uncommon";
                        default -> "common";
                    };
                    break;
                }
            }
            if (pool == null) return false;
            random = wanderingSequence(level, pool);
        } else {
            return false;
        }

        // Vanilla 26.1 draws from the remaining entries, removing each attempted
        // trade. Invalid biome/experiment-specific offers do not consume a slot.
        var remaining = new ArrayList<>(Arrays.asList(listings));
        int found = 0;
        while (found < count && !remaining.isEmpty()) {
            VillagerTrades.ItemListing listing = remaining.remove(random.nextInt(remaining.size()));
            MerchantOffer offer = listing.getOffer(merchant, random);
            if (offer != null) {
                offers.add(offer);
                found++;
            }
        }
        return true;
    }

    private static ResourceLocation sequenceId(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
