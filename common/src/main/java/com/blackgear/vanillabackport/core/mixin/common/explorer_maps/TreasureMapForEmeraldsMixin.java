package com.blackgear.vanillabackport.core.mixin.common.explorer_maps;

import com.blackgear.vanillabackport.common.level.items.maps.ExplorerMaps;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.world.entity.npc.VillagerTrades$TreasureMapForEmeralds")
public abstract class TreasureMapForEmeraldsMixin {
    @ModifyArg(method = "getOffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffer;<init>(Lnet/minecraft/world/item/trading/ItemCost;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;IIF)V"), index = 2)
    private ItemStack vb$dedicatedExplorerMap(ItemStack result) {
        return ExplorerMaps.convert(result);
    }
}
