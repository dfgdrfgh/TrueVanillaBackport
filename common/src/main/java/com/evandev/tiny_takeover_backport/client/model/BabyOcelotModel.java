package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.Ocelot;
import org.jetbrains.annotations.NotNull;

public class BabyOcelotModel<T extends Ocelot> extends OcelotModel<T> {

    public BabyOcelotModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, -3.0F, -2.875F, 5.0F, 4.0F, 4.0F)
                        .texOffs(18, 0)
                        .addBox(-2.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F)
                        .texOffs(24, 0)
                        .addBox(1.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F)
                        .texOffs(18, 3)
                        .addBox(-1.5F, -1.0F, -3.875F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 20.0F, -3.125F)
        );
        partdefinition.addOrReplaceChild(
                "left_front_leg", CubeListBuilder.create().texOffs(18, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, -1.5F)
        );
        partdefinition.addOrReplaceChild(
                "right_front_leg", CubeListBuilder.create().texOffs(12, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 22.0F, -1.5F)
        );
        partdefinition.addOrReplaceChild(
                "left_hind_leg", CubeListBuilder.create().texOffs(18, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, 2.5F)
        );
        partdefinition.addOrReplaceChild(
                "body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F), PartPose.offset(0.0F, 20.5F, 0.5F)
        );
        partdefinition.addOrReplaceChild(
                "right_hind_leg", CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 22.0F, 2.5F)
        );
        partdefinition.addOrReplaceChild(
                "tail1",
                CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -0.107F, 0.0849F, 1.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 19.107F, 3.9151F, -0.567232F, 0.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void prepareMobModel(@NotNull T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);

        this.body.setPos(0.0F, 20.5F, 0.5F);
        this.body.xRot = 0.0F;

        this.head.setPos(0.0F, 20.0F, -3.125F);
        this.tail1.setPos(0.0F, 19.107F, 3.9151F);
        this.tail1.xRot = -0.567232F;

        this.leftFrontLeg.setPos(1.0F, 22.0F, -1.5F);
        this.rightFrontLeg.setPos(-1.0F, 22.0F, -1.5F);
        this.leftHindLeg.setPos(1.0F, 22.0F, 2.5F);
        this.rightHindLeg.setPos(-1.0F, 22.0F, 2.5F);

        if (entity.isCrouching()) {
            this.body.y += 1.0F;
            this.head.y += 2.0F;
            this.tail1.y += 1.0F;
            this.tail1.xRot = (float) (Math.PI / 2);
        } else if (entity.isSprinting()) {
            this.tail1.xRot = (float) (Math.PI / 2);
        }
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.body.xRot = 0.0F;
    }
}
