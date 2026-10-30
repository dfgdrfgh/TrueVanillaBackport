"""Snapshot 6 visual contracts for the build-time Ice Caves resource overlay."""
import hashlib
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
OVERLAY = ROOT / ".github/ice-caves-resource-overrides/assets/ice_caves"
COMMON = ROOT / "common/src/main/resources/assets/minecraft"


class IceVisualTests(unittest.TestCase):
    def read(self, base, path):
        source = base / path
        self.assertTrue(source.is_file(), f"Missing snapshot visual correction: {source}")
        return json.loads(source.read_text())

    def test_official_texture_bytes(self):
        # SHA-256 of the authoritative 26.4-snapshot-6 client assets.
        hashes = {
            "textures/block/ice_crystal.png": "aaf7ac729a2de5c896dbe444309fddb4ca947d599d20ac92522bccaeecb597f7",
            "textures/block/icicle_down_base.png": "dfe49a23522f8e593b6c6bd1dd953a5ce1f1255153a6554472b7ff75055dde5a",
            "textures/block/icicle_down_frustum.png": "2909f2ef65844f5773a65c3123af95c75ebc62769798e0c8f964345b6cfc6169",
            "textures/block/icicle_down_middle.png": "cb881581f8bf1a58d9871c7be4802875fac9cae5f74c3cbe9b1c0171a5c0423a",
            "textures/block/icicle_down_tip.png": "047e4eeb3e92f8d82791f367e0205e8f015db138dcf22604cf68d660a8ce8e76",
            "textures/block/icicle_down_tip_merge.png": "ebb07ec76d99231cf30129114af9ab45e888caa1f534e53690dce229c56f64ae",
            "textures/block/packed_ice_bricks.png": "aaab227e8bbd92378ed516f2084275584d777a7bf6963bb31758d5863939d49f",
            "textures/entity/frostbite.png": "31c4436f04d440281600dd32ddb00e7723eed8ca5f142e4f25df92871ba9f5f1",
            "textures/entity/frostbite_baby.png": "23cfc1c622df7344cfe183e1c498983980f0e7c93fc54a07fa8b5794a50c76d6",
            "textures/entity/frostbite_outer_layer.png": "c340535e8c9b74017f993c8cbe5b7e921a140fa21a6f404ecb77e5da879703e0",
            "textures/item/frostbite_spawn_egg.png": "8713256620104706e59cd5d1372ee068c7c6c0460ac2a5d09a2fe2a63d4e53ca",
            "textures/item/ice_ball.png": "f352c143907e7c991c8fd88236bc8eef2047bfaf53296259605d6d1af962c8e6",
            "textures/item/ice_crystal.png": "aaf7ac729a2de5c896dbe444309fddb4ca947d599d20ac92522bccaeecb597f7",
            "textures/item/icicle.png": "047e4eeb3e92f8d82791f367e0205e8f015db138dcf22604cf68d660a8ce8e76",
            "textures/mob_effect/freezing.png": "bc11a1871bb0afa7c6acf7de23d840d4a2d01a7a941a6b95683076f32b727ea2",
        }
        for path, digest in hashes.items():
            with self.subTest(path=path):
                source = OVERLAY / path
                self.assertTrue(source.is_file(), f"Missing official texture: {path}")
                self.assertEqual(hashlib.sha256(source.read_bytes()).hexdigest(), digest)

    def test_icicle_geometry_and_1_21_rotations(self):
        model = self.read(OVERLAY, "models/block/template_icicle.json")
        self.assertFalse(model["ambientocclusion"])
        a, b = model["elements"]
        self.assertEqual((a["from"], a["to"]), ([-2.182, 0, 8], [18.182, 16, 8]))
        self.assertEqual((b["from"], b["to"]), ([8, 0, -2.182], [8, 16, 18.182]))
        self.assertEqual(a["rotation"], {"origin": [8, 8, 8], "axis": "y", "angle": 45})
        self.assertFalse(a["shade"])
        variants = self.read(OVERLAY, "blockstates/icicle.json")["variants"]
        self.assertEqual(len(variants), 10)
        for thickness in ("base", "frustum", "middle", "tip", "tip_merge"):
            up = variants[f"thickness={thickness},vertical_direction=up"]
            self.assertEqual((up["x"], up["y"]), (180, 180))
            self.assertEqual(up["model"], f"ice_caves:block/icicle_down_{thickness}")
        for path in OVERLAY.rglob("*.json"):
            text = path.read_text()
            self.assertNotIn('"shade_direction_override"', text, path)
            if path.parent.name == "blockstates":
                for state in json.loads(text).get("variants", {}).values():
                    self.assertNotIn("z", state, path)

    def test_crystal_item_has_dedicated_item_texture(self):
        block = self.read(OVERLAY, "models/block/ice_crystal.json")
        self.assertEqual(block, {"parent": "minecraft:block/cross", "textures": {"cross": "ice_caves:block/ice_crystal"}})
        item = self.read(OVERLAY, "models/item/ice_crystal.json")
        self.assertEqual(item["textures"], {"layer0": "ice_caves:item/ice_crystal"})

    def test_brittle_ice_uses_all_four_ages(self):
        variants = self.read(OVERLAY, "blockstates/brittle_ice.json")["variants"]
        self.assertEqual(variants, {f"age={age}": {"model": f"ice_caves:block/brittle_ice_{age}"} for age in range(4)})

    def test_jukebox_has_separate_bottom_and_official_textures(self):
        model = self.read(COMMON, "models/block/jukebox.json")
        self.assertEqual(model, {"parent": "minecraft:block/cube_bottom_top", "textures": {
            "bottom": "minecraft:block/jukebox_bottom", "side": "minecraft:block/jukebox_side", "top": "minecraft:block/jukebox_top"}})
        for name, digest in {
            "bottom": "55c0bd5c5aa1417205ac6c028971c226fb6b2a619b3da6a7ba2754e9d7bfdf4b",
            "side": "0ef04269bc228d4f455e65fa78af9756000289cac5a36de4eb9defb05671f38f",
            "top": "2b5c5e9e7519c1326f69a5e6dfb475c30ff3af17c6416db7f559e7c572b79b41",
        }.items():
            source = COMMON / f"textures/block/jukebox_{name}.png"
            self.assertTrue(source.is_file())
            self.assertEqual(hashlib.sha256(source.read_bytes()).hexdigest(), digest)


if __name__ == "__main__":
    unittest.main()
