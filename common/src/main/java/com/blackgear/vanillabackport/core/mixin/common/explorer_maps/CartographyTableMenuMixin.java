package com.blackgear.vanillabackport.core.mixin.common.explorer_maps;

import com.blackgear.vanillabackport.common.level.items.maps.ExplorerMaps;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CartographyTableMenu.class)
public abstract class CartographyTableMenuMixin {
    @Shadow @Final private ResultContainer resultContainer;
    @Shadow public abstract void broadcastChanges();

    @Inject(method = "setupResultSlot", at = @At("HEAD"), cancellable = true)
    private void vb$onlyExtendTaggedMaps(ItemStack map, ItemStack material, ItemStack result, CallbackInfo ci) {
        if (material.is(Items.PAPER) && !map.is(ExplorerMaps.EXTENDABLE_MAPS)) {
            this.resultContainer.removeItemNoUpdate(2);
            this.broadcastChanges();
            ci.cancel();
        }
    }

    @WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean vb$moveAllMapItems(ItemStack stack, Item item, Operation<Boolean> original) {
        return item == Items.FILLED_MAP ? stack.getItem() instanceof MapItem : original.call(stack, item);
    }
}
