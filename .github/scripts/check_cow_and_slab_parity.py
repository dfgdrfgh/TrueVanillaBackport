"""Validate the Spring to Life cow renderer and all 32 slab loot tables."""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
render = (root / "common/src/main/java/com/blackgear/vanillabackport/client/api/modules/mob_rendering/CowVariantRenderer.java").read_text()
babies = (root / "common/src/main/java/com/evandev/tiny_takeover_backport/client/ModBabyTextureRegistry.java").read_text()
generator = (root / "fabric/src/main/java/com/blackgear/vanillabackport/data/server/loot/BlockLootGenerator.java").read_text()

assert "new CowVariantModel<>(context.bakeLayer(ModModelLayers.TEMPERATE_COW))" in render
assert "textures/entity/cow/temperate_cow.png" in render
assert 'case "textures/entity/cow/temperate_cow.png"' in babies
assert "textures/entity/cow/cow_temperate_baby.png" in babies

tables = sorted((root / "common/src/main/generated/data/minecraft/loot_table/blocks").glob("*_slab.json"))
matched = [p for p in tables if re.fullmatch(r"(?:[a-z_]+_)?(?:wool|concrete)_slab\.json", p.name)]
assert len(matched) == 32, len(matched)
for path in matched:
    ident = path.stem
    expected = "minecraft:" + ident
    obj = json.loads(path.read_text())
    entries = obj["pools"][0]["entries"]
    assert len(entries) == 1 and entries[0]["name"] == expected, path
    effects = entries[0]["functions"]
    assert any(
        effect["function"] == "minecraft:set_count"
        and effect["count"] == 2
        and any(cond.get("condition") == "minecraft:block_state_property"
                and cond.get("block") == expected
                and cond.get("properties", {}).get("type") == "double"
                for cond in effect.get("conditions", []))
        for effect in effects
    ), path
    key = ident.upper()
    assert f"this.add(ModBlocks.{key}.get(), this::createSlabItemTable);" in generator, path
    assert f"this.dropSelf(ModBlocks.{key}.get());" not in generator, path
print("PASS: temperate adult/baby cow assets and 32 double-slab loot tables + generator")
