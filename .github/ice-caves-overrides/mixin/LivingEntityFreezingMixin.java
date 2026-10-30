package org.zerw.net.icecaves.mixin;

import net.minecraft.class_1282;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.zerw.net.icecaves.effect.FreezingLogic;

@Mixin(class_1309.class)
public abstract class LivingEntityFreezingMixin {
    // 1.21.1 aiStep: ordinal 0 adds powder-snow freezing; ordinal 1 thaws it.
    @ModifyArg(method = "method_6007", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/class_1309;method_32317(I)V", ordinal = 1), index = 0)
    private int iceCaves$preserveEffectFreezing(int proposed) {
        return FreezingLogic.thawTicks((class_1309)(Object)this, proposed);
    }

    // The first hurt call is freezing, before the later drowning damage call.
    // Capture both arguments as well so the helper also verifies the damage type.
    @ModifyArg(method = "method_6007", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/class_1309;method_5643(Lnet/minecraft/class_1282;F)Z", ordinal = 0), index = 1)
    private float iceCaves$freezingEffectDamage(class_1282 source, float proposed) {
        return FreezingLogic.freezeDamage((class_1309)(Object)this, source, proposed);
    }
}
