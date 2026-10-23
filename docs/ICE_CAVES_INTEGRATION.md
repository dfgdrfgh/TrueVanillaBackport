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
