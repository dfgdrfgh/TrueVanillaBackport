"""Guard 1.21.1 advancement overrides extended to vanilla 1.21.11 / 26.3.
The vanilla 1.21.1 JSON format is intentionally retained for parser compatibility.
This is a data parity check, not a full in-game advancement test.
"""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[2]
adv = root / "common/src/main/resources/data/minecraft/advancement"

def read(name):
    return json.loads((adv / f"{name}.json").read_text(encoding="utf8"))

biomes = read("adventure/adventuring_time")
for key in ("pale_garden", "sulfur_caves", "dappled_forest"):
    ident = f"minecraft:{key}"
    assert ident in biomes["criteria"]
    assert [ident] in biomes["requirements"]
    assert biomes["criteria"][ident]["trigger"] == "minecraft:location"
assert len(biomes["criteria"]) == len(biomes["requirements"]) == 56

new_mobs = ("camel_husk", "creaking", "parched", "zombie_horse", "zombie_nautilus")
any_kill = read("adventure/kill_a_mob")
all_kill = read("adventure/kill_all_mobs")
for name in new_mobs:
    ident = f"minecraft:{name}"
    assert ident in any_kill["criteria"] and ident in all_kill["criteria"]
    assert [ident] in all_kill["requirements"]
    assert any_kill["criteria"][ident]["trigger"] == "minecraft:player_killed_entity"
assert len(any_kill["criteria"]) == len(all_kill["criteria"]) == 41
assert len(any_kill["requirements"]) == 1
assert set(any_kill["requirements"][0]) == set(any_kill["criteria"])
assert len(all_kill["requirements"]) == 41

animals = read("husbandry/bred_all_animals")
assert "minecraft:nautilus" in animals["criteria"]
assert ["minecraft:nautilus"] in animals["requirements"]
assert len(animals["criteria"]) == len(animals["requirements"]) == 26

effects = read("nether/all_effects")
assert "minecraft:breath_of_the_nautilus" in effects["criteria"]["all_effects"]["conditions"]["effects"]
assert len(effects["criteria"]["all_effects"]["conditions"]["effects"]) == 34
print("PASS: vanilla 1.21.11 / 26.3 advancement parity checks")
