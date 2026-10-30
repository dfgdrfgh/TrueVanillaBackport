package org.zerw.net.icecaves.worldgen;

import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1588;
import net.minecraft.class_1936;
import net.minecraft.class_1959;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3730;
import net.minecraft.class_5425;
import net.minecraft.class_5321;
import net.minecraft.class_5819;
import net.minecraft.class_6862;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.zerw.net.icecaves.entity.FrostbiteZombie;
import org.zerw.net.icecaves.registry.ModBlocks;
import org.zerw.net.icecaves.registry.ModEntities;

/** Scope the decorator's extra cold variants without removing ordinary hostiles. */
public final class IceCaveSpawnControl {
    public static final class_6862<class_1959> HAS_ICE_CAVES = class_6862.method_40092(
        class_7924.field_41236, class_2960.method_60655("ice_caves", "has_ice_caves"));
    private static final class_5321<class_1959> ICE_CAVES = class_5321.method_29179(
        class_7924.field_41236, class_2960.method_60655("minecraft", "ice_caves"));
    public static final class_6862<class_1959> FROSTBITE_SURFACE_BIOMES = class_6862.method_40092(
        class_7924.field_41236, class_2960.method_60655("minecraft", "spawns_frostbite_surface"));
    private static final int[][] DIRECTIONS = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
    private IceCaveSpawnControl() {}

    public static boolean shouldBlockMonsterSpawn(class_1308 mob, class_1936 level, class_3730 reason) {
        return shouldBlockMonsterType(mob.method_5864(), level, mob.method_24515(), reason);
    }

    public static boolean shouldBlockMonsterSpawn(class_1297 entity, class_1936 level, class_3730 reason) {
        return entity instanceof class_1308 mob && shouldBlockMonsterSpawn(mob, level, reason);
    }

    public static boolean shouldBlockMonsterType(class_1299<?> type, class_1936 level, class_2338 pos, class_3730 reason) {
        if (!isFilteredSpawn(reason) || !isFrostbiteType(type)) return false;
        return !hasSolidSpawnFloor(level, pos, type) || !canSpawnColdVariant(level, pos);
    }

    public static boolean checkFrostbiteSpawnRules(class_1299<FrostbiteZombie> type, class_5425 level,
                                                  class_3730 reason, class_2338 pos, class_5819 random) {
        return class_1588.method_20680(type, level, reason, pos, random)
            && hasSolidSpawnFloor(level, pos, type)
            && (!isFilteredSpawn(reason) || canSpawnColdVariant(level, pos));
    }

    public static boolean hasSolidSpawnFloor(class_1936 level, class_2338 pos, class_1299<?> type) {
        class_2338 below = pos.method_10074();
        class_2680 floor = level.method_8320(below);
        return !floor.method_26215() && floor.method_26170(level, below, type);
    }

    public static boolean shouldAllowCaveStraySpawn(class_1299<?> type, class_5425 level,
                                                  class_2338 pos, class_3730 reason, class_5819 random) {
        return type == class_1299.field_6098 && isFilteredSpawn(reason)
            && isIceCaveInterior(level, pos)
            && class_1588.method_20680(class_1299.field_6098, level, reason, pos, random);
    }

    private static boolean isFrostbiteType(class_1299<?> type) {
        return ModEntities.FROSTBITE_ID.equals(class_7923.field_41177.method_10221(type));
    }

    private static boolean canSpawnColdVariant(class_1936 level, class_2338 pos) {
        // Vanilla's cold variants require sky except in a designated cave biome.
        // Skip powder snow above the entity as vanilla does for surface strays.
        class_2338 skyPos = pos.method_10084();
        while (level.method_8320(skyPos).method_27852(class_2246.field_27879)) skyPos = skyPos.method_10084();
        return level.method_23753(pos).method_40220(FROSTBITE_SURFACE_BIOMES)
            && level.method_8311(skyPos.method_10074()) || isIceCaveInterior(level, pos);
    }

    public static boolean isTrueIceCaves(class_1936 level, class_2338 pos) {
        return level.method_23753(pos).method_40225(ICE_CAVES);
    }

    private static boolean isIceCaveInterior(class_1936 level, class_2338 pos) {
        if (isTrueIceCaves(level, pos)) return true;
        if (!level.method_23753(pos).method_40220(HAS_ICE_CAVES) || level.method_8311(pos)) return false;
        // A surface-biome tag alone cannot identify a legacy decorated cave.
        // Retain existing caves only with distinctive decorations or a mixed wall
        // of packed ice and calcite. Ordinary snow, stone, and ice never qualify.
        int packedIce = 0, calcite = 0;
        class_2248 crystal = ModBlocks.iceCrystal();
        class_2248 icicle = ModBlocks.icicle();
        for (int[] direction : DIRECTIONS) {
            for (int step = 1; step <= 3; step++) {
                class_2338 probe = new class_2338(pos.method_10263() + direction[0] * step,
                    pos.method_10264() + direction[1] * step, pos.method_10260() + direction[2] * step);
                if (!level.method_22340(probe)) break;
                class_2680 state = level.method_8320(probe);
                if (crystal != class_2246.field_10124 && state.method_27852(crystal)
                    || icicle != class_2246.field_10124 && state.method_27852(icicle)) return true;
                if (state.method_27852(class_2246.field_10225)) { packedIce++; break; }
                if (state.method_27852(class_2246.field_27114)) { calcite++; break; }
            }
        }
        return packedIce >= 2 && calcite >= 1;
    }

    private static boolean isFilteredSpawn(class_3730 reason) {
        return reason == class_3730.field_16459 || reason == class_3730.field_16472;
    }
}
