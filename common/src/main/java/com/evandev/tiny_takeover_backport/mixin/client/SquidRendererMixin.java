package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SquidRenderer.class)
public abstract class SquidRendererMixin {

    @Inject(method = "setupRotations(Lnet/minecraft/world/entity/animal/Squid;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$setupRotations(Squid entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
        if (entity.isBaby() && ModConfig.get().isModelEnabled(entity)) {
            float f = Mth.lerp(partialTick, entity.xBodyRotO, entity.xBodyRot);
            float f1 = Mth.lerp(partialTick, entity.zBodyRotO, entity.zBodyRot);
            poseStack.translate(0.0F, 0.25F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yBodyRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(f));
            poseStack.mulPose(Axis.YP.rotationDegrees(f1));
            poseStack.translate(0.0F, -0.6F, 0.0F);
            ci.cancel();
        }
    }
}
