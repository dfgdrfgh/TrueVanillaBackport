package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.entity.AgeLockable;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {
    @ModifyExpressionValue(method = "handleEating", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;isBaby()Z"))
    private boolean tiny_takeover_backport$canFeedForGrowth(boolean baby) {
        // Healing and temper still work; only the growth branch is disabled.
        return baby && !((AgeLockable) this).tiny_takeover_backport$isAgeLocked();
    }
}
