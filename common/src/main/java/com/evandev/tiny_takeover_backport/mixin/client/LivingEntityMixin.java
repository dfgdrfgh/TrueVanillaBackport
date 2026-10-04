package com.evandev.tiny_takeover_backport.mixin.client;

import com.evandev.tiny_takeover_backport.client.ModRenderHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "getAgeScale", at = @At("HEAD"), cancellable = true)
    private void suppressAgeScale(CallbackInfoReturnable<Float> cir) {
        if (ModRenderHelper.SUPPRESS_AGE_SCALE.get()) {
            cir.setReturnValue(1.0F);
        }
    }
}
