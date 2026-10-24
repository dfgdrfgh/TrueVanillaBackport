# Integrated Ice Caves — Fabric 1.21.1

The combined Ice Caves gameplay implementation is compiled from a pinned copy
of [Ice-Caves-Backport](https://github.com/dfgdrfgh/Ice-Caves-Backport)
during CI and **merged into TrueVanillaBackport's outer Fabric JAR**. It is
not loaded as a separately identified, nested Fabric mod anymore.

The installed mod ID is `vanillabackport`. It declares `ice_caves` under
Fabric's `provides` aliases for compatibility. There is no additional Ice Caves
JAR to install. Do not install the old Ice Caves backport alongside this build;
it registers overlapping features.

The merged classes and resources are initialized through VanillaBackport's own
`fabric.mod.json` entrypoints and `ice_caves.mixins.json`. When running
inside VanillaBackport, the old standalone Ice Caves creative tab is disabled;
the content instead appears in VanillaBackport's unified tab and its **Ice Caves**
sidebar section. The order is ice materials, packed-ice building variants,
Ice Ball, Frostbite egg, then potion and arrow variants (normal before extended).

The Ice Caves gameplay remains Fabric-only for now; the NeoForge branch is not
silently given incompatible compiled Fabric code.

**World-generation safety:** No Bananaman global biome climate-map hooks,
TerraBlender injections, or external noise settings were copied. The existing
Ice Cave decoration mechanism is retained and the WWOO tests remain active.

**Build dependencies are not runtime dependencies:** CI still uses the pinned
source repository to compile Ice Caves. The resulting single installable mod JAR
contains its compiled classes/assets directly. Source has not yet been converted
to TrueVanillaBackport's shared Mojang-mapped common module.

Client creative UI and the full most-recent modpack still require in-game
validation; no such testing is claimed.

## Vanilla 26.4 ID and inventory normalization

The Fabric release now builds Ice Caves with `minecraft:` resource identifiers
for blocks, items, potions, effects, entities and worldgen features. All Ice Door
registrations, models, tags, and recipes are excluded. The external standalone
Ice Caves checkout is transformed **only in CI** before compilation; unchanged
source remains available upstream.

Minecraft's Natural Blocks, Building Blocks, Combat, Spawn Eggs, and Food &
Drinks tabs use anchored Fabric creative-tab entries. The existing VanillaBackport
Ice Caves filter is retained and lists the same items without an Ice Door.

**Existing worlds:** Previously placed `ice_caves:` block/entity/item IDs are
not automatically remapped. Back up old saves and migrate content IDs before
loading important worlds made with the earlier experimental release.

## Snapshot 5 parity pass (Fabric only)

The pinned Ice Caves source is adjusted during the existing CI preparation step.
These changes apply **only** to the merged Fabric release:

- Packed Ice Bricks and all related variants use normal block friction rather
  than inheriting the slippery ice setting.
- Ordinary cave monsters keep vanilla spawning rules inside decorated ice cave
  areas; Frostbite cave-only restrictions remain.
- The extra non-vanilla Ice Ball drop is removed by omitting the bundled
  override of Minecraft's packed ice loot table.
- Icicles check for melting at block light level 5, in line with 26.4.

The packed-ice loot-table removal is intentional: regular packed ice continues
to use Minecraft's built-in loot table. No global climate noise, biome selector,
or WWOO placement changes are made by this pass.

The packaged-JAR integration check covers these source invariants. Dedicated
server and WWOO smoke tests remain in the build workflow, but a passing CI
build alone does not establish complete 26.4 parity or full modpack coverage.
