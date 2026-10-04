package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.Constants;
import com.evandev.tiny_takeover_backport.client.ModModelLayers;
import com.evandev.tiny_takeover_backport.client.model.BabyDrownedModel;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrownedOuterLayer.class)
public abstract class DrownedOuterLayerMixin<T extends Drowned> extends RenderLayer<T, DrownedModel<T>> {

    @Unique
    private DrownedModel<T> tiny_takeover_backport$babyModel;

    public DrownedOuterLayerMixin(RenderLayerParent<T, DrownedModel<T>> renderer) {
        super(renderer);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(RenderLayerParent<T, DrownedModel<T>> renderer, EntityModelSet modelSet, CallbackInfo ci) {
        this.tiny_takeover_backport$babyModel = new BabyDrownedModel(modelSet.bakeLayer(ModModelLayers.DROWNED_BABY_OUTER_LAYER));
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/monster/Drowned;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/DrownedOuterLayer;coloredCutoutModelCopyLayerRender(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFFI)V"))
    private void wrapRenderCall(EntityModel<T> parentModel, EntityModel<T> model, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, int color, Operation<Void> original) {
        if (entity.isBaby() && ModConfig.get().isModelEnabled(entity)) {
            model = this.tiny_takeover_backport$babyModel;
            texture = Constants.vanillaLocation("textures/entity/zombie/drowned_outer_layer_baby.png");
        }
        original.call(parentModel, model, texture, poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, color);
    }
}
