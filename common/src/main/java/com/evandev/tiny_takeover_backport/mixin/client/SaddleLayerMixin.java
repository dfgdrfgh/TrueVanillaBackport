package com.evandev.tiny_takeover_backport.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.camel.Camel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaddleLayer.class)
public abstract class SaddleLayerMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$hideBabySaddles(PoseStack poses, MultiBufferSource buffers, int light, Entity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Pig pig && pig.isBaby() || entity instanceof Camel camel && camel.isBaby()) {
            ci.cancel();
        }
    }
}
