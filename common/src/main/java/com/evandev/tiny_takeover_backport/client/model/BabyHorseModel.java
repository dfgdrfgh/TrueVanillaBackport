package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.NotNull;

public class BabyHorseModel<T extends AbstractHorse> extends HorseModel<T> {
    private final ModelPart tail;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindBabyLeg;
    private final ModelPart leftHindBabyLeg;
    private final ModelPart rightFrontBabyLeg;
    private final ModelPart leftFrontBabyLeg;

    public BabyHorseModel(ModelPart root) {
        super(root);
        this.tail = this.body.getChild("tail");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.rightHindBabyLeg = root.getChild("right_hind_baby_leg");
        this.leftHindBabyLeg = root.getChild("left_hind_baby_leg");
        this.rightFrontBabyLeg = root.getChild("right_front_baby_leg");
        this.leftFrontBabyLeg = root.getChild("left_front_baby_leg");
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(createBabyMesh(CubeDeformation.NONE), 64, 64);
    }

    public static MeshDefinition createBabyMesh(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("right_hind_baby_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_hind_baby_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_front_baby_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_front_baby_leg", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition Body = root.addOrReplaceChild(
                "body", CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, -3.5F, -7.0F, 8.0F, 7.0F, 14.0F, g), PartPose.offset(0.0F, 12.5F, 0.0F)
        );
        Body.addOrReplaceChild(
                "tail",
                CubeListBuilder.create().texOffs(24, 34).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 8.0F, g),
                PartPose.offsetAndRotation(0.0F, -1.0F, 7.0F, -0.7418F, 0.0F, 0.0F)
        );

        Body.addOrReplaceChild("saddle", CubeListBuilder.create(), PartPose.ZERO);
        Body.addOrReplaceChild("left_chest", CubeListBuilder.create(), PartPose.ZERO);
        Body.addOrReplaceChild("right_chest", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild(
                "left_hind_leg", CubeListBuilder.create().texOffs(12, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(2.4F, 16.0F, 5.4F)
        );
        root.addOrReplaceChild(
                "right_hind_leg", CubeListBuilder.create().texOffs(0, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(-2.4F, 16.0F, 5.4F)
        );
        root.addOrReplaceChild(
                "left_front_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(2.4F, 16.0F, -5.4F)
        );
        root.addOrReplaceChild(
                "right_front_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(-2.4F, 16.0F, -5.4F)
        );

        PartDefinition neck = root.addOrReplaceChild(
                "head_parts",
                CubeListBuilder.create().texOffs(30, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 8.0F, 4.0F, g),
                PartPose.offsetAndRotation(0.0F, 10.0F, -6.0F, 0.6109F, 0.0F, 0.0F)
        );

        neck.addOrReplaceChild("head_saddle", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("left_saddle_mouth", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("right_saddle_mouth", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("left_saddle_line", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("right_saddle_line", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("mouth_saddle_wrap", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition head = neck.addOrReplaceChild(
                "head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.9484F, -6.705F, 6.0F, 4.0F, 9.0F, g), PartPose.offset(0.0F, -6.0516F, -0.2951F)
        );
        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -2.5F, -0.8F, 2.0F, 3.0F, 1.0F, g),
                PartPose.offsetAndRotation(2.0F, -4.2484F, 1.9451F, 0.0F, 0.0F, 0.2618F)
        );
        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 3.0F, 1.0F, g),
                PartPose.offsetAndRotation(-2.0F, -4.2484F, 1.645F, 0.0F, 0.0F, -0.2618F)
        );

        return mesh;
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.body.y = 12.5F;
    }

    @Override
    public void prepareMobModel(@NotNull T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);

        float eatAnim = entity.getEatAnim(partialTick);
        float standAnim = entity.getStandAnim(partialTick);

        float headY = 10.0F;
        float headZ = -6.0F;

        if (eatAnim > 0.0F) {
            headY = Mth.lerp(eatAnim, headY, 12.0F);
        } else if (standAnim > 0.0F) {
            headY = Mth.lerp(standAnim, headY, 8.0F);
            headZ = Mth.lerp(standAnim, headZ, -4.0F);
        }

        this.headParts.y = headY;
        this.headParts.z = headZ;
        this.body.y = 12.5F;

        this.tail.setPos(0.0F, -1.0F, 7.0F);
        this.tail.xRot = -0.7418F + limbSwingAmount * 0.75F;
        this.leftFrontLeg.y = 16.0F - 4.0F * standAnim;
        this.leftFrontLeg.z = -5.4F;
        this.rightFrontLeg.y = this.leftFrontLeg.y;
        this.rightFrontLeg.z = -5.4F;

        this.rightHindLeg.visible = true;
        this.leftHindLeg.visible = true;
        this.rightFrontLeg.visible = true;
        this.leftFrontLeg.visible = true;
        this.rightHindBabyLeg.visible = false;
        this.leftHindBabyLeg.visible = false;
        this.rightFrontBabyLeg.visible = false;
        this.leftFrontBabyLeg.visible = false;
    }

}
