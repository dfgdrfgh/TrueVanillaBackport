package com.evandev.tiny_takeover_backport.mixin;

import net.minecraft.world.entity.npc.VillagerTrades;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

@Mixin(VillagerTrades.class)
public interface VillagerTradesAccessor {
    @Mutable
    @Accessor("EXPERIMENTAL_WANDERING_TRADER_TRADES")
    static void tiny_takeover_backport$setExperimentalTraderTrades(List<Pair<VillagerTrades.ItemListing[], Integer>> trades) {
        throw new AssertionError();
    }
}
