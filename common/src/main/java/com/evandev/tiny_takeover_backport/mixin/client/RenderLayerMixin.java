package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.client.ModRenderHelper;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin {

    @Inject(
            method = "coloredCutoutModelCopyLayerRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tiny_takeover_backport$cancelUndercoatForBaby(
            EntityModel<?> modelParent,
            EntityModel<?> model,
            ResourceLocation textureLocation,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            float partialTick,
            int color,
            CallbackInfo ci
    ) {
        if (entity instanceof Sheep && entity.isBaby() && ModConfig.get().isModelEnabled(entity) && textureLocation.getPath().contains("sheep_wool_undercoat")) {
            ci.cancel();
        }
    }

    @SuppressWarnings("rawtypes")
    @ModifyVariable(
            method = "coloredCutoutModelCopyLayerRender",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1
    )
    private static EntityModel tiny_takeover_backport$redirectWoolModel(
            EntityModel model,
            EntityModel modelParent,
            EntityModel modelOld,
            ResourceLocation textureLocation,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity
    ) {
        if (entity instanceof Sheep && entity.isBaby() && ModConfig.get().isModelEnabled(entity)) {
            return ModRenderHelper.babyWoolModel != null ? ModRenderHelper.babyWoolModel : modelParent;
        }
        return model;
    }
}
