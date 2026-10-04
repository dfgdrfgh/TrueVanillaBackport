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

On 1.21.1, WWOO can overhaul the older Overworld biomes while the backport retains its Pale Garden, Dappled Forest, and Sulfur Caves definitions. The compatibility handler only replaces a WWOO override of those three biome resources; custom world datapacks still take priority. World generation changes apply to newly generated chunks. Fabric and NeoForge builds are checked with WWOO 2.6.7 and Cristel Lib 3.1.5 in a normal world.
