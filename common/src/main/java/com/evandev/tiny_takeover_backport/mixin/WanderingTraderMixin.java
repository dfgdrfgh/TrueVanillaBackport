package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.registry.ModTradeSelection;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin {
    @ModifyExpressionValue(method = "updateTrades", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/npc/WanderingTrader;random:Lnet/minecraft/util/RandomSource;"))
    private RandomSource tiny_takeover_backport$seedUncommonTrades(RandomSource original) {
        WanderingTrader trader = (WanderingTrader) (Object) this;
        return trader.level() instanceof ServerLevel level ? ModTradeSelection.wanderingSequence(level, "uncommon") : original;
    }
}
