package com.blackgear.vanillabackport.core.mixin.common.explorer_maps;

import com.blackgear.vanillabackport.common.level.items.maps.ExplorerMaps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExplorationMapFunction.class)
public abstract class ExplorationMapFunctionMixin {
    @ModifyReturnValue(method = "run", at = @At("RETURN"))
    private ItemStack vb$dedicatedExplorerMap(ItemStack result) {
        return ExplorerMaps.convert(result);
    }
}
