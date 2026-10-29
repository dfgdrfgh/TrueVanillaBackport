package com.blackgear.vanillabackport.core.mixin.common.explorer_maps;

import com.blackgear.vanillabackport.common.level.items.maps.ExplorerMaps;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow @Final @Mutable private Item item;
    @Shadow @Final @Mutable private PatchedDataComponentMap components;

    // Both persistent codecs and the network decoder use this constructor. Doing
    // this after decoding also handles maps nested in bundles and merchant offers.
    @Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/DataComponentPatch;)V", at = @At("RETURN"))
    private void vb$upgradeLegacyExplorerMap(Holder<Item> item, int count, DataComponentPatch patch, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        ItemStack converted = ExplorerMaps.convert(self);
        if (converted != self) {
            this.item = converted.getItem();
            this.components = (PatchedDataComponentMap)converted.getComponents();
        }
    }
}
