"""Fetch official, unmodified Platform 1.21.1 releases from Modrinth.

Run before Gradle compilation. Both loader jars remain external and are never
embedded in VanillaBackport. Artifact hashes are verified before use.
"""
import hashlib
import io
import json
import re
import urllib.request
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
props = dict(re.findall(r"^([\w_]+)\s*=\s*(.*?)\s*$",
    (root / "gradle.properties").read_text(), re.M))
mc = props["minecraft_version"]
version = props["platform_release_version"]
with urllib.request.urlopen("https://api.modrinth.com/v2/project/i6fiqm5y/version", timeout=90) as response:
    releases = json.load(response)
for loader in ("fabric", "neoforge"):
    name = f"Platform-{loader}-{mc}-{version}.jar"
    candidates = [
        f for release in releases
        if mc in release.get("game_versions", []) and loader in release.get("loaders", [])
        for f in release.get("files", [])
        if f["filename"].lower() == name.lower()
    ]
    if len(candidates) != 1:
        raise SystemExit(f"Expected one published Platform artifact {name}; found {len(candidates)}")
    f = candidates[0]
    destination = root / "libs" / name
    destination.parent.mkdir(exist_ok=True)
    if destination.is_file() and hashlib.sha512(destination.read_bytes()).hexdigest() == f["hashes"]["sha512"]:
        print(f"Verified cached published Platform: {name}")
        continue
    with urllib.request.urlopen(f["url"], timeout=120) as response:
        data = response.read()
    if hashlib.sha512(data).hexdigest() != f["hashes"]["sha512"]:
        raise SystemExit(f"SHA-512 mismatch for published Platform {name}")
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        if archive.testzip() is not None:
            raise SystemExit(f"Corrupt external Platform {name}")
        if loader == "fabric":
            meta = json.loads(archive.read("fabric.mod.json"))
            if meta["id"] != "platform" or meta["version"] != version:
                raise SystemExit(f"Unexpected Platform metadata in {name}: {meta}")
    destination.write_bytes(data)
    print(f"Fetched published external Platform {name} (verified SHA-512)")
