package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.Constants;
import com.evandev.tiny_takeover_backport.client.ModModelLayers;
import com.evandev.tiny_takeover_backport.client.ModRenderHelper;
import com.evandev.tiny_takeover_backport.client.model.BabySheepModel;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SheepFurLayer.class)
public abstract class SheepWoolLayerMixin extends RenderLayer<Sheep, SheepModel<Sheep>> {

    @Unique
    private SheepModel<Sheep> tiny_takeover_backport$babyModel;

    public SheepWoolLayerMixin(RenderLayerParent<Sheep, SheepModel<Sheep>> renderer) {
        super(renderer);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(RenderLayerParent<Sheep, SheepModel<Sheep>> renderer, EntityModelSet modelSet, CallbackInfo ci) {
        this.tiny_takeover_backport$babyModel = new BabySheepModel(modelSet.bakeLayer(ModModelLayers.SHEEP_BABY_WOOL));
        ModRenderHelper.babyWoolModel = this.tiny_takeover_backport$babyModel;
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Sheep;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/SheepFurLayer;coloredCutoutModelCopyLayerRender(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFFI)V"))
    private void redirectRenderCall(EntityModel<Sheep> parentModel, EntityModel<Sheep> model, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, int color, Operation<Void> original) {
        if (entity.isBaby() && ModConfig.get().isModelEnabled(entity)) {
            model = this.tiny_takeover_backport$babyModel;
            texture = Constants.vanillaLocation("textures/entity/sheep/sheep_wool_baby.png");
        }
        original.call(parentModel, model, texture, poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, color);
    }
}
