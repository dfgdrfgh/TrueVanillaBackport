package com.blackgear.vanillabackport.core.mixin.common.recipe;

import com.blackgear.vanillabackport.common.level.recipes.DynamicRecipeResources;
import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow @Final private HolderLookup.Provider registries;

    @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), argsOnly = true)
    private Map<ResourceLocation, JsonElement> vb$dynamicRecipes(Map<ResourceLocation, JsonElement> resources) {
        return DynamicRecipeResources.addRecipes(resources, this.registries);
    }
}
