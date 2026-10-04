package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void tiny_takeover_backport$modifyDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.isBaby() && pose != Pose.SLEEPING) {
            if (ModConfig.get().isModelEnabled(entity)) {
                EntityDimensions custom = null;

                if (entity instanceof Squid) {
                    custom = EntityDimensions.scalable(0.5F, 0.63F);
                } else if (entity instanceof Dolphin) {
                    custom = EntityType.DOLPHIN.getDimensions().scale(0.65F);
                } else if (entity instanceof Chicken) {
                    custom = EntityDimensions.scalable(0.3F, 0.4F);
                } else if (entity instanceof Fox) {
                    custom = EntityType.FOX.getDimensions().scale(0.6F);
                } else if (entity instanceof ZombieVillager) {
                    custom = tiny_takeover_backport$babyHumanoid(0.125F);
                } else if (entity instanceof Zombie || entity instanceof ZombifiedPiglin || entity instanceof Piglin) {
                    custom = tiny_takeover_backport$babyHumanoid(0.1875F);
                } else if (entity instanceof Villager) {
                    custom = EntityDimensions.scalable(0.49F, 0.99F);
                } else if (entity instanceof Axolotl) {
                    custom = EntityDimensions.scalable(0.5F, 0.25F);
                } else if (entity instanceof Horse) {
                    custom = tiny_takeover_backport$babyHorse(EntityType.HORSE, -0.125F);
                } else if (entity instanceof SkeletonHorse) {
                    custom = tiny_takeover_backport$babyHorse(EntityType.SKELETON_HORSE, -0.25F);
                } else if (entity instanceof ZombieHorse) {
                    custom = tiny_takeover_backport$babyHorse(EntityType.ZOMBIE_HORSE, -0.25F);
                }

                if (custom != null) {
                    Float eyeHeight = tiny_takeover_backport$getBabyEyeHeight(entity);
                    if (eyeHeight != null) {
                        custom = custom.withEyeHeight(eyeHeight);
                    }
                    cir.setReturnValue(custom.scale(entity.getScale()));
                }
            }
        }
    }

    @Inject(method = "getAgeScale", at = @At("RETURN"), cancellable = true)
    private void tiny_takeover_backport$modifyAgeScale(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.isBaby()) {
            if (ModConfig.get().isModelEnabled(entity)) {
                if (entity instanceof Dolphin) {
                    cir.setReturnValue(0.65F);
                } else if (entity instanceof Goat) {
                    cir.setReturnValue(0.55F);
                } else if (entity instanceof Horse || entity instanceof SkeletonHorse || entity instanceof ZombieHorse) {
                    cir.setReturnValue(0.7F);
                }
            }
        }
    }

    @Unique
    private static EntityDimensions tiny_takeover_backport$babyHumanoid(float vehicleAttachment) {
        EntityDimensions dimensions = EntityDimensions.scalable(0.49F, 0.99F);
        dimensions = dimensions.withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, 0.0F, vehicleAttachment, 0.0F));
        return dimensions;
    }

    @Unique
    private static EntityDimensions tiny_takeover_backport$babyHorse(EntityType<?> type, float passengerOffset) {
        EntityDimensions dimensions = type.getDimensions();
        dimensions = dimensions.withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, type.getHeight() + passengerOffset, 0.0F));
        return dimensions.scale(0.7F);
    }

    @Unique
    private static Float tiny_takeover_backport$getBabyEyeHeight(LivingEntity entity) {
        if (entity instanceof Squid) return 0.37F;
        if (entity instanceof Chicken) return 0.28F;
        if (entity instanceof Fox) return 0.2975F;
        if (entity instanceof ZombieVillager) return 0.67F;
        if (entity instanceof Husk) return 0.825F;
        if (entity instanceof Drowned) return 0.775F;
        if (entity instanceof Zombie) return 0.775F;
        if (entity instanceof ZombifiedPiglin) return 0.78F;
        if (entity instanceof Piglin) return 0.78F;
        if (entity instanceof Villager) return 0.63F;
        if (entity instanceof Axolotl) return 0.2F;
        return null;
    }
}
