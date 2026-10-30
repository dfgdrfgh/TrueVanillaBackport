# Ice Caves biome accuracy checkpoint

Reference: Mojang's `26.4-snapshot-6` client resources and decompiled feature behavior, inspected on 2026-10-30. Target: Minecraft 1.21.1 Fabric. The Frostbite-dependent biome and configured/placed features are Fabric resources; the shared module contains only its resource key, configuration flag, noise definition and dormant surface rule.

Implemented:

- A real `minecraft:ice_caves` biome and a separate `has_ice_caves` configuration flag. One Fabric climate point uses temperature `[-1,-0.8]`, humidity `[-1,0.7]`, continentalness `[-1.05,-0.19]`, erosion `[-1,1]`, depth `[0.2,0.9]`, weirdness `[0.5,1.1]`, offset `0`. Existing Pale Garden, Dappled Forest and Sulfur Caves placements are unchanged.
- Snapshot water/fog/sky colors, cave carvers, ordinary hostile spawns, Frostbite weight 50/count 4, Stray weight 10/count 4, bat and glow-squid spawns, and the 1.21.1 equivalents of the biome effects schema.
- Three-dimensional gradient noise at octave -4 with amplitudes `[1,0,1]`. Five inclusive ordered bands produce calcite/packed ice with the snapshot's float thresholds; untouched underground values fall through to native deepslate/stone rules. Bedrock remains intact.
- Sixteen configured ore features and 21 placements. Ordinary ores first place a size-20 stone/deepslate host pocket and then their original configured ore with the same random source and origin. Height selection happens at each visited block (`y >= 0` stone, otherwise deepslate), including tuff. Original ore passes cannot directly replace packed ice/calcite. Gravel size 33 and diorite/andesite size 64 directly extend their replaceable targets instead of creating host pockets.
- True-biome Frostbite/Stray spawn checks, while legacy decorated cold caves require nearby crystals/icicles or mixed packed-ice/calcite walls. The exact eight snapshot surface Frostbite biomes have a separate tag. Ordinary hostiles, spawners and conversions are preserved.
- The existing decoration feature is restricted to the true biome and its ad hoc wall-painting pass is disabled inside it.

Explicit adaptations and remaining limits:

- Ore geometry uses native Minecraft 1.21.1's sphere traversal. The snapshot has a newer column traversal optimization; seed-level block placement is not claimed to match that implementation. The pocket size, order, origin, shared randomness, ore sizes, discard chances, placement modifiers and per-block height choices follow the snapshot.
- Decoration still uses the pinned 1.21.1 backport's snow, crystals, pointed icicles, ice pillars and the reviewed crevasse port. The modern separate `large_icicle`, `ice_crystals`, `snow_layers` and `icicle_cluster` feature implementations/distributions are not fully ported. The biome intentionally does not reference unavailable registry entries. The broad old surface-biome decorator registration must be removed by preparation.
- No broad terrain density, cave noise or biome remapping is performed.

Validation:

- `.github/scripts/test_ice_biome.py [official-client.jar]`: compiles/exercises production climate and surface-rule assembly with Java fixtures, checks exact band endpoints and config behavior, validates complete 1.21.1 biome resources, and optionally compares all ore placements/config values/spawns against the official snapshot JAR.
- `.github/scripts/test_ice_cave_ores.py`: runs the production per-voxel ore traversal across 300 seeded packed-ice/calcite, tuff and protected voxel worlds, testing both passes, host/ore size relationship, y=0 crossing, same RNG/origin, deterministic output and failed-pocket short circuit.
- `.github/scripts/test_ice_cave_spawns.py`: exercises true and legacy cave behavior, exact surface scope, ordinary cave monsters, floor and vanilla light/difficulty checks, spawners and conversions.

The Java fixtures isolate behavior but do not replace the full Fabric/NeoForge build and server startup checks. CI must validate actual target API linkage, merged resource registries and in-game generation.
