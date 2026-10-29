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

On 1.21.1, this fork restores its *actual earlier* WWOO compatibility behavior from the working October 11 revision (`3fafeea`): when WWOO is loaded, Pale Garden occupies the native 1.21.4 Dark Forest plateau variant instead of inserting extra climate points; without WWOO, it retains its original Pale Garden placements. The previous attempt to disable this hook was mistaken and has been undone. Sulfur Caves and Dappled Forest still use their independent config-gated placement entries. This restores the original fork's biome-selection logic but does not yet prove identical terrain across all modpack versions; other surface-rule and Platform changes require separate investigation. World-generation behavior affects new chunks. Automated normal-world smoke tests use WWOO 2.6.7 and Cristel Lib 3.1.5, **not** the newer WWOO 3.0.1 backport or the complete user modpack.

Tiny Takeover content is integrated directly into VanillaBackport, including its baby models, Golden Dandelion, sounds, recipes, and creative tab. No separate Tiny Takeover mod or build dependency is required. Its original resource IDs and `tiny_takeover_backport.json` settings are preserved. **Platform is no longer bundled.** Install [Platform](https://modrinth.com/mod/platform) for Minecraft 1.21.1 matching your loader as a separate required mod (alongside Fabric API for Fabric).

The integrated code and assets are based on Tiny Takeover Backport commit `accd6d7ec828160cc1723256280baaa3d1a8829a`, with credit to Evan Bowness under the MIT license included at `common/src/main/resources/META-INF/licenses/tiny-takeover.txt`.

### Updated vanilla visual assets (Minecraft 1.21.1)

Shared Fabric/NeoForge resources now include updated vanilla map and filled-map items, 10 banner pattern icons, redstone torch/repeater/comparator textures, and the current cloud texture. The item textures and cloud texture come from Minecraft 26.3. The redstone block models use Minecraft 1.21.2 geometry to stay compatible with the 1.21.1 model format; all four comparator states, 16 repeater states, lit/unlit redstone torches, and their supporting templates are included.

The filled map uses the modern single-layer item model to avoid combining the updated icon with obsolete map-markings artwork. These are resource/model backports, not registrations of post-1.21.1 banner-pattern items or changes to redstone mechanics. Asset provenance and repeatable imports are recorded in `.github/workflows/import-modern-vanilla-textures.yml`.
