package com.blackgear.vanillabackport.core.mixin.client.explorer_maps;

import com.blackgear.vanillabackport.common.level.items.maps.ExplorerMaps;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CartographyTableScreen.class)
public abstract class CartographyTableScreenMixin extends AbstractContainerScreen<CartographyTableMenu> {
    protected CartographyTableScreenMixin(CartographyTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;scale:B"))
    private byte vb$showUnextendableMapError(byte scale) {
        return this.menu.getSlot(0).getItem().is(ExplorerMaps.EXTENDABLE_MAPS) ? scale : (byte)4;
    }
}
