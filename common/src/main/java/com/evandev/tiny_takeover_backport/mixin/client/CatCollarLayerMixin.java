package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.Constants;
import com.evandev.tiny_takeover_backport.client.model.BabyCatModel;
import com.evandev.tiny_takeover_backport.client.ModModelLayers;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CatCollarLayer.class)
public abstract class CatCollarLayerMixin extends RenderLayer<Cat, CatModel<Cat>> {

    @Unique
    private CatModel<Cat> tiny_takeover_backport$babyModel;

    public CatCollarLayerMixin(RenderLayerParent<Cat, CatModel<Cat>> renderer) {
        super(renderer);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(RenderLayerParent<Cat, CatModel<Cat>> renderer, EntityModelSet modelSet, CallbackInfo ci) {
        this.tiny_takeover_backport$babyModel = new BabyCatModel(modelSet.bakeLayer(ModModelLayers.CAT_BABY_COLLAR));
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Cat;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/CatCollarLayer;coloredCutoutModelCopyLayerRender(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFFI)V"))
    private void wrapRenderCall(EntityModel<Cat> parentModel, EntityModel<Cat> model, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, int color, Operation<Void> original) {
        if (entity.isBaby() && ModConfig.get().isModelEnabled(entity)) {
            model = this.tiny_takeover_backport$babyModel;
            texture = Constants.vanillaLocation("textures/entity/cat/cat_collar_baby.png");
        }
        original.call(parentModel, model, texture, poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, color);
    }
}
