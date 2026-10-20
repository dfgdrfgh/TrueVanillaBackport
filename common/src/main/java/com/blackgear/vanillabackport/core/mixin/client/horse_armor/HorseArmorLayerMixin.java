package com.blackgear.vanillabackport.core.mixin.client.horse_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseArmorLayer.class)
public abstract class HorseArmorLayerMixin {
    private static final ResourceLocation LEATHER_OVERLAY =
        ResourceLocation.withDefaultNamespace("textures/entity/horse/armor/horse_armor_leather_overlay.png");

    @Shadow @Final private HorseModel<Horse> model;

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Horse;FFFFFF)V",
        at = @At("TAIL")
    )
    private void vanillabackport$renderLeatherHorseArmorOverlay(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        Horse horse,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        CallbackInfo ci
    ) {
        if (!horse.getBodyArmorItem().is(Items.LEATHER_HORSE_ARMOR)) return;

        // The 1.21.11 base is dyed by vanilla; its colored details render untinted.
        VertexConsumer overlay = buffer.getBuffer(RenderType.entityCutoutNoCull(LEATHER_OVERLAY));
        this.model.renderToBuffer(poseStack, overlay, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }
}
