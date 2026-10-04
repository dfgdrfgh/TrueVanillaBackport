package com.evandev.tiny_takeover_backport.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.NotNull;

public class SmallArmorStandArmorModel extends ArmorStandArmorModel {
    public SmallArmorStandArmorModel(ModelPart root) {
        super(root);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        // Match a 0.5 uniform mesh transform around the model's ground plane.
        // AgeableListModel's infant head enlargement does not apply to stands.
        boolean young = this.young;
        this.young = false;
        poseStack.pushPose();
        try {
            poseStack.translate(0.0F, 0.75F, 0.0F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        } finally {
            poseStack.popPose();
            this.young = young;
        }
    }
}
