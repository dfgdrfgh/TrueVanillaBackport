package com.evandev.tiny_takeover_backport.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.animal.Rabbit$RabbitMoveControl")
public abstract class RabbitMoveControlMixin extends MoveControl {

    protected RabbitMoveControlMixin(Mob mob) {
        super(mob);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Rabbit$RabbitMoveControl;hasWanted()Z"))
    private boolean tiny_takeover_backport$keepSpeedWhileJumping(boolean hasWanted) {
        return hasWanted || this.operation == MoveControl.Operation.JUMPING;
    }
}
