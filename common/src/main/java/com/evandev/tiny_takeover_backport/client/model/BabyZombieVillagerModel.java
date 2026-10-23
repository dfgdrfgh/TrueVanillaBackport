package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.NotNull;

public class BabyZombieVillagerModel<T extends Zombie> extends ZombieVillagerModel<T> {

    public BabyZombieVillagerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("nose", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("mole", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rootHat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        rootHat.addOrReplaceChild("hat_rim", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.0F, 3.0F).texOffs(16, 22).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 18.75F, 0.0F)
        );

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition hat = head.addOrReplaceChild(
                "hat", CubeListBuilder.create().texOffs(0, 31).addBox(-4.0F, -4.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, -4.0F, 0.0F)
        );
        hat.addOrReplaceChild("hat_rim", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(0, 46).addBox(-7.0F, -0.5F, -6.0F, 14.0F, 1.0F, 12.0F), PartPose.offset(0.0F, -4.5F, 0.0F));

        PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -1.0F, -4.0F));
        nose.addOrReplaceChild("mole", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.0F, 21.5F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.0F, 21.5F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.head.setPos(0.0F, 16.0F, 0.0F);
        this.body.setPos(0.0F, 18.75F, 0.0F);
        this.rightArm.setPos(-3.0F, 15.5F, 0.0F);
        this.leftArm.setPos(3.0F, 15.5F, 0.0F);
        this.rightLeg.setPos(-1.0F, 21.5F, 0.0F);
        this.leftLeg.setPos(1.0F, 21.5F, 0.0F);
    }
}
