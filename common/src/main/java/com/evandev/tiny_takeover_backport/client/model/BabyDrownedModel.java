package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.monster.Drowned;
import org.jetbrains.annotations.NotNull;

public class BabyDrownedModel<T extends Drowned> extends DrownedModel<T> {

    public BabyDrownedModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(CubeDeformation.NONE);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation g) {
        return BabyZombieModel.createBodyLayer(g);
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
