package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.NotNull;

public class BabyZombieModel<T extends Zombie> extends ZombieModel<T> implements ModBabyArmorModel {

    public final ModelPart waist;
    public final ModelPart bodyBase;
    public final ModelPart rightLegBase;
    public final ModelPart leftLegBase;
    public final ModelPart rightFoot;
    public final ModelPart leftFoot;

    public BabyZombieModel(ModelPart root) {
        super(root);
        this.waist = root.hasChild("waist") ? root.getChild("waist") : null;
        this.bodyBase = this.body.hasChild("body_base") ? this.body.getChild("body_base") : null;
        this.rightLegBase = this.rightLeg.hasChild("right_leg_base") ? this.rightLeg.getChild("right_leg_base") : null;
        this.leftLegBase = this.leftLeg.hasChild("left_leg_base") ? this.leftLeg.getChild("left_leg_base") : null;
        this.rightFoot = this.leftLeg.hasChild("right_foot") ? this.leftLeg.getChild("right_foot") : null;
        this.leftFoot = this.rightLeg.hasChild("left_foot") ? this.rightLeg.getChild("left_foot") : null;
    }

    @Override
    public ModelPart getWaist() { return waist; }
    @Override
    public ModelPart getBodyBase() { return bodyBase; }
    @Override
    public ModelPart getRightLegBase() { return rightLegBase; }
    @Override
    public ModelPart getLeftLegBase() { return leftLegBase; }
    @Override
    public ModelPart getRightFoot() { return rightFoot; }
    @Override
    public ModelPart getLeftFoot() { return leftFoot; }

    @Override
    public void copyPropertiesTo(HumanoidModel<T> model) {
        super.copyPropertiesTo(model);
        model.body.y += 0.5F;
        model.head.y -= 0.25F;
        model.hat.y -= 0.25F;
        model.rightArm.x -= 0.5F;
        model.leftArm.x += 0.5F;
        model.rightLeg.x -= 0.5F;
        model.rightLeg.z += 0.5F;
        model.leftLeg.x += 0.5F;
        model.leftLeg.z += 0.5F;
    }

    public static LayerDefinition createArmorLayer(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, -7.0F, -4.5F, 9.0F, 8.0F, 8.0F, g),
                PartPose.offset(0.0F, 15.0F, 0.0F)
        );
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 18.0F, 0.0F)
        );
        body.addOrReplaceChild(
                "body_base",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-3.0F, -3.0F, -1.5F, 6.0F, 5.0F, 3.0F, g),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                "waist",
                CubeListBuilder.create()
                        .texOffs(0, 36)
                        .addBox(-3.0F, -1.2F, -1.49F, 5.9F, 2.0F, 2.9F, g.extend(-0.1F)),
                PartPose.offset(0.0F, 19.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "right_arm", CubeListBuilder.create().texOffs(30, 25).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g), PartPose.offset(-3.5F, 15.5F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm", CubeListBuilder.create().texOffs(30, 17).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g), PartPose.offset(3.5F, 15.5F, 0.0F)
        );

        PartDefinition rightLeg = root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create(),
                PartPose.offset(-1.5F, 20.0F, 0.5F)
        );
        rightLeg.addOrReplaceChild(
                "right_leg_base",
                CubeListBuilder.create()
                        .texOffs(18, 17)
                        .addBox(-1.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F)),
                PartPose.ZERO
        );
        rightLeg.addOrReplaceChild(
                "left_foot",
                CubeListBuilder.create()
                        .texOffs(0, 29)
                        .mirror()
                        .addBox(-1.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, g)
                        .mirror(false),
                PartPose.ZERO
        );

        PartDefinition leftLeg = root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create(),
                PartPose.offset(1.5F, 20.0F, 0.5F)
        );
        leftLeg.addOrReplaceChild(
                "left_leg_base",
                CubeListBuilder.create()
                        .texOffs(18, 24)
                        .addBox(-2.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F)),
                PartPose.ZERO
        );
        leftLeg.addOrReplaceChild(
                "right_foot",
                CubeListBuilder.create()
                        .texOffs(0, 25)
                        .addBox(-2.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, g),
                PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(CubeDeformation.NONE);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "body", CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, -2.5F, -1.0F, 4.0F, 5.0F, 2.0F, g), PartPose.offset(0.0F, 17.5F, 0.0F)
        );
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(3, 3)
                        .addBox(-3.0F, -6.25F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(35, 3)
                        .addBox(-3.0F, -6.15F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 15.25F, 0.0F)
        );

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild(
                "right_arm", CubeListBuilder.create().texOffs(36, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g), PartPose.offset(-3.0F, 15.5F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm", CubeListBuilder.create().texOffs(28, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g), PartPose.offset(3.0F, 15.5F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg", CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g), PartPose.offset(-1.0F, 20.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g), PartPose.offset(1.0F, 20.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.head.setPos(0.0F, 15.25F, 0.0F);
        this.hat.setPos(0.0F, 15.25F, 0.0F);
        this.body.setPos(0.0F, 17.5F, 0.0F);
        this.rightArm.setPos(-3.0F, 15.5F, 0.0F);
        this.leftArm.setPos(3.0F, 15.5F, 0.0F);
        this.rightLeg.setPos(-1.0F, 20.0F, 0.0F);
        this.leftLeg.setPos(1.0F, 20.0F, 0.0F);
    }
}
