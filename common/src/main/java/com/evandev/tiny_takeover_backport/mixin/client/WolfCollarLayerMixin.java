package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.Constants;
import com.evandev.tiny_takeover_backport.client.model.BabyWolfModel;
import com.evandev.tiny_takeover_backport.client.ModModelLayers;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfCollarLayer.class)
public abstract class WolfCollarLayerMixin extends RenderLayer<Wolf, WolfModel<Wolf>> {

    @Unique
    private WolfModel<Wolf> tiny_takeover_backport$babyModel;

    public WolfCollarLayerMixin(RenderLayerParent<Wolf, WolfModel<Wolf>> renderer) {
        super(renderer);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(RenderLayerParent<Wolf, WolfModel<Wolf>> renderer, CallbackInfo ci) {
        this.tiny_takeover_backport$babyModel = new BabyWolfModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(ModModelLayers.WOLF_BABY)
        );
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Wolf;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/WolfModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void wrapRenderCall(WolfModel<Wolf> instance, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int overlay, int color, Operation<Void> original, PoseStack methodPoseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int methodPackedLight, Wolf entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isBaby() && ModConfig.get().isModelEnabled(entity)) {
            instance.copyPropertiesTo(this.tiny_takeover_backport$babyModel);
            this.tiny_takeover_backport$babyModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            this.tiny_takeover_backport$babyModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            instance = this.tiny_takeover_backport$babyModel;
        }
        original.call(instance, poseStack, vertexConsumer, packedLight, overlay, color);
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Wolf;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType wrapCollarTexture(ResourceLocation texture, Operation<RenderType> original, PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, Wolf entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isBaby() && ModConfig.get().isModelEnabled(entity)) {
            texture = Constants.vanillaLocation("textures/entity/wolf/wolf_collar_baby.png");
        }
        return original.call(texture);
    }
}
