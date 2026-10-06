package com.blackgear.vanillabackport.common.level.recipes;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Generate recipes and their unlock advancements together on every data reload. */
public final class DynamicRecipeResources {
    private DynamicRecipeResources() {}

    public static Map<ResourceLocation, JsonElement> addRecipes(Map<ResourceLocation, JsonElement> resources, HolderLookup.Provider registries) {
        return generate(resources, registries, false);
    }

    public static Map<ResourceLocation, JsonElement> addAdvancements(Map<ResourceLocation, JsonElement> resources, HolderLookup.Provider registries) {
        return generate(resources, registries, true);
    }

    private static Map<ResourceLocation, JsonElement> generate(Map<ResourceLocation, JsonElement> resources, HolderLookup.Provider registries, boolean advancements) {
        Map<ResourceLocation, JsonElement> result = new HashMap<>(resources);
        Set<ResourceLocation> generated = new HashSet<>();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        new VanillaDynamicRecipes().buildRecipes(new RecipeOutput() {
            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement) {
                if (!generated.add(id)) {
                    throw new IllegalStateException("Duplicate dynamic recipe " + id);
                }
                if (advancements) {
                    if (advancement != null) {
                        result.put(advancement.id(), Advancement.CODEC.encodeStart(ops, advancement.value()).getOrThrow());
                    }
                } else {
                    result.put(id, Recipe.CODEC.encodeStart(ops, recipe).getOrThrow());
                }
            }

            @Override
            @SuppressWarnings("removal")
            public Advancement.Builder advancement() {
                return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
            }
        }, registries, ResourceLocation::withDefaultNamespace);
        return result;
    }
}
