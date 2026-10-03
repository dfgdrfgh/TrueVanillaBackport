"""Bundle pinned Platform and Tiny Takeover jars using each loader's native jar-in-jar format."""
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

main = production_jar(root / loader / "build/libs", "TrueVanillaBackport-")
platform = production_jar(root / "vendor/platform" / loader / "build/libs", f"Platform-{loader}-")
tiny = production_jar(root / "vendor/tiny" / "build/libs/1.5.1", "tiny_takeover_backport-")
output = root / "dist" / main.name
output.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(main) as source:
    data = {name: source.read(name) for name in source.namelist()}
nested = [(platform, "com.blackgear", "platform", "1.4.0.2638.1.1"),
          (tiny, "com.evandev", "tiny_takeover_backport", "1.5.1")]
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
