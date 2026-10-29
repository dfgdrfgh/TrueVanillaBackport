import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
TAGS = ROOT / 'common/src/main/generated/data/minecraft/tags/item'

class ColdArchetypeResources(unittest.TestCase):
    def test_blue_ice_has_only_cold_archetype(self):
        cold = json.loads((TAGS / 'sulfur_cube_archetype/cold.json').read_text())['values']
        self.assertEqual(cold, ['minecraft:blue_ice'])
        fast = json.loads((TAGS / 'sulfur_cube_archetype/fast_sliding.json').read_text())['values']
        self.assertNotIn('minecraft:blue_ice', fast)
        self.assertIn('minecraft:snow_bricks', fast)
        for path in TAGS.joinpath('sulfur_cube_archetype').glob('*.json'):
            if path.stem != 'cold':
                self.assertNotIn('minecraft:blue_ice', json.loads(path.read_text())['values'], path)
        self.assertIn('#minecraft:sulfur_cube_archetype/cold', json.loads((TAGS / 'sulfur_cube_swallowable.json').read_text())['values'])
