package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.entity.AxolotlAnimationStates;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Axolotl.class)
public abstract class AxolotlMixin implements AxolotlAnimationStates {
    @Unique
    private final AnimationState[] tiny_takeover_backport$animations = tiny_takeover_backport$createAnimations();

    @Unique
    private static AnimationState[] tiny_takeover_backport$createAnimations() {
        // Older Forge Mixin versions cannot merge array stores from an instance
        // field initializer. Keep those instructions in an ordinary method.
        return new AnimationState[]{new AnimationState(), new AnimationState(), new AnimationState(),
                new AnimationState(), new AnimationState(), new AnimationState(), new AnimationState()};
    }

    @Override
    public AnimationState[] tiny_takeover_backport$getAxolotlAnimations() {
        return this.tiny_takeover_backport$animations;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void tiny_takeover_backport$tickBabyAnimations(CallbackInfo ci) {
        Axolotl axolotl = (Axolotl) (Object) this;
        if (!axolotl.level().isClientSide()) return;
        int selected = -1;
        if (axolotl.isBaby()) {
            boolean water = axolotl.isInWater();
            boolean ground = axolotl.onGround();
            boolean moving = axolotl.walkAnimation.isMoving()
                    || axolotl.getXRot() != axolotl.xRotO || axolotl.getYRot() != axolotl.yRotO;
            if (axolotl.isPlayingDead()) {
                selected = PLAY_DEAD;
            } else if (moving) {
                selected = water && !ground ? SWIM : !water && ground ? WALK : WALK_WATER;
            } else {
                selected = water ? (ground ? IDLE_WATER_GROUND : IDLE_WATER) : IDLE_GROUND;
            }
        }
        for (int i = 0; i < this.tiny_takeover_backport$animations.length; i++) {
            if (i == selected) this.tiny_takeover_backport$animations[i].startIfStopped(axolotl.tickCount);
            else this.tiny_takeover_backport$animations[i].stop();
        }
    }
}
