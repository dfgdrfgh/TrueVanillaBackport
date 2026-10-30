package com.blackgear.vanillabackport.core.mixin.common.explorer_maps;

import com.blackgear.vanillabackport.common.level.items.maps.ExplorerMaps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.MapExtendingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapExtendingRecipe.class)
public abstract class MapExtendingRecipeMixin {
    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void vb$onlyExtendTaggedMaps(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        boolean matches = input.width() == 3 && input.height() == 3;
        if (matches) {
            for (int slot = 0; slot < 9; slot++) {
                if (slot == 4 ? !input.getItem(slot).is(ExplorerMaps.EXTENDABLE_MAPS) : !input.getItem(slot).is(Items.PAPER)) {
                    matches = false;
                    break;
                }
            }
        }
        MapItemSavedData data = matches ? MapItem.getSavedData(input.getItem(4), level) : null;
        cir.setReturnValue(data != null && data.scale < 4);
    }

    @Inject(method = "findFilledMap", at = @At("HEAD"), cancellable = true)
    private static void vb$findTaggedMap(CraftingInput input, CallbackInfoReturnable<ItemStack> cir) {
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.is(ExplorerMaps.EXTENDABLE_MAPS)) {
                cir.setReturnValue(stack);
                return;
            }
        }
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
