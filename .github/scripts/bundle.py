"""Assemble VanillaBackport with Platform as a separate runtime dependency."""
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
output = root / "dist" / main.name
output.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(main) as source:
    data = {name: source.read(name) for name in source.namelist()}

def merge_tag(previous, incoming, name):
    """Keep both mods' block tags when collapsing two datapacks into one."""
    left = json.loads(previous)
    right = json.loads(incoming)
    if left.get("replace") or right.get("replace"):
        raise RuntimeError(f"Conflicting replacement tag cannot be safely merged: {name}")
    if not isinstance(left.get("values"), list) or not isinstance(right.get("values"), list):
        raise RuntimeError(f"Unsupported tag merge: {name}")
    # Registry tags may contain optional {id, required} entries as well as strings.
    for value in right["values"]:
        if value not in left["values"]:
            left["values"].append(value)
    left["replace"] = False
    return (json.dumps(left, indent=2) + "\n").encode()

# Fabric: integrate Ice Caves into the SAME mod, not as a separately loaded
# nested Fabric mod. Classes, resources, entrypoints and mixins live in the
# outer TrueVanillaBackport jar; Platform is installed as a separate mod.
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

        def normalize_resource(name, content):
            # Materialize the data and assets in vanilla's own minecraft namespace.
            if "ice_door" in name:
                return None, None
            if name.startswith("assets/ice_caves/textures/entity/frostbite"):
                name = name.replace("textures/entity/frostbite", "textures/entity/zombie/frostbite", 1)
            for prefix in ("assets/ice_caves/", "data/ice_caves/"):
                if name.startswith(prefix):
                    name = prefix.split("/")[0] + "/minecraft/" + name[len(prefix):]
                    break
            if not name.endswith(".json"):
                return name, content

            node = json.loads(content.decode("utf-8")
                .replace("ice_caves:", "minecraft:")
                .replace(".ice_caves.", ".minecraft."))

            def remove_door(value):
                if isinstance(value, dict):
                    return {k: remove_door(v) for k,v in value.items() if "ice_door" not in k}
                if isinstance(value, list):
                    return [remove_door(v) for v in value if not (isinstance(v,str) and "ice_door" in v)]
                return value

            node = remove_door(node)
            return name, (json.dumps(node, ensure_ascii=False, indent=2) + "\n").encode()

        def merge_dictionary(previous, incoming, name):
            left, right = json.loads(previous), json.loads(incoming)
            if not isinstance(left, dict) or not isinstance(right, dict):
                raise RuntimeError(f"Not dictionary JSON metadata: {name}")
            for key, value in right.items():
                if key in left and left[key] != value:
                    raise RuntimeError(f"Conflicting resource value at {name}: {key}")
                left[key] = value
            return (json.dumps(left, ensure_ascii=False, indent=2) + "\n").encode()

        skipped_metadata = {"fabric.mod.json", "pack.mcmeta", "ice_caves.png"}
        for info in ice.infolist():
            name = info.filename
            if info.is_dir() or name in skipped_metadata or name.startswith("META-INF/"):
                continue
            name, content = normalize_resource(name, ice.read(info))
            if name is None:
                continue
            if name in data:
                # Both modules carry the official jukebox textures. Collapse exact
                # duplicates; a differing asset must still fail for review below.
                if data[name] == content:
                    continue
                if name.startswith("data/") and "/tags/" in name and name.endswith(".json"):
                    data[name] = merge_tag(data[name], content, name)
                    continue
                if name.startswith("assets/minecraft/lang/") or name == "assets/minecraft/sounds.json":
                    data[name] = merge_dictionary(data[name], content, name)
                    continue
                raise RuntimeError(f"Overlapping Ice Caves and VanillaBackport resource: {name}")
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
    data["fabric.mod.json"] = json.dumps(metadata, indent=2).encode()

# Keep the loader-level Platform dependency, but never nest/shade its binary.
if loader == "fabric":
    metadata = json.loads(data["fabric.mod.json"])
    assert "platform" in metadata.get("depends", {}), "Missing external Platform dependency"
    assert not any("platform" in entry.get("file", "").lower()
                   for entry in metadata.get("jars", []))
else:
    assert 'modId = "platform"' in data["META-INF/neoforge.mods.toml"].decode()
    if "META-INF/jarjar/metadata.json" in data:
        jarjar = json.loads(data["META-INF/jarjar/metadata.json"])
        assert not any(entry.get("identifier", {}).get("artifact") == "platform"
                       for entry in jarjar.get("jars", []))
assert not any(name.startswith("com/blackgear/platform/") or
               name.startswith("META-INF/jars/Platform-") or
               name.startswith("META-INF/jarjar/Platform-") for name in data), (
    "Platform must be installed separately, never embedded"
)

with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as result:
    for name, content in data.items():
        result.writestr(name, content)
(output.parent / (output.name + ".sha256")).write_text(hashlib.sha256(output.read_bytes()).hexdigest() + "  " + output.name + "\n")
print(output)
