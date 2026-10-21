package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CatRenderer.class)
public abstract class CatRendererMixin {
    @Inject(method = "scale(Lnet/minecraft/world/entity/animal/Cat;Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$babyCatScale(Cat cat, PoseStack poses, float partialTicks, CallbackInfo ci) {
        if (cat.isBaby() && ModConfig.get().isModelEnabled(cat)) ci.cancel();
    }
}
