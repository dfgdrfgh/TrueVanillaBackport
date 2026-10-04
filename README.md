# TrueVanillaBackport

A vanilla parity fork of [VanillaBackport](https://modrinth.com/mod/vanillabackport).

## Parity changes

- Restore the vanilla 20% nautilus armor pool chance in buried treasure, ocean ruins and shipwrecks.
- Add all eight Mounts of Mayhem spear loot entries to their existing vanilla pools, including enchantments and durability.
- Correct 1.20.1 Lunge direction, exhaustion and maximum enchanting costs.
- Use the vanilla wolf armor damage exceptions on 1.20.1.

Pinned dependencies and their licenses:

- [Platform (MIT)](https://github.com/ItsBlackGear/Platform)

Minecraft assets belong to Mojang and Microsoft. This mod is not affiliated with either company.

### William Wythers' Overhauled Overworld compatibility

On 1.21.1, WWOO keeps its Pale Garden trees, vegetation, terrain, and biome definition. When WWOO is installed, the backport places the Pale Garden in vanilla 1.21.4's plateau-variant slot instead of appending overlapping Dark Forest climate entries. The Pale Garden toggle still applies, and world generation changes apply to new chunks. Fabric and NeoForge builds are tested with WWOO 2.6.7 and Cristel Lib 3.1.5 in a normal world.

Tiny Takeover is no longer bundled or needed to compile this fork. Platform remains bundled.
