package org.zerw.net.icecaves.worldgen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_5281;
import net.minecraft.class_5819;

/**
 * 26.4 snapshot 6's ice_crevasse geometry and fluid sealing, adapted to 1.21.1.
 * Cave selection and placement frequency still belong to the older decorator.
 */
public final class VanillaCrevasse {
    private static final int[][] SIDES = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
    private VanillaCrevasse() {}

    private record Surface(class_2338 pos, class_2680 state) {}
    private record Column(class_2338 pos, class_2680 original, int depth) {
        int bottom() { return pos.method_10264() - depth; }
        boolean contains(int y) { return y >= bottom() && y <= pos.method_10264(); }
    }

    private static long key(int x, int z) {
        return (x & 0xffffffffL) | ((z & 0xffffffffL) << 32);
    }

    private static class_2338 offset(class_2338 p, int x, int y, int z) {
        return new class_2338(p.method_10263() + x, p.method_10264() + y, p.method_10260() + z);
    }

    private static boolean carvable(class_5281 level, class_2338 pos) {
        if (!level.method_37368(pos)) return false;
        class_2680 state = level.method_8320(pos);
        // 26.4 ice_crevasse_replaceables includes stone/deepslate ore tags,
        // height_specific_ore_replaceables (tuff), and packed ice + calcite.
        return state.method_26164(class_3481.field_28992)
            || state.method_26164(class_3481.field_28993)
            || state.method_27852(class_2246.field_27165)
            || state.method_27852(class_2246.field_10225)
            || state.method_27852(class_2246.field_27114);
    }

    private static boolean barrier(class_5281 level, class_2338 pos) {
        class_2680 state = level.method_8320(pos);
        // 26.4 blocks_fluid_flow is blocks_motion plus signs. In 1.21.1 the
        // material motion predicate remains available directly on block states.
        return state.method_51366() || state.method_26164(class_3481.field_41282);
    }

    private static Surface findSurface(class_5281 level, class_2338 center, int range) {
        class_2338 start = offset(center, 0, range / 2, 0);
        boolean airAbove = level.method_8320(offset(start, 0, 1, 0)).method_26215();
        int minY = Math.max(level.method_31607(), start.method_10264() - range);
        for (int y = start.method_10264(); y >= minY; y--) {
            class_2338 pos = new class_2338(center.method_10263(), y, center.method_10260());
            class_2680 state = level.method_8320(pos);
            if (carvable(level, pos) && airAbove) return new Surface(pos, state);
            airAbove = state.method_26215();
        }
        return null;
    }

    private static void appendBranch(Map<Long, Column> columns, class_5281 level,
                                     class_2338 origin, int carveDepth, float yaw, class_5819 random) {
        double cursorX = origin.method_10263();
        double cursorZ = origin.method_10260();
        int previousY = origin.method_10264();
        float acceleration = 0.0F;
        for (int step = 0; step < 8; step++) {
            cursorX += Math.cos(yaw);
            cursorZ += Math.sin(yaw);
            float direction = (float) random.method_43059();
            float magnitude = random.method_43057();
            acceleration = acceleration * 0.5F + direction * magnitude * 4.0F;
            yaw += acceleration * 0.05F;
            int blockX = (int) Math.floor(cursorX);
            int blockZ = (int) Math.floor(cursorZ);
            Surface surface = findSurface(level, new class_2338(blockX, previousY, blockZ), 8);
            if (surface == null) continue;
            float taper = 1.0F - ((float) step / 8) * 0.6F;
            float radius = Math.max(4 * 0.5F * taper, 0.5F);
            float radiusSquared = radius * radius;
            int maxDepth = Math.max((int) (carveDepth * taper), 1);
            int expanded = (int) Math.ceil(radius);
            for (int x = blockX - expanded; x <= blockX + expanded; x++) {
                for (int z = blockZ - expanded; z <= blockZ + expanded; z++) {
                    int dx = x - blockX, dz = z - blockZ;
                    if (dx * dx + dz * dz > radiusSquared || columns.containsKey(key(x, z))) continue;
                    Surface next = findSurface(level, new class_2338(x, surface.pos.method_10264(), z), 8);
                    if (next == null) continue;
                    int depth = 1;
                    for (class_2338 probe = offset(next.pos, 0, -1, 0);
                         depth < maxDepth && carvable(level, probe); depth++) {
                        probe = offset(probe, 0, -1, 0);
                    }
                    columns.put(key(x, z), new Column(next.pos, next.state, depth - 1));
                }
            }
            previousY = surface.pos.method_10264();
        }
    }

