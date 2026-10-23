"""Bundle the pinned Platform jar using each loader's native jar-in-jar format."""
import hashlib
import json
import sys
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
version, loader = sys.argv[1:3]

def production_jar(directory, prefix):
    jars = [p for p in directory.glob("*.jar") if p.name.startswith(prefix)
            and not any(p.stem.endswith(suffix) for suffix in ["-dev", "-dev-shadow", "-sources", "-javadoc"]) ]
    if len(jars) != 1:
        raise RuntimeError(f"Expected one production jar in {directory}: {jars}")
    return jars[0]

main = production_jar(root / loader / "build/libs", "VanillaBackport-")
platform = production_jar(root / "vendor/platform" / loader / "build/libs", f"Platform-{loader}-")
output = root / "dist" / main.name
output.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(main) as source:
    data = {name: source.read(name) for name in source.namelist()}
properties = dict((key.strip(), value.strip()) for line in (root / "gradle.properties").read_text().splitlines()
                  if "=" in line and not line.strip().startswith("#")
                  for key, value in [line.split("=", 1)])
platform_version = properties["platform_indev_version"]
nested = [(platform, "com.blackgear", "platform", platform_version)]

def merge_tag(previous, incoming, name):
    """Keep both mods' block tags when collapsing two datapacks into one."""
    left = json.loads(previous)
    right = json.loads(incoming)
    if left.get("replace") or right.get("replace"):
        raise RuntimeError(f"Conflicting replacement tag cannot be safely merged: {name}")
    if not isinstance(left.get("values"), list) or not isinstance(right.get("values"), list):
        raise RuntimeError(f"Unsupported tag merge: {name}")
    left["values"] = list(dict.fromkeys(left["values"] + right["values"]))
    left["replace"] = False
    return (json.dumps(left, indent=2) + "\n").encode()

# Fabric: integrate Ice Caves into the SAME mod, not as a separately loaded
# nested Fabric mod. Classes, resources, entrypoints and mixins live in the
# outer TrueVanillaBackport jar; Platform remains a real bundled dependency.
if loader == "fabric":
    ice_build = root / "vendor/ice-caves/build/libs"
    ice_mod = production_jar(ice_build, "ice-caves-backport-fabric-")
    with zipfile.ZipFile(ice_mod) as ice:
        if ice.testzip() is not None:
            raise RuntimeError("Ice Caves module archive is corrupt")
        ice_meta = json.loads(ice.read("fabric.mod.json"))
        if ice_meta.get("id") != "ice_caves" or ice_meta.get("depends", {}).get("minecraft") != "~1.21.1":
            raise RuntimeError(f"Wrong Ice Caves build: {ice_meta}")
        if ice_meta.get("jars") or ice_meta.get("accessWidener"):
            raise RuntimeError("Ice Caves module contains unsupported nested dependencies/access widener")

        skipped_metadata = {"fabric.mod.json", "pack.mcmeta", "ice_caves.png"}
        for info in ice.infolist():
            name = info.filename
            if info.is_dir() or name in skipped_metadata or name.startswith("META-INF/"):
                continue
            content = ice.read(info)
            if name in data:
                if name.startswith("data/") and "/tags/" in name and name.endswith(".json"):
                    data[name] = merge_tag(data[name], content, name)
                    continue
                raise RuntimeError(f"Ice Caves resource would replace VanillaBackport content: {name}")
            data[name] = content

    metadata = json.loads(data["fabric.mod.json"])
    for group in ("main", "client"):
        entries = metadata.setdefault("entrypoints", {}).setdefault(group, [])
        for entry in ice_meta.get("entrypoints", {}).get(group, []):
            if entry in entries:
                raise RuntimeError(f"Duplicate Fabric initializer: {entry}")
            entries.append(entry)
    for config in ice_meta.get("mixins", []):
        if config in metadata.setdefault("mixins", []):
            raise RuntimeError(f"Duplicate Fabric Mixin configuration: {config}")
        metadata["mixins"].append(config)
    # Other mods that rely on the former ice_caves ID still see this capability;
    # Fabric loads only the vanillabackport mod instead of a second child mod.
    aliases = metadata.setdefault("provides", [])
    if "ice_caves" not in aliases:
        aliases.append("ice_caves")
    data["fabric.mod.json"] = json.dumps(metadata, indent=2).encode()

if loader == "fabric":
    metadata = json.loads(data["fabric.mod.json"])
    for jar, _, _, _ in nested:
        path = f"META-INF/jars/{jar.name}"
        metadata.setdefault("jars", []).append({"file": path})
        data[path] = jar.read_bytes()
    data["fabric.mod.json"] = json.dumps(metadata, indent=2).encode()
else:
    metadata = json.loads(data.get("META-INF/jarjar/metadata.json", b'{"jars":[]}'))
    for jar, group, artifact, artifact_version in nested:
        path = f"META-INF/jarjar/{jar.name}"
        metadata["jars"].append({"identifier": {"group": group, "artifact": artifact},
            "version": {"range": f"[{artifact_version},)", "artifactVersion": artifact_version},
            "path": path, "isObfuscated": loader == "forge"})
        data[path] = jar.read_bytes()
    data["META-INF/jarjar/metadata.json"] = json.dumps(metadata, indent=2).encode()
with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as result:
    for name, content in data.items():
        result.writestr(name, content)
(output.parent / (output.name + ".sha256")).write_text(hashlib.sha256(output.read_bytes()).hexdigest() + "  " + output.name + "\n")
print(output)
