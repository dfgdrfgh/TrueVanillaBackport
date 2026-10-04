package com.evandev.tiny_takeover_backport.registry;

import com.evandev.tiny_takeover_backport.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityType;

public class ModTags {
    public static final TagKey<EntityType<?>> CANNOT_BE_AGE_LOCKED = TagKey.create(Registries.ENTITY_TYPE, Constants.vanillaLocation("cannot_be_age_locked"));
    public static final TagKey<Block> PLAYS_TRUMPET = block("plays_trumpet");
    public static final TagKey<Block> PLAYS_TRUMPET_EXPOSED = block("plays_trumpet_exposed");
    public static final TagKey<Block> PLAYS_TRUMPET_WEATHERED = block("plays_trumpet_weathered");
    public static final TagKey<Block> PLAYS_TRUMPET_OXIDIZED = block("plays_trumpet_oxidized");

    private static TagKey<Block> block(String name) {
        return TagKey.create(Registries.BLOCK, Constants.location(Constants.MOD_ID, name));
    }
}