    private static Column neighbor(Map<Long, Column> columns, class_2338 pos) {
        return columns.get(key(pos.method_10263(), pos.method_10260()));
    }

    private static boolean steepNeighbor(Map<Long, Column> columns, Column column) {
        for (int[] side : SIDES) {
            Column next = neighbor(columns, offset(column.pos, side[0], 0, side[1]));
            if (next != null && Math.abs(column.pos.method_10264() - next.pos.method_10264()) > 1) return true;
        }
        return false;
    }

    private static void keepConnected(Map<Long, Column> columns, class_2338 origin) {
        Column start = neighbor(columns, origin);
        if (start == null || steepNeighbor(columns, start)) { columns.clear(); return; }
        var connected = new ArrayList<Column>();
        var processed = new HashSet<Long>();
        connected.add(start);
        processed.add(key(origin.method_10263(), origin.method_10260()));
        for (int index = 0; index < connected.size(); index++) {
            for (int[] side : SIDES) {
                class_2338 pos = offset(connected.get(index).pos, side[0], 0, side[1]);
                Column next = neighbor(columns, pos);
                long k = key(pos.method_10263(), pos.method_10260());
                if (next != null && !processed.contains(k) && !steepNeighbor(columns, next)) {
                    connected.add(next);
                    processed.add(k);
                }
            }
        }
        columns.clear();
        for (Column column : connected) columns.put(key(column.pos.method_10263(), column.pos.method_10260()), column);
    }

    private static boolean sideLiquid(class_5281 level, class_2338 pos) {
        for (int[] side : SIDES) {
            if (!level.method_8316(offset(pos, side[0], 0, side[1])).method_15769()) return true;
        }
        return false;
    }

    private static void carveColumn(class_5281 level, Map<Long, Column> columns,
                                    Column column, int fillLevel, class_2248 brittle) {
        boolean carved = false;
        for (int y = column.pos.method_10264(); y >= column.bottom(); y--) {
            class_2338 pos = new class_2338(column.pos.method_10263(), y, column.pos.method_10260());
            boolean fill = y <= fillLevel;
            if (!fill && sideLiquid(level, pos)) break;
            class_2248 placement = fill ? class_2246.field_10382 : class_2246.field_10124;
            for (int[] side : SIDES) {
                class_2338 nextPos = offset(pos, side[0], 0, side[1]);
                if (!level.method_37368(nextPos)) continue;
                Column next = neighbor(columns, nextPos);
                if (!barrier(level, nextPos) && (next == null || !next.contains(y))
                    && !level.method_8320(nextPos).method_27852(placement)) {
                    level.method_8652(nextPos, column.original, 2);
                }
            }
            level.method_8652(pos, placement.method_9564(), 2);
            if (fill && y == column.bottom()) {
                class_2338 below = offset(pos, 0, -1, 0);
                if (level.method_37368(below) && !barrier(level, below)
                    && !level.method_8320(below).method_27852(placement)) {
                    level.method_8652(below, column.original, 2);
                }
            }
            carved = true;
        }
        if (carved) level.method_8652(column.pos, brittle.method_9564(), 2);
    }

    /** Origin must be the exposed, solid cave floor (the crevasse cap level). */
    public static boolean place(class_5281 level, class_2338 origin, class_5819 random, class_2248 brittle) {
        int depth = 6 + random.method_43048(5);
        if (origin.method_10264() <= level.method_31607() + depth) return false;
        float yaw = random.method_43057() * (float) (Math.PI * 2);
        int fillDepth = 4 + random.method_43048(3);
        Map<Long, Column> columns = new HashMap<>(50);
        for (int direction = 0; direction < 2; direction++) {
            appendBranch(columns, level, origin, depth, yaw + direction * (float) Math.PI, random);
        }
        keepConnected(columns, origin);
        if (columns.size() < 10) return false;
        int lowestY = columns.values().stream().mapToInt(c -> c.pos.method_10264()).min().orElseThrow();
        int fillLevel = Math.min(origin.method_10264() - fillDepth, lowestY - 1);
        for (Column column : columns.values().stream().sorted(Comparator
                .comparingInt((Column c) -> c.pos.method_10263()).thenComparingInt(c -> c.pos.method_10260())).toList()) {
            carveColumn(level, columns, column, fillLevel, brittle);
        }
        return true;
    }
}
