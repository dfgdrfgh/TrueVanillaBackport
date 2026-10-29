package org.zerw.net.icecaves.block;

import net.minecraft.*;
import net.minecraft.class_4970.class_2251;
import org.zerw.net.icecaves.registry.ModSounds;

public final class IceCrystalBlock extends class_5542 {
    // 1.21.1's cluster constructor uses height and horizontal inset, rather than height and width.
    public IceCrystalBlock(class_2251 properties) { super(7.0F, 3.0F, properties); }

    @Override
    public void method_9496(class_2680 state, class_1937 level, class_2338 pos, class_5819 random) {
        if (random.method_43048(250) == 0 && level.method_8320(pos.method_10074()).method_27852(class_2246.field_10225)) {
            level.method_8486(pos.method_10263(), pos.method_10264(), pos.method_10260(),
                ModSounds.ICE_CRYSTAL_AMBIENT, class_3419.field_15256, 1.0F, 1.0F, false);
        }
    }
}
