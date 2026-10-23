package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class BabyPiglinModel extends PiglinModel implements ModBabyArmorModel {

    public final ModelPart waist;
    public final ModelPart bodyBase;
    public final ModelPart rightLegBase;
    public final ModelPart leftLegBase;
    public final ModelPart rightFoot;
    public final ModelPart leftFoot;

    public BabyPiglinModel(ModelPart root) {
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
    @SuppressWarnings("unchecked")
    public void copyPropertiesTo(HumanoidModel model) {
        super.copyPropertiesTo(model);
        model.body.z += 0.5F;
        model.rightLeg.z += 0.5F;
        model.leftLeg.z += 0.5F;
    }

    public static LayerDefinition createArmorLayer(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -7.0F, -4.5F, 9.0F, 8.0F, 8.0F, g),
                PartPose.offset(0.0F, 15.0F, 0.0F)
        );

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("ear", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.ZERO);

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
                "left_arm", CubeListBuilder.create().texOffs(30, 17).mirror().addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g), PartPose.offset(4.0F, 15.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_arm", CubeListBuilder.create().texOffs(30, 25).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g), PartPose.offset(-4.0F, 15.0F, 0.0F)
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
                "body", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 5.0F, 3.0F, g), PartPose.offset(0.0F, 18.0F, -0.5F)
        );

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(21, 30).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 3.0F, 1.0F, g).texOffs(0, 0).addBox(-4.5F, -6.0F, -3.5F, 9.0F, 6.0F, 7.0F, g),
                PartPose.offset(0.0F, 15.0F, 0.0F)
        );

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("ear", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition leftear = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(4.2F, -4.0F, 0.0F));
        leftear.addOrReplaceChild(
                "left_ear_r1",
                CubeListBuilder.create().texOffs(0, 21).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F, g),
                PartPose.offsetAndRotation(1.0F, 1.75F, 0.0F, 0.0F, 0.0F, -0.6109F)
        );
        PartDefinition rightear = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.2F, -4.0F, 0.0F));
        rightear.addOrReplaceChild(
                "right_ear_r1",
                CubeListBuilder.create().texOffs(18, 13).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F, g),
                PartPose.offsetAndRotation(-1.0F, 1.75F, 0.0F, 0.0F, 0.0F, 0.6109F)
        );
        root.addOrReplaceChild(
                "left_arm", CubeListBuilder.create().texOffs(28, 13).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F, g), PartPose.offset(4.0F, 15.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_arm", CubeListBuilder.create().texOffs(10, 30).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F, g), PartPose.offset(-4.0F, 15.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg", CubeListBuilder.create().texOffs(22, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F)), PartPose.offset(-1.5F, 20.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg", CubeListBuilder.create().texOffs(10, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F)), PartPose.offset(1.5F, 20.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull Mob entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.head.setPos(0.0F, 15.0F, 0.0F);
        this.hat.setPos(0.0F, 15.0F, 0.0F);
        this.body.setPos(0.0F, 18.0F, -0.5F);
        this.rightArm.setPos(-4.0F, 15.0F, 0.0F);
        this.leftArm.setPos(4.0F, 15.0F, 0.0F);
        this.rightLeg.setPos(-1.5F, 20.0F, 0.0F);
        this.leftLeg.setPos(1.5F, 20.0F, 0.0F);
    }
}
