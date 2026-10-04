package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.client.model.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> {

    @Final
    @Shadow
    private ItemInHandRenderer itemInHandRenderer;

    @SuppressWarnings("unchecked")
    @Inject(
            method = "renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderCustomBabyItem(
            LivingEntity livingEntity,
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        ItemInHandLayer<T, M> self = (ItemInHandLayer<T, M>) (Object) this;
        EntityModel<?> parentModel = self.getParentModel();
        boolean isBabyCustomModel = livingEntity.isBaby() && (
                parentModel instanceof BabyZombieModel ||
                parentModel instanceof BabyPiglinModel ||
                parentModel instanceof BabyZombifiedPiglinModel ||
                parentModel instanceof BabyDrownedModel ||
                parentModel instanceof BabyZombieVillagerModel
        );

        if (isBabyCustomModel && !itemStack.isEmpty()) {
            poseStack.pushPose();
            ((ArmedModel) parentModel).translateToHand(arm, poseStack);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.translate(0.0F, 0.125F, -0.5625F);
            this.itemInHandRenderer.renderItem(livingEntity, itemStack, displayContext, arm == HumanoidArm.LEFT, poseStack, buffer, packedLight);
            poseStack.popPose();
            ci.cancel();
        }
    }
}
