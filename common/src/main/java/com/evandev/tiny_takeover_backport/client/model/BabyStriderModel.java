package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Strider;
import org.jetbrains.annotations.NotNull;

public class BabyStriderModel extends StriderModel {

    private static final float BABY_BODY_Y = 16.75F;
    private static final float BABY_LEG_Y = 20.0F;

    private final ModelPart body;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    private final ModelPart frontBristle;
    private final ModelPart middleBristle;
    private final ModelPart bottomBristle;

    public BabyStriderModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");

        this.frontBristle = this.body.getChild("bristle2");
        this.middleBristle = this.body.getChild("bristle1");
        this.bottomBristle = this.body.getChild("bristle0");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.75F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, BABY_BODY_Y, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.5F, BABY_LEG_Y, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, BABY_LEG_Y, 0.0F)
        );
        body.addOrReplaceChild(
                "bristle0",
                CubeListBuilder.create().texOffs(0, 21).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -4.25F, 2.0F)
        );
        body.addOrReplaceChild(
                "bristle1",
                CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -4.25F, 0.0F)
        );
        body.addOrReplaceChild(
                "bristle2",
                CubeListBuilder.create().texOffs(0, 15).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -4.25F, -2.0F)
        );

        body.addOrReplaceChild("right_bottom_bristle", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("right_middle_bristle", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("right_top_bristle", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("left_top_bristle", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("left_middle_bristle", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("left_bottom_bristle", CubeListBuilder.create(), PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(@NotNull Strider entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        limbSwingAmount = Math.min(0.25F, limbSwingAmount);
        this.body.y = BABY_BODY_Y - 2.0F * Mth.cos(limbSwing * 1.5F) * 2.0F * limbSwingAmount;
        this.leftLeg.y = BABY_LEG_Y + 2.0F * Mth.sin(limbSwing * 1.5F * 0.5F + (float) Math.PI) * 2.0F * limbSwingAmount;
        this.rightLeg.y = BABY_LEG_Y + 2.0F * Mth.sin(limbSwing * 1.5F * 0.5F) * 2.0F * limbSwingAmount;

        float bristleFlow = Mth.cos(limbSwing * 1.5F + (float) Math.PI) * limbSwingAmount;
        this.frontBristle.xRot = bristleFlow * 0.6F + 0.1F * Mth.sin(ageInTicks * 0.4F);
        this.middleBristle.xRot = bristleFlow * 1.2F + 0.1F * Mth.sin(ageInTicks * 0.2F);
        this.bottomBristle.xRot = bristleFlow * 1.3F + 0.05F * Mth.sin(ageInTicks * -0.4F);
    }
}
