"""Validate binary linkage against the *published* external Platform release.

Compiling against pinned Platform source is insufficient: its method names
may differ from the release mod Minecraft actually loads. Inspect bytecode
method and field references before running the full startup smoke test.
"""
import hashlib
import json
import struct
import sys
import urllib.request
import zipfile
from pathlib import Path

loader = sys.argv[1]
assert loader in ("fabric", "neoforge"), loader
release = next(line.split("=", 1)[1].strip() for line in Path("gradle.properties").read_text().splitlines() if line.startswith("platform_release_version"))
minecraft = "1.21.1"
name = f"Platform-{loader}-{minecraft}-{release}.jar"
with urllib.request.urlopen("https://api.modrinth.com/v2/project/i6fiqm5y/version", timeout=90) as response:
    versions = json.load(response)
matches = [
    file
    for version in versions
    if minecraft in version.get("game_versions", []) and loader in version.get("loaders", [])
    for file in version.get("files", [])
    if file["filename"] == name
]
assert len(matches) == 1, f"Expected one published {name}, got {len(matches)}"
with urllib.request.urlopen(matches[0]["url"], timeout=90) as response:
    jar_bytes = response.read()
assert hashlib.sha512(jar_bytes).hexdigest() == matches[0]["hashes"]["sha512"], name

destination = Path("build") / "external-platform" / name
destination.parent.mkdir(parents=True, exist_ok=True)
destination.write_bytes(jar_bytes)


def read_class(data):
    offset = 0

    def read(fmt):
        nonlocal offset
        size = struct.calcsize(fmt)
        result = struct.unpack_from(fmt, data, offset)
        offset += size
        return result[0] if len(result) == 1 else result

    if read(">I") != 0xCAFEBABE:
        raise ValueError("Invalid Java class header")
    read(">H")
    read(">H")
    count = read(">H")
    cp = [None] * count
    index = 1
    while index < count:
        tag = read(">B")
        if tag == 1:
            length = read(">H")
            cp[index] = (tag, data[offset:offset + length].decode("utf-8", "replace"))
            offset += length
        elif tag in (3, 4):
            offset += 4
        elif tag in (5, 6):
            offset += 8
            index += 1
        elif tag in (7, 8, 16, 19, 20):
            cp[index] = (tag, read(">H"))
        elif tag in (9, 10, 11, 12, 17, 18):
            cp[index] = (tag, read(">H"), read(">H"))
        elif tag == 15:
            cp[index] = (tag, read(">B"), read(">H"))
        else:
            raise ValueError(f"Unsupported constant pool tag {tag}")
        index += 1

    def string(i):
        return cp[i][1]

    def classname(i):
        return string(cp[i][1]) if i else None

    read(">H")  # access flags
    owner = classname(read(">H"))
    parent = classname(read(">H"))
    interfaces = [classname(read(">H")) for _ in range(read(">H"))]

    def members():
        result = set()
        for _ in range(read(">H")):
            read(">H")  # modifiers
            n = string(read(">H"))
            d = string(read(">H"))
            result.add((n, d))
            for _ in range(read(">H")):
                read(">H")
                offset_skip = read(">I")
                skip(offset_skip)
        return result

    def skip(size):
        nonlocal offset
        offset += size

    # keep each classfile's exact declared method and field descriptors
    fields = members()
    methods = members()
    refs = set()
    for entry in cp:
        if entry is None or entry[0] not in (9, 10, 11):
            continue
        target = classname(entry[1])
        if not target.startswith("com/blackgear/platform/"):
            continue
        name_type = cp[entry[2]]
        refs.add((entry[0], target, string(name_type[1]), string(name_type[2])))
    return owner, (parent, interfaces, methods, fields), refs


def classes_in(path):
    classes = {}
    references = set()
    with zipfile.ZipFile(path) as archive:
        for entry in archive.namelist():
            if not entry.endswith(".class") or entry.startswith("META-INF/versions/"):
                continue
            owner, structure, refs = read_class(archive.read(entry))
            classes[owner] = structure
            references.update(refs)
    return classes, references


platform, unused = classes_in(destination)
products = list(Path("dist").glob("*.jar"))
assert len(products) == 1, products
mod, references = classes_in(products[0])
embedded = sorted(key for key in mod if key.startswith("com/blackgear/platform/"))
assert not embedded, f"VanillaBackport unexpectedly bundles Platform classes: {embedded[:10]}"


def declares(target, member, method, seen):
    if target in seen or target not in platform:
        return False
    seen.add(target)
    parent, interfaces, methods, fields = platform[target]
    if member in (methods if method else fields):
        return True
    return any(declares(base, member, method, seen) for base in [parent, *interfaces] if base)


missing = []
for tag, owner, member, descriptor in sorted(references):
    if owner not in platform:
        missing.append(f"Missing Platform class: {owner}")
    elif not declares(owner, (member, descriptor), tag != 9, set()):
        missing.append(f"Missing {'field' if tag == 9 else 'method'}: {owner}.{member} {descriptor}")

print(f"Verified {len(references)} Platform method/field references in {products[0].name} against {name}")
if missing:
    print("\n".join(missing))
    raise SystemExit(f"Published Platform {release} ABI incompatible ({len(missing)} references)")
print("PASS: No missing Platform classes or directly-linked members")
