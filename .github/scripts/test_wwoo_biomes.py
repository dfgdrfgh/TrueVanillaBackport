"""Regression checks for 1.21.1 world-generation parity with the pre-sync fork.

The production WWOO normal-world server smoke test also verifies that the Pale
Garden can be located. These source-level checks protect the optional-mod
boundary and all three backported biome placement hooks against regressions.
"""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
src = (root / "common/src/main/java/com/blackgear/vanillabackport/common/integrations/worldgen/BiomeGeneration.java").read_text()
mixins = json.loads((root / "common/src/main/resources/vanillabackport.mixins.json").read_text())
setup = (root / "common/src/main/java/com/blackgear/vanillabackport/common/CommonSetup.java").read_text()
configs = (root / "common/src/main/java/com/blackgear/vanillabackport/common/CommonConfig.java").read_text()

# The native plateau overwrite is not seed-preserving: it rewrites the Dark
# Forest climate mapping and previously stopped the original Pale Garden
# placement whenever WWOO was loaded.
assert "compat.wwoo.OverworldBiomeBuilderMixin" not in mixins["mixins"]
assert "WwooBiomeCompatibility" not in src
assert "ModChecker.WWOO" not in src
assert "useNativePaleGardenPlacement" not in src

for flag, field, expression in [
    ("pale_garden", "PALE_GARDEN", "PALE_GARDEN.forEach(event::add)"),
    ("has_sulfur_caves", "SULFUR_CAVES", "event.add(SULFUR_CAVES)"),
    ("has_dappled_forest", "DAPPLED_FOREST", "DAPPLED_FOREST.forEach(event::add)"),
]:
    assert f'.define("{flag}", true)' in configs, f"Default disabled: {flag}"
    assert expression in src, f"Missing biome mapping: {field}"
    # Ensure the mapping is still gated by its own config, not by WWOO detection.
    offset = src.index(expression)
    before = src[max(src.index("public static void bootstrap"), offset-160):offset]
    assert "COMMON_CONFIG" in before, f"No config check for {field}"

assert "BiomePlacement.registerBiomePlacements(BiomeGeneration::bootstrap)" in setup
assert re.search(r"if \(VanillaBackport\.COMMON_CONFIG\.hasPaleGarden\.get\(\)\)\s*PALE_GARDEN\.forEach", src)
assert re.search(r"if \(VanillaBackport\.COMMON_CONFIG\.hasSulfurCaves\.get\(\)\)\s*event\.add\(SULFUR_CAVES\)", src)
assert re.search(r"if \(VanillaBackport\.COMMON_CONFIG\.hasDappledForest\.get\(\)\)\s*DAPPLED_FOREST\.forEach", src)
print("PASS: original 1.21.1 Pale Garden/Dappled Forest/Sulfur Caves placements and config gates retained; WWOO climate overwrite inactive")
