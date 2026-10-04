package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.Constants;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(HorseMarkingLayer.class)
public abstract class HorseMarkingLayerMixin extends RenderLayer<Horse, HorseModel<Horse>> {

    public HorseMarkingLayerMixin(RenderLayerParent<Horse, HorseModel<Horse>> renderer) {
        super(renderer);
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Horse;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object wrapMarkingLookup(Map<?, ?> map, Object key, Operation<Object> original, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Horse livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        Object result = original.call(map, key);
        if (result instanceof ResourceLocation location && livingEntity.isBaby() && ModConfig.get().isModelEnabled(livingEntity)) {
            String path = location.getPath();
            return Constants.vanillaLocation(path.substring(0, path.length() - 4) + "_baby.png");
        }
        return result;
    }
}
