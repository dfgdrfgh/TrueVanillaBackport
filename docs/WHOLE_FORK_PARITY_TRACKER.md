# Whole-fork vanilla parity tracking (Fabric/NeoForge 1.21.1)

Baseline: Minecraft Java 1.21.2, 1.21.4, 1.21.5, 1.21.6–1.21.7,
1.21.9, 1.21.11, 26.1, 26.2, 26.3 and **26.4 Snapshot 5** as
appropriate to each bundled section. This is not an assertion of complete
vanilla parity. Each item needs runtime verification before closing.

## Audit and implementation status

| Section | Implemented in the current PR | Still open for validation/correction |
| --- | --- | --- |
| Bundles of Bravery | Holding Use now starts a use-duration window and ejects successive bundles of items at eight-tick intervals while held; manual single click retained | Verify client/server held-use packet behavior and selected-stack order in the full modpack |
| The Garden Awakens | Eyeblossom only poisons bees when open and does not switch state in Nether/End | Bee attraction/feeding, creaking/heart/resin behavior, snapshot day/night transitions and full runtime validation |
| Spring to Life | Firefly light check corrected; fireflies now survive non-solid blocks; undercoat leaves Tiny Takeover babies alone; fallen-tree support check corrected | Remaining fallen-tree frequency/selection, wolf armor sounds, adult sheep render comparisons and particles in game |
| Chase the Skies | Harness data components preserved; guaranteed equipped-harness drop chance; sneaking bypasses Happy Ghast mounting | Verify all equipment drops after save/reload, passenger seat selection and interaction edge cases |
| Hot as Lava | Dennis painting variant now includes newer title and author JSON metadata | Validate actual 1.21.1 painting tooltip rendering; metadata may require a separate runtime backport. Disc behavior confirmed separately, not modified here |
| Copper Age | Copper Golem path-height comparison fixed; comparator only reads shelf from its back via narrowly targeted mixin; waxed construction preserves all four copper chest stages | Validate inventory conversions, wax state in-game, and comparator-through-solid-block cases |
| Mounts of Mayhem | No speculative changes | Zombie spear replacement, six dismount thresholds, Lunge in water, nautilus shift-click bounds require controlled tests |
| Tiny Takeover | Keep baby sheep geometry untouched | Model dimensions, fox animation, armor-stand helmet scaling, particles require render-state comparisons |
| Chaos Cubed | No speculative changes | Sulfur spike placement/growth/sounds, cave placement, fog, geyser interactions need fixture worlds and client/server checks |
| Wilderness Bound | No worldgen changes | Dappled Forest leaf litter and fallen-tree distribution need same-seed worldgen comparisons with WWOO |
| Fourth Drop | Fabric Ice Caves: ice bricks friction, Frostbite Ice Ball loot, vanilla packed ice loot, ordinary cave monster spawning, icicle light threshold, single-chest playlists, safe negative index | Freezing effect/immunity, real ice cave biome/crevasse placement, brittle ice, Frostbite AI/conversion, other playlist edge cases |

## Regression requirements

1. `.github/scripts/test_parity_regressions.py` — static parity guards for
   common gameplay and resource data (Fabric + NeoForge).
2. `.github/scripts/check_ice_caves_integration.py` — packaged Fabric Ice
   Caves contents, new registry IDs and loot/source checks.
3. Existing mixin consistency, dynamic recipes, WWOO placement, packaged
   worldgen, Tiny Takeover integration, dedicated server and WWOO smoke checks.
4. Before merging: no changes to global biome noise, biome cleaner
   compatibility, or WWOO placement except when backed by same-seed fixtures.
5. Full newest user modpack wasn't available with this task. **Passing CI is
   not full gameplay parity and does not replace real client testing.**

References used: official Minecraft Java release/snapshot notes, Minecraft
Wiki for the matching game drop, and modern vanilla data definitions. Do not
conflate the official snapshot's features with those from a separate mod.
