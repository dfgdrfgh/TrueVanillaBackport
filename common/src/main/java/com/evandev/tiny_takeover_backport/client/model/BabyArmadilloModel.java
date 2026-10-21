package com.evandev.tiny_takeover_backport.client.model;

import com.evandev.tiny_takeover_backport.client.animation.BabyArmadilloAnimation;
import net.minecraft.client.model.ArmadilloModel;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class BabyArmadilloModel extends ArmadilloModel {
    private final ModelPart body;
    private final ModelPart head;

    public BabyArmadilloModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, -2.0F, -3.5F, 5.0F, 4.0F, 7.0F, new CubeDeformation(0.3F))
                        .texOffs(0, 11)
                        .addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 20.0F, 0.5F)
        );
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 3.4F));
        tail.addOrReplaceChild(
                "right_ear_cube",
                CubeListBuilder.create().texOffs(22, 11).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 1.5F, 1.0F, -1.0472F, 0.0F, 0.0F)
        );
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.2F));
        PartDefinition headGroup = head.addOrReplaceChild(
                "head_cube",
                CubeListBuilder.create().texOffs(20, 17).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7417649F, 0.0F, 0.0F)
        );
        headGroup.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create().texOffs(28, 8).mirror().addBox(-1.8F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F).mirror(false),
                PartPose.offsetAndRotation(-1.0F, -2.0F, -0.3F, -0.4363F, -0.1134F, 0.0524F)
        );
        headGroup.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create().texOffs(28, 8).addBox(-0.2F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F),
                PartPose.offsetAndRotation(1.0F, -2.0F, -0.3F, -0.4363F, 0.1134F, -0.0524F)
        );
        root.addOrReplaceChild(
                "right_hind_leg",
                CubeListBuilder.create().texOffs(20, 27).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false),
                PartPose.offset(-1.5F, 22.0F, 2.5F)
        );
        root.addOrReplaceChild(
                "left_hind_leg", CubeListBuilder.create().texOffs(20, 27).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.5F, 22.0F, 2.5F)
        );
        root.addOrReplaceChild(
                "right_front_leg", CubeListBuilder.create().texOffs(20, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.5F, 22.0F, -1.5F)
        );
        root.addOrReplaceChild(
                "left_front_leg",
                CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false),
                PartPose.offset(-1.5F, 22.0F, -1.5F)
        );
        root.addOrReplaceChild(
                "cube",
                CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, 20.7F, 0.5F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull Armadillo entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (!entity.shouldHideInShell()) {
            this.head.xRot = Mth.clamp(headPitch, -22.5F, 25.0F) * (float) (Math.PI / 180.0);
            this.head.yRot = Mth.clamp(netHeadYaw, -32.5F, 32.5F) * (float) (Math.PI / 180.0);
        }

        this.animateWalk(BabyArmadilloAnimation.ARMADILLO_BABY_WALK, limbSwing, limbSwingAmount, 16.5F, 2.5F);
        this.animate(entity.rollOutAnimationState, BabyArmadilloAnimation.ARMADILLO_BABY_ROLL_OUT, ageInTicks, 1.0F);
        this.animate(entity.rollUpAnimationState, BabyArmadilloAnimation.ARMADILLO_BABY_ROLL_UP, ageInTicks, 1.0F);
        this.animate(entity.peekAnimationState, BabyArmadilloAnimation.ARMADILLO_BABY_PEEK, ageInTicks, 1.0F);
    }
}
