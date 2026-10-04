package com.evandev.tiny_takeover_backport.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.client.renderer.entity.layers.WolfArmorLayer")
public abstract class WolfArmorLayerMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Wolf;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$hideBabyArmor(PoseStack poses, MultiBufferSource buffers, int light, Wolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch, CallbackInfo ci) {
        if (wolf.isBaby()) ci.cancel();
    }
}
