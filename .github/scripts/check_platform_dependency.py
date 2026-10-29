"""Check the installable mod requires external Platform and contains no bundled API."""
import json
import re
import sys
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
loader = sys.argv[1]
assert loader in ("fabric", "neoforge"), loader
properties = dict((k.strip(), v.strip())
                  for line in (root / "gradle.properties").read_text().splitlines()
                  if "=" in line and not line.lstrip().startswith("#")
                  for k, v in [line.split("=", 1)])
minimum = properties["platform_release_version"]
jars = sorted((root / "dist").glob("*.jar"))
assert len(jars) == 1, jars
external = root / "libs" / f"Platform-{loader}-1.21.1-{minimum}.jar"
assert external.is_file(), f"Missing external Platform mod: {external}"
with zipfile.ZipFile(external) as api:
    assert "com/blackgear/platform/core/BuiltInCoreRegistry.class" in api.namelist()
    if loader == "fabric":
        metadata = json.loads(api.read("fabric.mod.json"))
        assert metadata["id"] == "platform" and metadata["version"] == minimum, metadata
with zipfile.ZipFile(jars[0]) as mod:
    names = mod.namelist()
    assert not any(n.startswith("com/blackgear/platform/") for n in names), "Shaded Platform classes found"
    assert not any(n.lower().endswith(".jar") and "platform-" in n.lower()
                   for n in names), "Nested Platform jar found"
    if loader == "fabric":
        metadata = json.loads(mod.read("fabric.mod.json"))
        assert metadata["depends"]["platform"] == f">={minimum}", metadata["depends"]["platform"]
        assert not any("platform" in j.get("file", "").lower() for j in metadata.get("jars", []))
    else:
        manifest = mod.read("META-INF/neoforge.mods.toml").decode()
        dep = re.search(r'\[\[dependencies\.vanillabackport\]\]\s*modId = "platform".*?versionRange = "(.*?)"', manifest, re.S)
        assert dep and dep.group(1) == f"[{minimum},)", "External Platform version range mismatch"
        if "META-INF/jarjar/metadata.json" in names:
            metadata = json.loads(mod.read("META-INF/jarjar/metadata.json"))
            assert not any(j.get("identifier", {}).get("artifact") == "platform"
                           for j in metadata.get("jars", [])), "Embedded Platform jarjar entry"
print(f"PASS: {loader} requires separate Platform >= {minimum}, no embedded API")
