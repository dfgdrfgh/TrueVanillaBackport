"""Exercise the installable-JAR merge, including optional registry tag entries."""
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
import zipfile

SCRIPT = Path(__file__).resolve().parents[1] / ".github/scripts/bundle.py"
TEXTURE = "assets/minecraft/textures/block/jukebox_side.png"
TAG = "data/minecraft/tags/worldgen/biome/is_overworld.json"


class BundleResourcesTests(unittest.TestCase):
    def bundle(self, incoming):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        root = Path(directory.name)
        script = root / ".github/scripts/bundle.py"
        script.parent.mkdir(parents=True)
        shutil.copyfile(SCRIPT, script)
        files = {
            "fabric/build/libs/VanillaBackport-fabric-1.21.1-test.jar": {
                "fabric.mod.json": json.dumps({"depends": {"platform": "*"}}),
                TEXTURE: b"official-texture",
                TAG: json.dumps({"values": ["minecraft:plains", {"id": "minecraft:ice_caves", "required": False}]}),
            },
            "vendor/ice-caves/build/libs/ice-caves-backport-fabric-test.jar": {
                "fabric.mod.json": json.dumps({"id": "ice_caves", "depends": {"minecraft": "~1.21.1"}}),
                **incoming,
            },
        }
        for name, entries in files.items():
            path = root / name
            path.parent.mkdir(parents=True)
            with zipfile.ZipFile(path, "w") as archive:
                for entry, content in entries.items():
                    archive.writestr(entry, content)
        result = subprocess.run([sys.executable, str(script), "1.21.1", "fabric"], capture_output=True, text=True)
        return root, result

    def test_identical_shared_texture_is_kept_once(self):
        root, result = self.bundle({TEXTURE: b"official-texture"})
        self.assertEqual(result.returncode, 0, result.stderr)
        with zipfile.ZipFile(next((root / "dist").glob("*.jar"))) as archive:
            self.assertEqual(archive.namelist().count(TEXTURE), 1)
            self.assertEqual(archive.read(TEXTURE), b"official-texture")

    def test_conflicting_texture_still_fails(self):
        _, result = self.bundle({TEXTURE: b"outdated-texture"})
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Overlapping Ice Caves", result.stderr)

    def test_optional_tag_entries_merge_without_loss_or_duplicates(self):
        root, result = self.bundle({TAG: json.dumps({"values": [{"id": "minecraft:ice_caves", "required": False}, "minecraft:forest"]})})
        self.assertEqual(result.returncode, 0, result.stderr)
        with zipfile.ZipFile(next((root / "dist").glob("*.jar"))) as archive:
            self.assertEqual(json.loads(archive.read(TAG))["values"], [
                "minecraft:plains", {"id": "minecraft:ice_caves", "required": False}, "minecraft:forest"
            ])


if __name__ == "__main__":
    unittest.main()
