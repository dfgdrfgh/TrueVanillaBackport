package com.evandev.tiny_takeover_backport.entity;

import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalSoundVariants {
    public static final String CLASSIC = "minecraft:classic";

    private static final Map<EntityType<?>, Map<String, Map<SoundEvent, SoundEvent>>> VARIANTS = new HashMap<>();

    static {
        register(EntityType.PIG, "minecraft:mini", Map.of(
                SoundEvents.PIG_AMBIENT, ModRegistry.PIG_MINI_AMBIENT,
                SoundEvents.PIG_HURT, ModRegistry.PIG_MINI_HURT,
                SoundEvents.PIG_DEATH, ModRegistry.PIG_MINI_DEATH,
                ModRegistry.PIG_EAT, ModRegistry.PIG_MINI_EAT
        ));
        register(EntityType.PIG, "minecraft:big", Map.of(
                SoundEvents.PIG_AMBIENT, ModRegistry.PIG_BIG_AMBIENT,
                SoundEvents.PIG_HURT, ModRegistry.PIG_BIG_HURT,
                SoundEvents.PIG_DEATH, ModRegistry.PIG_BIG_DEATH,
                ModRegistry.PIG_EAT, ModRegistry.PIG_BIG_EAT
        ));
        register(EntityType.COW, "minecraft:moody", Map.of(
                SoundEvents.COW_AMBIENT, ModRegistry.COW_MOODY_AMBIENT,
                SoundEvents.COW_HURT, ModRegistry.COW_MOODY_HURT,
                SoundEvents.COW_DEATH, ModRegistry.COW_MOODY_DEATH,
                SoundEvents.COW_STEP, ModRegistry.COW_MOODY_STEP
        ));
        VARIANTS.put(EntityType.MOOSHROOM, VARIANTS.get(EntityType.COW));
        register(EntityType.CHICKEN, "minecraft:picky", Map.of(
                SoundEvents.CHICKEN_AMBIENT, ModRegistry.CHICKEN_PICKY_AMBIENT,
                SoundEvents.CHICKEN_HURT, ModRegistry.CHICKEN_PICKY_HURT,
                SoundEvents.CHICKEN_DEATH, ModRegistry.CHICKEN_PICKY_DEATH
        ));
        register(EntityType.CAT, "minecraft:royal", Map.of(
                SoundEvents.CAT_AMBIENT, ModRegistry.CAT_ROYAL_AMBIENT,
                SoundEvents.CAT_STRAY_AMBIENT, ModRegistry.CAT_ROYAL_STRAY_AMBIENT,
                SoundEvents.CAT_HISS, ModRegistry.CAT_ROYAL_HISS,
                SoundEvents.CAT_HURT, ModRegistry.CAT_ROYAL_HURT,
                SoundEvents.CAT_DEATH, ModRegistry.CAT_ROYAL_DEATH,
                SoundEvents.CAT_EAT, ModRegistry.CAT_ROYAL_EAT,
                SoundEvents.CAT_BEG_FOR_FOOD, ModRegistry.CAT_ROYAL_BEG_FOR_FOOD,
                SoundEvents.CAT_PURR, ModRegistry.CAT_ROYAL_PURR,
                SoundEvents.CAT_PURREOW, ModRegistry.CAT_ROYAL_PURREOW
        ));
    }

    private static void register(EntityType<?> type, String variant, Map<SoundEvent, SoundEvent> sounds) {
        VARIANTS.computeIfAbsent(type, t -> new HashMap<>()).put(variant, sounds);
    }

    @Nullable
    public static String pickRandom(EntityType<?> type, RandomSource random) {
        Map<String, Map<SoundEvent, SoundEvent>> variants = VARIANTS.get(type);
        if (variants == null) return null;
        List<String> choices = new ArrayList<>();
        choices.add(CLASSIC);
        choices.addAll(variants.keySet());
        choices.sort(null);
        return choices.get(random.nextInt(choices.size()));
    }

    public static boolean hasVariants(EntityType<?> type) {
        return VARIANTS.containsKey(type);
    }

    public static SoundEvent remap(EntityType<?> type, @Nullable String variant, SoundEvent sound) {
        if (variant == null) return sound;
        Map<String, Map<SoundEvent, SoundEvent>> variants = VARIANTS.get(type);
        if (variants == null) return sound;
        Map<SoundEvent, SoundEvent> sounds = variants.get(variant);
        return sounds == null ? sound : sounds.getOrDefault(sound, sound);
    }
}
