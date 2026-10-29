"""Snapshot 6 Snow Bricks contracts, adapted to the 1.21.1 resource codecs.

Run with: python -m unittest discover -s tests -v
The values below come from the official 26.4-snapshot-6 client resources.
"""
import json
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
GENERATED = ROOT / "common/src/main/generated"
NAMES = ("snow_bricks", "snow_brick_stairs", "snow_brick_slab", "snow_brick_wall")


class SnowBricksTests(unittest.TestCase):
    def read(self, path):
        full = GENERATED / path
        self.assertTrue(full.is_file(), f"Missing Snow Bricks resource: {path}")
        return json.loads(full.read_text())

    def test_crafting_uses_snow_blocks_and_vanilla_shape_yields(self):
        expected = {
            "snow_bricks": (["##", "##"], "snow_block", 4),
            "snow_brick_stairs": (["#  ", "## ", "###"], "snow_bricks", 4),
            "snow_brick_slab": (["###"], "snow_bricks", 6),
            "snow_brick_wall": (["###", "###"], "snow_bricks", 6),
        }
        for name, (pattern, ingredient, count) in expected.items():
            with self.subTest(name=name):
                recipe = self.read(f"data/minecraft/recipe/{name}.json")
                self.assertEqual(recipe["type"], "minecraft:crafting_shaped")
                self.assertEqual(recipe["pattern"], pattern)
                self.assertEqual(recipe["key"], {"#": {"item": f"minecraft:{ingredient}"}})
                self.assertEqual(recipe["result"], {"count": count, "id": f"minecraft:{name}"})
        for path in (GENERATED / "data/minecraft/recipe").glob("*.json"):
            recipe = json.loads(path.read_text())
            if recipe.get("type") == "minecraft:stonecutting":
                self.assertNotIn(recipe.get("result", {}).get("id"), {f"minecraft:{n}" for n in NAMES})

    def test_recipe_unlocks_use_1_21_1_item_predicate_lists(self):
        for name in NAMES:
            category = "decorations" if name.endswith("wall") else "building_blocks"
            advancement = self.read(f"data/minecraft/advancement/recipes/{category}/{name}.json")
            ingredient = "snow_block" if name == "snow_bricks" else "snow_bricks"
            self.assertEqual(advancement["criteria"][f"has_{ingredient}"]["conditions"]["items"],
                             [{"items": [f"minecraft:{ingredient}"]}])
            self.assertEqual(advancement["rewards"]["recipes"], [f"minecraft:{name}"])

    def test_loot_drops_self_and_double_slab_drops_two(self):
        for name in NAMES:
            loot = self.read(f"data/minecraft/loot_table/blocks/{name}.json")
            pool = loot["pools"][0]
            self.assertEqual(pool["rolls"], 1)
            entry = pool["entries"][0]
            self.assertEqual(entry["name"], f"minecraft:{name}")
            if name.endswith("slab"):
                count, decay = entry["functions"]
                self.assertEqual(count["function"], "minecraft:set_count")
                self.assertEqual(count["count"], 2)
                self.assertEqual(count["conditions"], [{"block": f"minecraft:{name}",
                    "condition": "minecraft:block_state_property", "properties": {"type": "double"}}])
                self.assertEqual(decay, {"function": "minecraft:explosion_decay"})
            else:
                self.assertEqual(pool["conditions"], [{"condition": "minecraft:survives_explosion"}])

    def test_shovel_and_shape_tags(self):
        shovel = self.read("data/minecraft/tags/block/mineable/shovel.json")["values"]
        self.assertTrue({f"minecraft:{n}" for n in NAMES}.issubset(shovel))
        pickaxe = self.read("data/minecraft/tags/block/mineable/pickaxe.json")["values"]
        self.assertFalse({f"minecraft:{n}" for n in NAMES}.intersection(pickaxe))
        for shape in ("slab", "stairs", "wall"):
            for registry in ("block", "item"):
                tag = self.read(f"data/minecraft/tags/{registry}/{shape if shape == 'stairs' else shape + 's'}.json")
                self.assertIn(f"minecraft:snow_brick_{shape}", tag["values"])

    def test_models_cover_all_states_and_legacy_item_models(self):
        for name in NAMES:
            self.read(f"assets/minecraft/blockstates/{name}.json")
            item = self.read(f"assets/minecraft/models/item/{name}.json")
            self.assertEqual(item["parent"], f"minecraft:block/{name}" + ("_inventory" if name.endswith("wall") else ""))
        slab = self.read("assets/minecraft/blockstates/snow_brick_slab.json")["variants"]
        self.assertEqual(set(slab), {"type=bottom", "type=top", "type=double"})
        self.assertEqual(slab["type=double"]["model"], "minecraft:block/snow_bricks")
        stairs = self.read("assets/minecraft/blockstates/snow_brick_stairs.json")["variants"]
        self.assertEqual(len(stairs), 40)
        wall = self.read("assets/minecraft/blockstates/snow_brick_wall.json")["multipart"]
        self.assertEqual(len(wall), 9)
        for name in ("snow_bricks", "snow_brick_slab", "snow_brick_slab_top", "snow_brick_stairs",
                     "snow_brick_stairs_inner", "snow_brick_stairs_outer", "snow_brick_wall_inventory",
                     "snow_brick_wall_post", "snow_brick_wall_side", "snow_brick_wall_side_tall"):
            model = self.read(f"assets/minecraft/models/block/{name}.json")
            self.assertEqual(set(model["textures"].values()), {"minecraft:block/snow_bricks"})
        texture = ROOT / "common/src/main/resources/assets/minecraft/textures/block/snow_bricks.png"
        self.assertTrue(texture.is_file(), "Missing official Snow Bricks texture")


if __name__ == "__main__":
    unittest.main()
