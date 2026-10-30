package org.zerw.net.icecaves.effect;

import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_8111;
import org.zerw.net.icecaves.registry.ModEffects;

/** The two effect-specific changes to 1.21.1's native freezing processing. */
public final class FreezingLogic {
    private FreezingLogic() {}

    public static int thawTicks(class_1309 mob, int proposed) {
        return mob.method_32316() && hasFreezing(mob) ? mob.method_32312() : proposed;
    }

    public static float freezeDamage(class_1309 mob, class_1282 source, float proposed) {
        return source.method_49708(class_8111.field_42354) && hasFreezing(mob) ? 1.5F : proposed;
    }

    private static boolean hasFreezing(class_1309 mob) {
        return ModEffects.freezing().map(mob::method_6059).orElse(false);
    }
}
