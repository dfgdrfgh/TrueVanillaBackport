package com.evandev.tiny_takeover_backport.client.model;

import com.evandev.tiny_takeover_backport.client.animation.CamelBabyAnimation;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.camel.Camel;
import org.jetbrains.annotations.NotNull;

public class BabyCamelModel extends CamelModel {
    private final ModelPart body;
    private final ModelPart head;

    public BabyCamelModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild(
                "body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -4.0F, -8.0F, 9.0F, 8.0F, 16.0F), PartPose.offset(0.0F, 7.0F, 0.0F)
        );

        body.addOrReplaceChild("saddle", CubeListBuilder.create(), PartPose.ZERO);

        body.addOrReplaceChild(
                "tail", CubeListBuilder.create().texOffs(50, 38).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 9.0F, 0.0F), PartPose.offset(0.0F, -1.5F, 8.05F)
        );

        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(20, 0)
                        .addBox(-2.5F, -3.0F, -7.5F, 5.0F, 5.0F, 7.0F)
                        .texOffs(0, 0)
                        .addBox(-2.5F, -12.0F, -7.5F, 5.0F, 9.0F, 5.0F)
                        .texOffs(0, 14)
                        .addBox(-2.5F, -12.0F, -10.5F, 5.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, 1.0F, -7.5F)
        );

        head.addOrReplaceChild("bridle", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("reins", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild(
                "right_ear", CubeListBuilder.create().texOffs(37, 0).addBox(-3.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(-2.5F, -11.0F, -4.0F)
        );
        head.addOrReplaceChild(
                "left_ear", CubeListBuilder.create().texOffs(47, 0).addBox(0.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(2.5F, -11.0F, -4.0F)
        );

        root.addOrReplaceChild(
                "right_front_leg", CubeListBuilder.create().texOffs(36, 14).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(-3.0F, 11.5F, -5.5F)
        );
        root.addOrReplaceChild(
                "left_front_leg", CubeListBuilder.create().texOffs(48, 14).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(3.0F, 11.5F, -5.5F)
        );
        root.addOrReplaceChild(
                "left_hind_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(3.0F, 11.5F, 5.5F)
        );
        root.addOrReplaceChild(
                "right_hind_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(-3.0F, 11.5F, 5.5F)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull Camel entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.root().getAllParts().forEach(ModelPart::resetPose);

        float clampedHeadYaw = Mth.clamp(netHeadYaw, -30.0F, 30.0F);
        float clampedHeadPitch = Mth.clamp(headPitch, -25.0F, 45.0F);
        if (entity.getJumpCooldown() > 0) {
            float f = ageInTicks - (float) entity.tickCount;
            float f1 = 45.0F * ((float) entity.getJumpCooldown() - f) / 55.0F;
            clampedHeadPitch = Mth.clamp(clampedHeadPitch + f1, -25.0F, 70.0F);
        }

        this.head.yRot = clampedHeadYaw * (float) (Math.PI / 180.0);
        this.head.xRot = clampedHeadPitch * (float) (Math.PI / 180.0);

        this.animateWalk(CamelBabyAnimation.CAMEL_BABY_WALK, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        this.animate(entity.sitAnimationState, CamelBabyAnimation.CAMEL_BABY_SIT, ageInTicks, 1.0F);
        this.animate(entity.sitPoseAnimationState, CamelBabyAnimation.CAMEL_BABY_SIT_POSE, ageInTicks, 1.0F);
        this.animate(entity.sitUpAnimationState, CamelBabyAnimation.CAMEL_BABY_STANDUP, ageInTicks, 1.0F);
        this.animate(entity.idleAnimationState, CamelBabyAnimation.CAMEL_BABY_IDLE, ageInTicks, 1.0F);
        this.animate(entity.dashAnimationState, CamelBabyAnimation.CAMEL_BABY_DASH, ageInTicks, 1.0F);
    }
}
