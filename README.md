# TrueVanillaBackport

A vanilla parity fork of [VanillaBackport](https://modrinth.com/mod/vanillabackport).
The Minecraft registry namespace and mod ID remain compatible with the original mod.

## Parity changes

- Restore the vanilla 20% nautilus armor pool chance in buried treasure, ocean ruins and shipwrecks.
- Add all eight Mounts of Mayhem spear loot entries to their existing vanilla pools, including enchantments and durability.
- Correct 1.20.1 Lunge direction, exhaustion and maximum enchanting costs.
- Use the vanilla wolf armor damage exceptions on 1.20.1.
- Bundle the patched Tiny Takeover implementation: golden dandelions, growth locking, name tag crafting, baby models and sounds, animal sound variants, aquatic babies and trumpet note blocks.

## Builds

Version 1.2.0.2638.7 fixes nautilus saddle slots appearing empty after reopening the inventory. The client opens the menu before applying the following slot-content update, with a client-thread handoff only when needed. A focused packet-ordering regression check reproduces the previous failure and covers repeated reopening, empty saddle slots, off-thread delivery and disconnects.

Version 1.2.0.2638.6 adds a Tiny Takeover sub-tab inside the mod's creative tab, between Mounts of Mayhem and Chaos Cubed. It uses a golden dandelion icon and contains golden dandelions and name tags.

Version 1.2.0.2638.5 reduces temporary allocations during nautilus, creaking and copper golem animation resets, bonemeal neighbor checks, and copper golem target visibility checks. Model part references are retained only for the lifetime of each baked model. Direction order, raycasts, random calls and animation calculations are preserved. These changes have not been benchmarked for an FPS or tick-time claim.

The Build parity backport GitHub Actions workflow builds each loader from pinned source revisions.
Its artifacts contain installable TrueVanillaBackport jars with Platform and Tiny Takeover embedded using the loader's jar-in-jar format, plus SHA-256 checksums.
Fabric still requires Fabric API. The Tiny Takeover JSON settings retain their upstream defaults; its optional configuration GUI is omitted from the embedded build.

Pinned dependencies and their licenses:

- [Platform (MIT)](https://github.com/ItsBlackGear/Platform)
- [Tinier Takeover fork (MIT)](https://github.com/dfgdrfgh/Tinier-Takeover-Backport/tree/accd6d7ec828160cc1723256280baaa3d1a8829a)

Minecraft assets belong to Mojang and Microsoft. This mod is not affiliated with either company.
