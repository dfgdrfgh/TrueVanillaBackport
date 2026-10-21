package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.client.ModRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.evandev.tiny_takeover_backport.client.model.ModBabyArmorModel;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    @ModifyVariable(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private A swapBabyArmorModel(
            A model,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            T livingEntity,
            EquipmentSlot slot,
            int packedLight
    ) {
        EntityModel<?> parentModel = ((HumanoidArmorLayer<T, M, A>) (Object) this).getParentModel();
        return ModRenderHelper.getBabyArmorModel(model, parentModel, slot);
    }

    @Inject(
            method = "setPartVisibility(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("TAIL")
    )
    private void adjustBabyArmorPartVisibility(A model, EquipmentSlot slot, CallbackInfo ci) {
        ModRenderHelper.adjustBabyArmorVisibility(model, slot);
    }

    @ModifyVariable(
            method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/HumanoidModel;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ResourceLocation redirectBabyArmorTexture(
            ResourceLocation textureLocation,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            HumanoidModel<?> model,
            int color
    ) {
        if (model instanceof ModBabyArmorModel) {
            String path = textureLocation.getPath();
            if (path.startsWith("textures/models/armor/")) {
                String materialAndSuffix = path.substring("textures/models/armor/".length());
                String newPath = "textures/models/armor/baby/" + materialAndSuffix.replace("_layer_1", "").replace("_layer_2", "");
                return ResourceLocation.fromNamespaceAndPath(textureLocation.getNamespace(), newPath);
            }
        }
        return textureLocation;
    }
}
