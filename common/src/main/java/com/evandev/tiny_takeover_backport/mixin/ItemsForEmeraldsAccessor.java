package com.evandev.tiny_takeover_backport.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.entity.npc.VillagerTrades$ItemsForEmeralds")
public interface ItemsForEmeraldsAccessor {
    @Accessor("itemStack")
    ItemStack tiny_takeover_backport$getItemStack();
}
