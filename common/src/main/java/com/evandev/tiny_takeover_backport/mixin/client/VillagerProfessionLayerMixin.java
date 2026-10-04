package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.client.ModBabyTextureRegistry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillagerProfessionLayer.class)
public abstract class VillagerProfessionLayerMixin {

    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/VillagerProfessionLayer;renderColoredCutoutModel(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;I)V",
                    ordinal = 0
            )
    )
    private void wrapTypeTextureRender(
            EntityModel<LivingEntity> model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, LivingEntity entity, int color,
            Operation<Void> original) {

        ResourceLocation finalTexture = entity.isBaby()
                ? ModBabyTextureRegistry.getBabyTexture(entity, texture)
                : texture;

        original.call(model, finalTexture, poseStack, buffer, packedLight, entity, color);
    }
}
