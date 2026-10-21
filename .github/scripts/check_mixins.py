"""Fail early if any mixin manifest refers to a missing Java source class.

This catches absent fork-only mixins before an expensive runtime smoke test.
Nested mixins (Outer$Inner) have their declaration in Outer.java.
"""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[2]
source_roots = [root / module / "src/main/java" for module in ("common", "fabric", "neoforge")]
manifest_paths = sorted(
    file
    for module in ("common", "fabric", "neoforge")
    for file in (root / module / "src/main/resources").glob("*mixins.json")
)
count = 0
missing = []
for manifest in manifest_paths:
    data = json.loads(manifest.read_text())
    prefix = data["package"].replace(".", "/")
    for side in ("mixins", "client", "server"):
        for entry in data.get(side, []):
            count += 1
            outer = entry.split("$", 1)[0]
            path = Path(prefix) / (outer.replace(".", "/") + ".java")
            if not any((source_root / path).is_file() for source_root in source_roots):
                missing.append(f"{manifest.relative_to(root)}: {entry} -> {path}")
if missing:
    raise SystemExit("Missing mixin source classes:\n" + "\n".join(missing))
print(f"PASS: all {count} mixin references across {len(manifest_paths)} manifests have source classes")
