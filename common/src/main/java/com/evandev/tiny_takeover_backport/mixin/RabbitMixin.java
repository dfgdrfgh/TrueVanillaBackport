package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.evandev.tiny_takeover_backport.entity.RabbitAnimationStates;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Rabbit.class)
public abstract class RabbitMixin extends Animal implements RabbitAnimationStates {
    @Shadow
    private int jumpTicks;
    @Unique
    private final AnimationState tiny_takeover_backport$hopAnimationState = new AnimationState();
    @Unique
    private final AnimationState tiny_takeover_backport$idleHeadTiltAnimationState = new AnimationState();
    @Unique
    private int tiny_takeover_backport$idleAnimationTimeout = this.random.nextInt(40) + 180;

    @Unique
    private static final EntityDimensions ADULT_261_DIMENSIONS = EntityDimensions.scalable(0.49F, 0.6F).withEyeHeight(0.59F);
    @Unique
    private static final EntityDimensions BABY_261_DIMENSIONS = EntityDimensions.scalable(0.24F, 0.4F).withEyeHeight(0.39F);

    protected RabbitMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public AnimationState tiny_takeover_backport$getHopAnimationState() {
        return this.tiny_takeover_backport$hopAnimationState;
    }

    @Override
    public AnimationState tiny_takeover_backport$getIdleHeadTiltAnimationState() {
        return this.tiny_takeover_backport$idleHeadTiltAnimationState;
    }

    @ModifyExpressionValue(method = {"startJumping", "handleEntityEvent"}, at = @At(value = "CONSTANT", args = "intValue=10"))
    private int tiny_takeover_backport$hopDuration(int duration) {
        ModConfig config = ModConfig.get();
        return (this.isBaby() ? config.isModelEnabled(this) : config.isAdultRabbitReplaced(this)) ? 15 : duration;
    }

    @ModifyReturnValue(method = "getJumpPower()F", at = @At("RETURN"))
    private float tiny_takeover_backport$jumpPower(float original) {
        float power = 0.3F;
        if (this.moveControl.getSpeedModifier() <= 0.6) {
            power = 0.2F;
        }
        Path path = this.navigation.getPath();
        if (path != null && !path.isDone() && path.getNextEntityPos(this).y > this.getY() + 0.5) {
            power = 0.5F;
        }
        if (this.horizontalCollision || this.jumping && this.moveControl.getWantedY() > this.getY() + 0.5) {
            power = 0.5F;
        }
        return this.getJumpPower(power / 0.42F);
    }

    @ModifyArg(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Rabbit;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"), index = 1)
    private Vec3 tiny_takeover_backport$standingJumpLift(Vec3 relative) {
        return new Vec3(0.0, this.isBaby() ? 0.5 : 1.5, 1.0);
    }

    @ModifyExpressionValue(method = "setLandingDelay", at = @At(value = "CONSTANT", args = "intValue=1"))
    private int tiny_takeover_backport$panicJumpDelay(int delay) {
        return 3;
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void tiny_takeover_backport$setupAnimationStates(CallbackInfo ci) {
        if (!this.level().isClientSide()) return;
        if (this.tiny_takeover_backport$idleAnimationTimeout <= 0 && !this.isLeashed() && !this.isNoAi()) {
            this.tiny_takeover_backport$idleAnimationTimeout = this.random.nextInt(40) + 180;
            this.tiny_takeover_backport$idleHeadTiltAnimationState.start(this.tickCount);
        } else if (this.jumpTicks > 0) {
            this.tiny_takeover_backport$hopAnimationState.startIfStopped(this.tickCount);
            this.tiny_takeover_backport$idleHeadTiltAnimationState.stop();
        } else {
            this.tiny_takeover_backport$idleAnimationTimeout--;
            this.tiny_takeover_backport$hopAnimationState.stop();
        }
        if (this.isLeashed()) {
            this.tiny_takeover_backport$idleHeadTiltAnimationState.stop();
        }
    }

    @Override
    public @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        if (ModConfig.get().rabbitBoundingBox) {
            return this.isBaby() ? BABY_261_DIMENSIONS : ADULT_261_DIMENSIONS;
        }
        return super.getDefaultDimensions(pose);
    }
}
