package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.evandev.tiny_takeover_backport.entity.AnimalSoundVariants;
import com.evandev.tiny_takeover_backport.entity.SoundVariantHolder;
import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyVariable(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At("HEAD"), argsOnly = true)
    private SoundEvent tiny_takeover_backport$modifyPlaySound(SoundEvent soundEvent) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof AgeableMob ageable && ageable.isBaby()) {
            if (soundEvent == SoundEvents.CHICKEN_AMBIENT) return ModRegistry.CHICKEN_AMBIENT_BABY;
            if (soundEvent == SoundEvents.CHICKEN_HURT) return ModRegistry.CHICKEN_HURT_BABY;
            if (soundEvent == SoundEvents.CHICKEN_DEATH) return ModRegistry.CHICKEN_DEATH_BABY;
            if (soundEvent == SoundEvents.CHICKEN_STEP) return ModRegistry.CHICKEN_STEP_BABY;

            if (soundEvent == SoundEvents.PIG_AMBIENT) return ModRegistry.PIG_AMBIENT_BABY;
            if (soundEvent == SoundEvents.PIG_HURT) return ModRegistry.PIG_HURT_BABY;
            if (soundEvent == SoundEvents.PIG_DEATH) return ModRegistry.PIG_DEATH_BABY;
            if (soundEvent == SoundEvents.PIG_STEP) return ModRegistry.PIG_STEP_BABY;
            if (soundEvent == ModRegistry.PIG_EAT) return ModRegistry.PIG_EAT_BABY;

            if (soundEvent == SoundEvents.CAT_AMBIENT) return ModRegistry.CAT_AMBIENT_BABY;
            if (soundEvent == SoundEvents.CAT_STRAY_AMBIENT) return ModRegistry.CAT_STRAY_AMBIENT_BABY;
            if (soundEvent == SoundEvents.CAT_DEATH) return ModRegistry.CAT_DEATH_BABY;
            if (soundEvent == SoundEvents.CAT_HURT) return ModRegistry.CAT_HURT_BABY;
            if (soundEvent == SoundEvents.CAT_EAT) return ModRegistry.CAT_EAT_BABY;
            if (soundEvent == SoundEvents.CAT_HISS) return ModRegistry.CAT_HISS_BABY;
            if (soundEvent == SoundEvents.CAT_BEG_FOR_FOOD) return ModRegistry.CAT_BEG_FOR_FOOD_BABY;
            if (soundEvent == SoundEvents.CAT_PURR) return ModRegistry.CAT_PURR_BABY;
            if (soundEvent == SoundEvents.CAT_PURREOW) return ModRegistry.CAT_PURREOW_BABY;

            if (soundEvent == SoundEvents.HORSE_AMBIENT) return ModRegistry.HORSE_AMBIENT_BABY;
            if (soundEvent == SoundEvents.HORSE_ANGRY) return ModRegistry.HORSE_ANGRY_BABY;
            if (soundEvent == SoundEvents.HORSE_BREATHE) return ModRegistry.HORSE_BREATHE_BABY;
            if (soundEvent == SoundEvents.HORSE_DEATH) return ModRegistry.HORSE_DEATH_BABY;
            if (soundEvent == SoundEvents.HORSE_EAT) return ModRegistry.HORSE_EAT_BABY;
            if (soundEvent == SoundEvents.HORSE_HURT) return ModRegistry.HORSE_HURT_BABY;
            if (soundEvent == SoundEvents.HORSE_LAND) return ModRegistry.HORSE_LAND_BABY;
            if (soundEvent == SoundEvents.HORSE_STEP) return ModRegistry.HORSE_STEP_BABY;

            if (soundEvent == SoundEvents.POLAR_BEAR_AMBIENT_BABY) return ModRegistry.POLAR_BEAR_AMBIENT_BABY;
            if (soundEvent == SoundEvents.TURTLE_DEATH_BABY) return ModRegistry.TURTLE_DEATH_BABY;
            if (soundEvent == SoundEvents.TURTLE_HURT_BABY) return ModRegistry.TURTLE_HURT_BABY;
            if (soundEvent == SoundEvents.TURTLE_SHAMBLE_BABY) return ModRegistry.TURTLE_SHAMBLE_BABY;
            if (soundEvent == SoundEvents.POLAR_BEAR_AMBIENT) return ModRegistry.POLAR_BEAR_AMBIENT_BABY;

            if (soundEvent == SoundEvents.TURTLE_DEATH) return ModRegistry.TURTLE_DEATH_BABY;
            if (soundEvent == SoundEvents.TURTLE_HURT) return ModRegistry.TURTLE_HURT_BABY;
            if (soundEvent == SoundEvents.TURTLE_SHAMBLE) return ModRegistry.TURTLE_SHAMBLE_BABY;

            if (soundEvent == SoundEvents.WOLF_AMBIENT) return ModRegistry.WOLF_AMBIENT_BABY;
            if (soundEvent == SoundEvents.WOLF_DEATH) return ModRegistry.WOLF_DEATH_BABY;
            if (soundEvent == SoundEvents.WOLF_GROWL) return ModRegistry.WOLF_GROWL_BABY;
            if (soundEvent == SoundEvents.WOLF_HURT) return ModRegistry.WOLF_HURT_BABY;
            if (soundEvent == SoundEvents.WOLF_PANT) return ModRegistry.WOLF_PANT_BABY;
            if (soundEvent == SoundEvents.WOLF_STEP) return ModRegistry.WOLF_STEP_BABY;
            if (soundEvent == SoundEvents.WOLF_WHINE) return ModRegistry.WOLF_WHINE_BABY;
        }
        if (entity instanceof SoundVariantHolder holder && ModConfig.get().enableAnimalSoundVariants) {
            return AnimalSoundVariants.remap(entity.getType(), holder.tiny_takeover_backport$getSoundVariant(), soundEvent);
        }
        return soundEvent;
    }
}
