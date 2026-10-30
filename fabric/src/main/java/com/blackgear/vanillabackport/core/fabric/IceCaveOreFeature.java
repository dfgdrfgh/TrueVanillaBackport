package com.blackgear.vanillabackport.core.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.BitSet;
import java.util.List;

/** Snapshot's sequence: a size-20 host pocket, then the original ore at the same origin.
 * The two passes share the random source; the pocket never directly produces ore.
 */
public final class IceCaveOreFeature extends Feature<OreConfiguration> {
    private static final OreConfiguration POCKET_CONFIG = new OreConfiguration(List.of(
        OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), Blocks.STONE.defaultBlockState()),
        OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), Blocks.DEEPSLATE.defaultBlockState())
    ), 20, 0.0F);
    private static final HeightAwareOre POCKET = new HeightAwareOre(true);
    private static final HeightAwareOre ORE = new HeightAwareOre(false);

    public IceCaveOreFeature() {
        super(OreConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        // SequenceFeature stops at the first failed feature, as in the snapshot.
        if (!POCKET.place(new FeaturePlaceContext<>(context.topFeature(), context.level(),
            context.chunkGenerator(), context.random(), context.origin(), POCKET_CONFIG))) return false;
        return ORE.place(context);
    }

    public static BlockState heightForOreBlock(BlockState stone, BlockState deepslate, int y) {
        return y >= 0 ? stone : deepslate;
    }

    /** 1.21.1's ore traversal with the snapshot's per-block height-sensitive targets.
     * A separate origin-height choice would produce wrong blocks across y=0.
     */
    private static final class HeightAwareOre extends OreFeature {
        private final boolean pocket;

        private HeightAwareOre(boolean pocket) {
            super(OreConfiguration.CODEC);
            this.pocket = pocket;
        }

        @Override
        protected boolean doPlace(WorldGenLevel level, RandomSource random, OreConfiguration config,
                                  double x0, double x1, double z0, double z1, double y0, double y1,
                                  int xStart, int yStart, int zStart, int sizeXZ, int sizeY) {
            int placed = 0;
            BitSet visited = new BitSet(sizeXZ * sizeY * sizeXZ);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int size = config.size;
            double[] spheres = new double[size * 4];
            for (int i = 0; i < size; i++) {
                float step = (float)i / size;
                spheres[i * 4] = Mth.lerp(step, x0, x1);
                spheres[i * 4 + 1] = Mth.lerp(step, y0, y1);
                spheres[i * 4 + 2] = Mth.lerp(step, z0, z1);
                double scale = random.nextDouble() * size / 16.0;
                spheres[i * 4 + 3] = ((Mth.sin((float)Math.PI * step) + 1.0F) * scale + 1.0) / 2.0;
            }
            for (int i = 0; i < size - 1; i++) {
                if (spheres[i * 4 + 3] <= 0.0) continue;
                for (int j = i + 1; j < size; j++) {
                    if (spheres[j * 4 + 3] <= 0.0) continue;
                    double dx = spheres[i * 4] - spheres[j * 4];
                    double dy = spheres[i * 4 + 1] - spheres[j * 4 + 1];
                    double dz = spheres[i * 4 + 2] - spheres[j * 4 + 2];
                    double dr = spheres[i * 4 + 3] - spheres[j * 4 + 3];
                    if (dr * dr > dx * dx + dy * dy + dz * dz) {
                        spheres[(dr > 0.0 ? j : i) * 4 + 3] = -1.0;
                    }
                }
            }
            try (BulkSectionAccess sections = new BulkSectionAccess(level)) {
                for (int i = 0; i < size; i++) {
                    double radius = spheres[i * 4 + 3];
                    if (radius < 0.0) continue;
                    double cx = spheres[i * 4], cy = spheres[i * 4 + 1], cz = spheres[i * 4 + 2];
                    int minX = Math.max(Mth.floor(cx - radius), xStart);
                    int minY = Math.max(Mth.floor(cy - radius), yStart);
                    int minZ = Math.max(Mth.floor(cz - radius), zStart);
                    int maxX = Math.max(Mth.floor(cx + radius), minX);
                    int maxY = Math.max(Mth.floor(cy + radius), minY);
                    int maxZ = Math.max(Mth.floor(cz + radius), minZ);
                    for (int x = minX; x <= maxX; x++) {
                        double dx = (x + 0.5 - cx) / radius;
                        if (dx * dx >= 1.0) continue;
                        for (int y = minY; y <= maxY; y++) {
                            double dy = (y + 0.5 - cy) / radius;
                            if (dx * dx + dy * dy >= 1.0) continue;
                            for (int z = minZ; z <= maxZ; z++) {
                                double dz = (z + 0.5 - cz) / radius;
                                if (dx * dx + dy * dy + dz * dz >= 1.0 || level.isOutsideBuildHeight(y)) continue;
                                int index = x - xStart + (y - yStart) * sizeXZ + (z - zStart) * sizeXZ * sizeY;
                                if (visited.get(index)) continue;
                                visited.set(index);
                                pos.set(x, y, z);
                                if (!level.ensureCanWrite(pos)) continue;
                                LevelChunkSection section = sections.getSection(pos);
                                if (section == null) continue;
                                int sx = SectionPos.sectionRelative(x), sy = SectionPos.sectionRelative(y), sz = SectionPos.sectionRelative(z);
                                BlockState existing = section.getBlockState(sx, sy, sz);
                                // Modern tuff is height-specific, not unconditionally deepslate.
                                // Packed ice/calcite are eligible only in the host-pocket pass.
                                boolean heightSpecific = existing.is(Blocks.TUFF)
                                    || pocket && (existing.is(Blocks.PACKED_ICE) || existing.is(Blocks.CALCITE));
                                if (heightSpecific) {
                                    OreConfiguration.TargetBlockState target = config.targetStates.get(y >= 0 ? 0 : 1);
                                    // Test with the corresponding normal host so air-discard RNG stays vanilla.
                                    BlockState host = heightForOreBlock(Blocks.STONE.defaultBlockState(), Blocks.DEEPSLATE.defaultBlockState(), y);
                                    if (canPlaceOre(host, sections::getBlockState, random, config, target, pos)) {
                                        section.setBlockState(sx, sy, sz, target.state, false);
                                        placed++;
                                    }
                                } else {
                                    for (OreConfiguration.TargetBlockState target : config.targetStates) {
                                        if (canPlaceOre(existing, sections::getBlockState, random, config, target, pos)) {
                                            section.setBlockState(sx, sy, sz, target.state, false);
                                            placed++;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return placed > 0;
        }
    }
}
