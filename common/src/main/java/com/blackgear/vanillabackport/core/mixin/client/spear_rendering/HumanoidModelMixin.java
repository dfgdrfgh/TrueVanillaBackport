package com.blackgear.vanillabackport.core.mixin.client.spear_rendering;

import com.blackgear.vanillabackport.common.api.extensions.entity.arms.ArmPoses;
import com.blackgear.vanillabackport.common.api.extensions.entity.spear.SpearSwingTracker;
import com.blackgear.vanillabackport.common.level.components.SwingAnimation;
import com.blackgear.vanillabackport.common.level.components.SwingAnimationType;
import com.blackgear.vanillabackport.common.level.items.spear.SpearAnimations;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class) @SuppressWarnings("unchecked")
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> {
    @Shadow public HumanoidModel.ArmPose rightArmPose;
    @Shadow public HumanoidModel.ArmPose leftArmPose;
    
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    
    @Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
    private void vb$poseRightArm(T entity, CallbackInfo ci) {
        if (this.rightArmPose == ArmPoses.SPEAR.get()) {
            SpearAnimations.thirdPersonHandUse(
                (HumanoidModel<T>)(Object)this,
                this.rightArm,
                this.head,
                HumanoidArm.RIGHT,
                entity.getMainArm() == HumanoidArm.RIGHT ? entity.getMainHandItem() : entity.getOffhandItem(),
                entity
            );
            ci.cancel();
        }
    }
    
    @Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
    private void vb$poseLeftArm(T entity, CallbackInfo ci) {
        if (this.leftArmPose == ArmPoses.SPEAR.get()) {
            SpearAnimations.thirdPersonHandUse(
                (HumanoidModel<T>)(Object)this,
                this.leftArm,
                this.head,
                HumanoidArm.LEFT,
                entity.getMainArm() == HumanoidArm.LEFT ? entity.getMainHandItem() : entity.getOffhandItem(),
                entity
            );
            ci.cancel();
        }
    }
    
    @Inject(method = "setupAttackAnimation", at = @At("HEAD"), cancellable = true)
    private void vb$attackAnimation(T entity, float ageInTicks, CallbackInfo ci) {
        if (this.attackTime <= 0.0F) return;
        if (entity instanceof SpearSwingTracker tracker && !tracker.vb$isAttackSwing()) return;
        
        InteractionHand hand = entity.swingingArm;
        ItemStack heldItem = entity.getItemInHand(hand);
        
        SwingAnimation animation = SwingAnimation.get(heldItem);
        if (animation != null && animation.type() == SwingAnimationType.STAB) {
            HumanoidArm attackArm = (hand == InteractionHand.MAIN_HAND)
                ? entity.getMainArm()
                : entity.getMainArm().getOpposite();
            
            // Modern vanilla applies the common body/arm swing setup before
            // the spear-specific STAB pose. Because this mixin cancels the
            // 1.21.1 method, reproduce that common prelude here first.
            this.body.yRot = Mth.sin(Mth.sqrt(this.attackTime) * Mth.TWO_PI) * 0.2F;
            if (attackArm == HumanoidArm.LEFT) {
                this.body.yRot *= -1.0F;
            }

            this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
            this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
            this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
            this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
            this.rightArm.yRot += this.body.yRot;
            this.leftArm.yRot += this.body.yRot;
            this.leftArm.xRot += this.body.yRot;

            SpearAnimations.thirdPersonAttackHand((HumanoidModel<T>)(Object)this, this.attackTime, attackArm);
            ci.cancel();
        }
    }
}