package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.registry.ModTradeSelection;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {
    @Inject(method = "addOffersFromItemListings", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$seedTrades(MerchantOffers offers, VillagerTrades.ItemListing[] listings, int count, CallbackInfo ci) {
        if (ModTradeSelection.addOffers((AbstractVillager) (Object) this, offers, listings, count)) ci.cancel();
    }
}
