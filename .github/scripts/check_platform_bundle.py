"""Validate that installable jars bundle the Platform API they were compiled against.

A separately installed older Platform takes precedence in some Fabric environments.
The mod manifests therefore require the exact minimum compatible Platform version.
"""
import io
import json
import re
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
loader = sys.argv[1]
assert loader in {"fabric", "neoforge"}, loader
properties = dict((key.strip(), value.strip()) for line in (root / "gradle.properties").read_text().splitlines()
                  if "=" in line and not line.strip().startswith("#")
                  for key, value in [line.split("=", 1)])
minimum = properties["platform_indev_version"]
jars = sorted((root / "dist").glob("*.jar"))
assert len(jars) == 1, f"Expected one bundled VanillaBackport artifact: {jars}"
with zipfile.ZipFile(jars[0]) as outer:
    if loader == "fabric":
        metadata = json.loads(outer.read("fabric.mod.json"))
        assert metadata["depends"]["platform"] == f">={minimum}", metadata["depends"]["platform"]
        nested = [entry["file"] for entry in metadata["jars"] if "Platform-fabric-" in entry["file"]]
    else:
        manifest = outer.read("META-INF/neoforge.mods.toml").decode("utf-8")
        dep = re.search(r'\[\[dependencies\.vanillabackport\]\]\s*modId = "platform".*?versionRange = "(.*?)"', manifest, re.S)
        assert dep and dep.group(1) == f"[{minimum},)", "Outdated NeoForge Platform minimum version"
        metadata = json.loads(outer.read("META-INF/jarjar/metadata.json"))
        nested = [entry["path"] for entry in metadata["jars"]
                  if entry["identifier"].get("artifact") == "platform"]
        assert any(entry["version"]["artifactVersion"] == minimum for entry in metadata["jars"]
                   if entry["identifier"].get("artifact") == "platform"), "Wrong nested Platform version"
    assert len(nested) == 1, f"Expected one embedded Platform jar: {nested}"
    with zipfile.ZipFile(io.BytesIO(outer.read(nested[0]))) as platform:
        if loader == "fabric":
            actual = json.loads(platform.read("fabric.mod.json"))["version"]
            assert actual == minimum, f"Embedded Platform is {actual}, expected {minimum}"
        symbol = "com/blackgear/platform/core/BuiltInCoreRegistry.class"
        assert symbol in platform.namelist(), "Embedded Platform missing BuiltInCoreRegistry"
    with tempfile.TemporaryDirectory() as directory:
        embedded = Path(directory) / "Platform.jar"
        embedded.write_bytes(outer.read(nested[0]))
        result = subprocess.run(["javap", "-s", "-classpath", str(embedded),
                                 "com.blackgear.platform.core.BuiltInCoreRegistry"],
                                text=True, capture_output=True, check=True)
        signature = "(Ljava/lang/String;Ljava/lang/Object;)Lcom/blackgear/platform/core/api/RegistryKey;"
        assert signature in result.stdout, "Embedded Platform lacks the registry method required by VanillaBackport"
print(f"PASS: {loader} bundles Platform {minimum} with the expected BuiltInCoreRegistry API")
