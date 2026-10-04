package com.evandev.tiny_takeover_backport.config;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public class NameTagRecipeEnabledCondition implements ICondition {
    public static final NameTagRecipeEnabledCondition INSTANCE = new NameTagRecipeEnabledCondition();
    public static final MapCodec<NameTagRecipeEnabledCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(@NotNull IContext context) {
        return ModConfig.get().enableNameTagRecipe;
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
