package com.evandev.tiny_takeover_backport.client.model;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class BabyZombifiedPiglinModel<T extends Mob> extends PiglinModel<T> {

    public BabyZombifiedPiglinModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return BabyPiglinModel.createBodyLayer();
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
