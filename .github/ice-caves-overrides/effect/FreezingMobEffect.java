package org.zerw.net.icecaves.effect;

import net.minecraft.class_1291;
import net.minecraft.class_1309;
import net.minecraft.class_4081;

/** Snapshot 6 adds one frozen tick per effect tick, independently of amplifier. */
public final class FreezingMobEffect extends class_1291 {
    public FreezingMobEffect() {
        super(class_4081.field_18272, 10875635);
    }

    @Override
    public boolean method_5572(class_1309 mob, int amplification) {
        if (mob.method_32316()) {
            mob.method_32317(Math.min(mob.method_32315(), mob.method_32312() + 1));
        }
        return true;
    }

    @Override
    public boolean method_5552(int duration, int amplification) {
        return true;
    }
}
