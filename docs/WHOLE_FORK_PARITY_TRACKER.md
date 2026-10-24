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
| Mounts of Mayhem | Correct Nautilus equipment-menu index offset so its 2 visible mount slots are followed by all 36 player slots | Validate Nautilus shift-click behavior in game; other combat movement, mob equipment and underwater behavior still need targeted gameplay tests |
| Tiny Takeover | Keep baby sheep geometry untouched | Model dimensions, fox animation, armor-stand helmet scaling, particles require render-state comparisons |
| Chaos Cubed | Sulfur spike growth starter now checks for its own block rather than pointed dripstone; the Fast Sliding archetype accepts both new packed ice item IDs when present (optional on NeoForge) | Sulfur spike maximum height, specialized sounds, cave placement, fog, geyser interactions still need verified parity |
| Wilderness Bound | Golden Dandelion stew now accepts any two items from the preexisting Minecraft mushrooms tag | Dappled Forest leaf litter and fallen-tree distribution need same-seed worldgen comparisons with WWOO |
| Fourth Drop | Fabric Ice Caves: non-slippery ice bricks, vanilla packed-ice loot, Frostbite Ice Ball loot, restored hostile cold-biome spawn lists, Frostbite surface spawns, Ice Crystal self-drop, icicle melting/falling/damage fixes, brittle ice standing detection, Ice Ball impact changes, Freezing thaw compensation, single-chest playlists and negative-index safety | Full status-effect immunity/damage, actual ice-cave biome/crevasse placement, growth models, Frostbite AI/conversion and playlist edge cases still open |

## Upstream issue follow-up (October 24, 2026)

- #513: copper golems explicitly breathe underwater as in vanilla (inherited behavior still needs gameplay verification).
- #469: sheep wool undercoat uses its own model-layer ID, avoiding reusing the main sheep's EMF model slot; Tiny Takeover baby rendering remains untouched.
- #510 / #481: copper torch, lantern and chain recipes use the conventional copper-nugget tag; the generator matches the committed recipes. Added additive c:chains block/item, c:bricks/resin, c:clumps/resin, and c:storage_blocks parent tags for cross-mod compatibility (#95).
- #442: Adventuring Time uses Minecraft 1.21.1 definitions plus the Pale Garden, Dappled Forest and Sulfur Caves criteria from vanilla 26.3 (56 total).
- #443: Monster Hunter and Monsters Hunted use 1.21.1 definitions plus the five new creature criteria from vanilla 1.21.11 (41 total).
- #445: Two by Two requires Nautilus breeding (26 total).
- #398: Happy Ghast ridden movement clamps input to unit length before applying its configured speed, avoiding a diagonal speed boost.
- #444: How Did We Get Here? includes Breath of the Nautilus.
- The advancement replacements live in persistent resources rather than generated resources; a datagen pass must not erase them. They are checked in both CI builds.
- Runtime follow-ups: feature-disabled configurations can intentionally make some parity criteria unreachable; biome-cleaner, WWOO, multiplayer advancements, copper-chest compatibility and all latest-modpack visual tests still require validation.

## VanillaBackport upstream 1.2.1 corrections

Upstream reference commit: `ItsBlackGear/VanillaBackport@88865d0`
(October 23, 2026). The following have been applied on this branch
without changing WWOO climate noise or biome placement:

- Wool/concrete slabs: all 32 generated loot tables and their generator
  count two blocks for double slabs and use vanilla explosion-decay semantics.
- Poplar wood set: flammability and wooden-shelf values registered through the
  cross-loader BlockIntegration API rather than a loader-specific direct map.
- Dried Ghast fossil placement: Nether eligibility determined using
  `BiomeTags.IS_NETHER` at the structure origin instead of dimension flags.
- Mounted mobs: Camel Husk riders and Zombie Horse riders select a vanilla iron
  sword instead of a spear when the spear content toggle is disabled. The
  corresponding Piglin and Zombified Piglin spawn paths also respect it.
- Dyeing: all 64 harness, cushion, wool slab and stair recipes refer to
  `#minecraft:harnesses`, `#minecraft:cushions`,
  `#minecraft:wool_slabs`, or `#minecraft:wool_stairs`. The data generator
  emits tag-based recipes, and the tag files already exist.
- Registry fallback: no longer registers the global duplicate-key and
  duplicate-holder mixins. Invalid duplicates now surface as actual errors;
  server/client startup and world migration still need verification.
- Lunge: no longer inserts duplicate enchanted books into the extra
  Mounts of Mayhem bundled tab; vanilla creative tabs retain responsibility
  for enchanted book displays.
- Dynamic recipes: registration moved to the post-registry setup path, matching
  upstream initialization order.

Static verification: `.github/scripts/check_upstream_121_parity.py` on both
loader jobs. This does **not** imply full in-game testing; the newest complete
modpack is not mounted in this conversation.

## Regression requirements

1. `.github/scripts/test_parity_regressions.py` — static parity guards for
   common gameplay and resource data (Fabric + NeoForge).
2. `.github/scripts/check_ice_caves_integration.py` — packaged Fabric Ice
   Caves contents, new registry IDs and loot/source checks.
3. Existing mixin consistency, dynamic recipes, WWOO placement, packaged
   worldgen, Tiny Takeover integration, dedicated server and WWOO smoke checks.
4. Before merging: no changes to global biome noise, biome cleaner
   compatibility, or WWOO placement except when backed by same-seed fixtures.
5. Earlier Modpack 1.0.1 Library records exist, but raw-byte materialization was denied for them; the full newest pack cannot be staged with current access. **Passing CI is
   not full gameplay parity and does not replace real client testing.**

References used: official Minecraft Java release/snapshot notes, Minecraft
Wiki for the matching game drop, and modern vanilla data definitions. Do not
conflate the official snapshot's features with those from a separate mod.
