package org.zerw.net.icecaves.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.*;
import net.minecraft.class_2689.class_2690;
import net.minecraft.class_4970.class_2251;
import org.zerw.net.icecaves.registry.ModSounds;

/** 26.4 cracking rules. STAGE is retained as a Java API alias for the generator. */
public class BrittleIceBlock extends class_2248 {
    public static final MapCodec<BrittleIceBlock> CODEC = method_54094(BrittleIceBlock::new);
    public static final class_2758 STAGE = class_2758.method_11867("age", 0, 3);
    private static final class_6862<class_1299<?>> IMMUNE = class_6862.method_40092(
        class_7924.field_41266, class_2960.method_60655("minecraft", "does_not_break_brittle_ice"));

    public BrittleIceBlock(class_2251 properties) {
        super(properties);
        this.method_9590(this.field_10647.method_11664().method_11657(STAGE, 0));
    }

    public MapCodec<BrittleIceBlock> method_53969() { return CODEC; }
    protected boolean method_9522(class_2680 state, class_2680 neighbor, class_2350 direction) {
        return neighbor.method_27852(this);
    }
    protected void method_9515(class_2690<class_2248, class_2680> builder) {
        builder.method_11667(STAGE);
    }

    public void method_9591(class_1937 world, class_2338 pos, class_2680 state, class_1297 entity) {
        if (!(world instanceof class_3218 level) || !(entity instanceof class_1309)
            || entity.method_5864().method_20210(IMMUNE)) return;
        if (entity instanceof class_1657 player) {
            if (level.method_8503().method_3785(level, pos, player)) return;
        } else if (!level.method_8450().method_8355(class_1928.field_19388)) return;
        if (level.method_8397().method_8674(pos, this)) return;

        if (crack(level, pos, state)) {
            for (class_2350 direction : class_2350.values()) {
                class_2338 adjacent = pos.method_10093(direction);
                class_2680 neighbor = level.method_8320(adjacent);
                if (neighbor.method_27852(this) && !crack(level, adjacent, neighbor)) {
                    level.method_39279(adjacent, this, 24);
                }
            }
        }
        // The scheduled tick is the cooldown; continued stepping performs the next crack.
        level.method_39279(pos, this, 24);
    }

    private boolean crack(class_3218 level, class_2338 pos, class_2680 state) {
        int age = state.method_11654(STAGE);
        if (age == 3) {
            level.method_30093(pos, false, null, 512);
            return true;
        }
        level.method_8652(pos, state.method_11657(STAGE, age + 1), 2);
        level.method_8396(null, pos, ModSounds.BRITTLE_ICE_CRACK, class_3419.field_15245, 1.0F, 1.0F);
        // 1.21.1 has no 2014 cracking-particle event. Use its block particles at the same first crack.
        if (age == 0) level.method_20290(2001, pos, class_2248.method_9507(state));
        level.method_33596(null, class_5712.field_28733, pos);
        return false;
    }
}
