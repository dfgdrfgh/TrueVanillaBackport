"""26.4 loot semantics expressed in Minecraft 1.21.1's loot schema."""
import json
from pathlib import Path
import unittest

DATA = Path(__file__).resolve().parents[1] / ".github/ice-caves-resource-overrides/data"


class IceLootTests(unittest.TestCase):
    def test_crystal_drops_itself_without_silk_touch(self):
        table = json.loads((DATA / "ice_caves/loot_table/blocks/ice_crystal.json").read_text())
        self.assertEqual(table["pools"], [{
            "rolls": 1,
            "conditions": [{"condition": "minecraft:survives_explosion"}],
            "entries": [{"type": "minecraft:item", "name": "ice_caves:ice_crystal"}],
        }])

    def test_packed_ice_has_only_its_silk_touch_drop(self):
        table = json.loads((DATA / "minecraft/loot_table/blocks/packed_ice.json").read_text())
        self.assertEqual(len(table["pools"]), 1)
        pool = table["pools"][0]
        self.assertEqual(pool["entries"], [{"type": "minecraft:item", "name": "minecraft:packed_ice"}])
        self.assertEqual(pool["conditions"], [{"condition": "minecraft:match_tool", "predicate": {
            "predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}
        }}])
        self.assertNotIn("ice_ball", json.dumps(table))


if __name__ == "__main__":
    unittest.main()
