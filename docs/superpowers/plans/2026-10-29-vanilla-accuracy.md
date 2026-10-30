# Vanilla accuracy implementation plan

Goal: apply the attached October 29 audit to Minecraft 1.21.1 Fabric, using official 26.3 and 26.4 Snapshot 6 assets and behavior as references.
Architecture: preserve the existing common registrations and integrated Ice Caves build; add focused changes through those interfaces. Keep Platform external. Do not change the user-reverted global creative ordering.
Tech stack: Java 21, Architectury/Fabric, Gradle, Python integration checks.
Spec: user attachment Accuracy Testing.txt, checked against Mojang changelogs and official client resources.

## Global constraints
- Minecraft 1.21.1 only; Fabric is the primary runtime.
- Never claim full parity, seed parity, or client visual verification from source checks.
- Test latest uploaded SMP ModPack 1.0.0.mrpack dated October 25/26, not older files with higher version names.
- Preserve WWOO and existing Pale Garden/Dappled Forest terrain behavior.
- Keep default output JAR filenames and external Platform.

## Review focus
- Data pack reload, existing filled maps and map cloning.
- Blocks mined with wrong tools, doubled slabs, creative entries.
- Brittle Ice with Strays, mob griefing disabled, and protected players.
- Sound assets and resource namespace collisions in merged JAR.
- Existing world generation and pack compatibility.

## Tasks
- [ ] 1. Establish baseline, acquire official source/resources, compare five upstream commits and integrate missing fixes while retaining fork-specific corrections. Run existing source checks and compilation.
- [ ] 2. Implement Snapshot 6 blocks/mechanics with tests: Snow Bricks, tool requirements, Cold archetype and ice sounds. Match official resources and test recipes/loot/registration/runtime.
- [ ] 3. Implement dedicated explorer maps and map resources, cloning and zoom constraints with regression coverage for existing maps.
- [ ] 4. Compare Ice Caves biome and crevasse implementation with official generation; implement only reference-backed changes and exercise normal worlds and WWOO. Record any remaining generation differences explicitly.
- [ ] 5. Build, inspect packaged resources, run production startup/reload/content probes and attempt complete latest-pack testing. Review diff and publish a reviewable PR with exact testing limits.
