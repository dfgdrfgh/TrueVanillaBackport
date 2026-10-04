package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import org.jetbrains.annotations.NotNull;

public class BabyHoglinModel<T extends Mob & HoglinBase> extends HoglinModel<T> {

    private static final float BABY_HEAD_Y = 13.0F;

    private final ModelPart head;

    public BabyHoglinModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -2.2605F, -10.547F, 10.0F, 4.0F, 12.0F)
                        .texOffs(44, 29)
                        .addBox(-7.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F)
                        .texOffs(52, 29)
                        .addBox(5.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, BABY_HEAD_Y, -7.0F, 0.8727F, 0.0F, 0.0F)
        );

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-4.0F, -14.0F, -7.0F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.02F))
                        .texOffs(24, 39)
                        .addBox(0.0F, -18.0F, -8.0F, 0.0F, 6.0F, 11.0F, new CubeDeformation(0.02F)),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        body.addOrReplaceChild("mane", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create().texOffs(32, 5).addBox(-5.1F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-5.0F, -1.0F, -1.5F, 0.0F, 0.0F, -0.8727F)
        );
        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.9F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F).mirror(false),
                PartPose.offsetAndRotation(5.0F, -1.0F, -1.5F, 0.0F, 0.0F, 0.8727F)
        );

        root.addOrReplaceChild(
                "right_hind_leg", CubeListBuilder.create().texOffs(0, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, 4.5F)
        );
        root.addOrReplaceChild(
                "left_hind_leg", CubeListBuilder.create().texOffs(12, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, 4.5F)
        );
        root.addOrReplaceChild(
                "right_front_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, -4.5F)
        );
        root.addOrReplaceChild(
                "left_front_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, -4.5F)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int attackTicks = entity.getAttackAnimationRemainingTicks();
        float attackFactor = 1.0F - Math.abs(10 - 2 * attackTicks) / 10.0F;
        this.head.y = net.minecraft.util.Mth.lerp(attackFactor, BABY_HEAD_Y, BABY_HEAD_Y + 2.5F);
    }
}
