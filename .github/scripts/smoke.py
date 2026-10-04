"""Start an assembled, installable backport jar using the production loader."""
import os
import json
import hashlib
import re
import signal
import shutil
import subprocess
import sys
import time
import urllib.request
from pathlib import Path

loader = sys.argv[1]
wwoo = len(sys.argv) > 2 and sys.argv[2] == "wwoo"
root = Path.cwd()
properties = dict(re.findall(r"^([\w_]+)\s*=\s*(.*?)\s*$", (root / "gradle.properties").read_text(), re.M))
version = properties["minecraft_version"]
runtime = root / "build" / ("wwoo-smoke" if wwoo else "production-smoke")
runtime.mkdir(parents=True, exist_ok=True)
mods = runtime / "mods"
mods.mkdir(exist_ok=True)
jars = list((root / "dist").glob("*.jar"))
assert len(jars) == 1, jars
shutil.copy2(jars[0], mods / jars[0].name)
(runtime / "eula.txt").write_text("eula=true\n")
(runtime / "server.properties").write_text("online-mode=false\nlevel-type=" +
    ("minecraft:normal" if wwoo else "minecraft:flat") +
    "\nlevel-seed=12345\nview-distance=2\nsimulation-distance=2\n")
java = str(Path(os.environ["JAVA_HOME"]) / "bin/java")

def download(url, destination):
    if not destination.exists():
        with urllib.request.urlopen(url, timeout=90) as response, destination.open("wb") as output:
            shutil.copyfileobj(response, output)

if loader == "fabric":
    fabric_loader = "0.18.5" if wwoo else properties["fabric_loader_version"]
    download(f"https://meta.fabricmc.net/v2/versions/loader/{version}/{fabric_loader}/1.0.3/server/jar", runtime / "launcher.jar")
    api = "0.116.12+1.21.1" if wwoo else properties["fabric_api_version"]
    download(f"https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/{api}/fabric-api-{api}.jar", mods / "fabric-api.jar")
    command = [java, "-Xmx2G", "-jar", "launcher.jar", "nogui"]
else:
    forge = properties[f"{loader}_version"]
    if loader == "forge":
        installer = f"https://maven.minecraftforge.net/net/minecraftforge/forge/{forge}/forge-{forge}-installer.jar"
        arguments = f"libraries/net/minecraftforge/forge/{forge}/unix_args.txt"
    else:
        installer = f"https://maven.neoforged.net/releases/net/neoforged/neoforge/{forge}/neoforge-{forge}-installer.jar"
        arguments = f"libraries/net/neoforged/neoforge/{forge}/unix_args.txt"
    download(installer, runtime / "installer.jar")
    subprocess.run([java, "-jar", "installer.jar", "--installServer"], cwd=runtime, check=True, timeout=600)
    command = [java, "-Xmx2G", "@" + arguments, "nogui"]
if wwoo:
    # Test external mods without redistributing or embedding them in the backport jar.
    dependencies = {
        "fabric": ["JBbDOEnc", "BRVWgniI"],
        "neoforge": ["sU7oaRZv", "JbGjwnV6"],
    }
    pins = json.loads((root / ".github/scripts/wwoo-smoke-dependencies.json").read_text())
    for version_id in dependencies[loader] + [pins[loader]]:
        with urllib.request.urlopen("https://api.modrinth.com/v2/version/" + version_id, timeout=90) as response:
            metadata = json.load(response)
        artifact = next(file for file in metadata["files"] if file["primary"])
        destination = mods / artifact["filename"]
        download(artifact["url"], destination)
        assert hashlib.sha512(destination.read_bytes()).hexdigest() == artifact["hashes"]["sha512"], destination

log = root / ("smoke-wwoo-server.log" if wwoo else "smoke-server.log")
passed = False
with log.open("w") as output:
    process = subprocess.Popen(command, cwd=runtime, stdin=subprocess.PIPE, stdout=output,
                               stderr=subprocess.STDOUT, start_new_session=True)
    deadline = time.monotonic() + 600
    ready = None
    locate_sent = False
    content_probe_sent = False
    try:
        while time.monotonic() < deadline:
            content = log.read_text(errors="replace")
            if re.search(r"Mixin apply failed|InjectionError|InvalidMixinException|ReportedException|Exception in thread", content):
                break
            if process.poll() is not None:
                break
            if re.search(r'Done \([^)]+\)! For help', content):
                ready = ready or time.monotonic()
                if not content_probe_sent:
                    # Exercise integrated item/block registration and baby entity mixins.
                    process.stdin.write(b"setblock 0 80 0 minecraft:golden_dandelion\n")
                    process.stdin.write(b'summon minecraft:cow 0 81 0 {Age:-24000}\n')
                    process.stdin.flush()
                    content_probe_sent = True
                if wwoo and not locate_sent:
                    if "WWOO compatibility: using native Pale Garden plateau placement" not in content:
                        raise RuntimeError("WWOO Pale Garden placement was not activated")
                    process.stdin.write(b"execute positioned 0 80 0 run locate biome minecraft:pale_garden\n")
                    process.stdin.flush()
                    locate_sent = True
                if wwoo and "Could not find a biome" in content:
                    raise RuntimeError("Pale Garden was not found in the seeded WWOO normal world")
                located = not wwoo or re.search(r"nearest .*pale_garden.* is at", content)
                content_registered = "Changed the block at 0, 80, 0" in content and "Summoned new Cow" in content
                if time.monotonic() - ready >= 5 and located and content_registered:
                    passed = True
                    break
            time.sleep(1)
    finally:
        if process.poll() is None:
            if passed:
                process.stdin.write(b"stop\n")
                process.stdin.flush()
                try:
                    process.wait(timeout=20)
                except subprocess.TimeoutExpired:
                    pass
            if process.poll() is None:
                os.killpg(process.pid, signal.SIGTERM)
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    os.killpg(process.pid, signal.SIGKILL)
print(log.read_text(errors="replace"))
if not passed:
    raise SystemExit("Production server startup did not pass")
print("Assembled TrueVanillaBackport" + (" + WWOO normal-world" if wwoo else "") + " server startup passed")
