"""Check installable JARs, not just Java sources, for the WWOO seed-parity hook.

The October 2026 regression shipped the compatibility class but omitted the
mixin from vanillabackport.mixins.json. Compilation and source-only tests could
pass while the actual installed JAR changed biome placements.
"""
import json
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
jars = sorted((root / "dist").glob("*.jar"))
assert len(jars) == 1, f"Expected exactly one release JAR: {jars}"

with zipfile.ZipFile(jars[0]) as jar:
    names = set(jar.namelist())
    manifest = json.loads(jar.read("vanillabackport.mixins.json"))
    entry = "compat.wwoo.OverworldBiomeBuilderMixin"
    assert manifest["mixins"].count(entry) == 1, (
        f"WWOO biome parity mixin missing or duplicated in assembled {jars[0]}"
    )
    for clazz in (
        "com/blackgear/vanillabackport/core/mixin/compat/wwoo/OverworldBiomeBuilderMixin.class",
        "com/blackgear/vanillabackport/common/integrations/compat/wwoo/WwooBiomeCompatibility.class",
        "com/blackgear/vanillabackport/common/integrations/worldgen/BiomeGeneration.class",
    ):
        assert clazz in names, f"Packaged class missing from {jars[0]}: {clazz}"

print("PASS: assembled release contains the original WWOO climate-selection hook and all required classes")
