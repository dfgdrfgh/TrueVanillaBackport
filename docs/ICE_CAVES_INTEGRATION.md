# Ice Caves (Fabric 1.21.1) integration
The 26.4 Ice Caves section is appended after Wilderness Bound in the bundled creative tab.
Items are drawn from the pinned merged Ice-Caves-Backport module and ordered by
decorative blocks, Ice Ball, Frostbite egg, then normal/extended potions and arrows.

The official 1.21.1 Fabric Loader nested-JAR mechanism packages the Ice Caves module
*inside* the final TrueVanillaBackport Fabric JAR, so no second download is required.
The embedded module retains the `ice_caves` mod ID, which also prevents accidentally
loading a second copy of the same mod.

This does NOT import Bananaman's problematic global climate or TerraBlender hooks.
The Ice Caves biome is still decorated cold caves, not an independent vanilla 26.4 biome.
For now NeoForge does not carry Ice Caves content; the creative section is inert there.

Do not confuse a build passing with full-pack gameplay testing. The latest complete
user modpack is not available in this session. Existing WWOO seed/worldgen checks
and server smoke tests must continue to pass.
