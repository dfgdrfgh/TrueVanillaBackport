package org.zerw.net.icecaves.mixin;

import net.minecraft.*;
import net.minecraft.class_4970.class_2251;
import org.spongepowered.asm.mixin.Mixin;
import org.zerw.net.icecaves.registry.ModSounds;

/** Adds the 26.4 regular-ice display tick without altering packed/blue ice. */
@Mixin(class_2386.class)
public abstract class IceAmbientMixin extends class_2248 {
    protected IceAmbientMixin(class_2251 properties) { super(properties); }

    @Override
    public void method_9496(class_2680 state, class_1937 level, class_2338 pos, class_5819 random) {
        if (random.method_43048(10000) != 0 || !level.method_8320(pos.method_10074()).method_27852(class_2246.field_10382)
            || !level.method_8320(pos.method_10084()).method_26215()) return;
        for (class_2350 direction : class_2350.class_2353.field_11062) {
            for (int distance = 1; distance <= 8; distance++) {
                if (!level.method_8320(pos.method_10079(direction, distance)).method_27852(class_2246.field_10295)) return;
            }
        }
        level.method_8486(pos.method_10263(), pos.method_10264(), pos.method_10260(),
            ModSounds.ICE_AMBIENT, class_3419.field_15245, 1.0F, 1.0F, false);
    }
}
