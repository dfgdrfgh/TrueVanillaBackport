"""Start an assembled, installable backport jar using the production loader."""
import os
import re
import signal
import shutil
import subprocess
import sys
import time
import urllib.request
from pathlib import Path

loader = sys.argv[1]
root = Path.cwd()
properties = dict(re.findall(r"^([\w_]+)\s*=\s*(.*?)\s*$", (root / "gradle.properties").read_text(), re.M))
version = properties["minecraft_version"]
runtime = root / "build" / "production-smoke"
runtime.mkdir(parents=True, exist_ok=True)
mods = runtime / "mods"
mods.mkdir(exist_ok=True)
jars = list((root / "dist").glob("*.jar"))
assert len(jars) == 1, jars
shutil.copy2(jars[0], mods / jars[0].name)
(runtime / "eula.txt").write_text("eula=true\n")
(runtime / "server.properties").write_text("online-mode=false\nlevel-type=minecraft:flat\nview-distance=2\nsimulation-distance=2\n")
java = str(Path(os.environ["JAVA_HOME"]) / "bin/java")

def download(url, destination):
    if not destination.exists():
        request = urllib.request.Request(url, headers={"User-Agent": "TrueVanillaBackport-build/1.0"})
        with urllib.request.urlopen(request, timeout=90) as response, destination.open("wb") as output:
            shutil.copyfileobj(response, output)

if loader == "fabric":
    fabric_loader = properties["fabric_loader_version"]
    download(f"https://meta.fabricmc.net/v2/versions/loader/{version}/{fabric_loader}/1.0.3/server/jar", runtime / "launcher.jar")
    api = properties["fabric_api_version"]
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
log = root / "smoke-server.log"
passed = False
with log.open("w") as output:
    process = subprocess.Popen(command, cwd=runtime, stdin=subprocess.PIPE, stdout=output,
                               stderr=subprocess.STDOUT, start_new_session=True)
    deadline = time.monotonic() + 600
    ready = None
    try:
        while time.monotonic() < deadline:
            content = log.read_text(errors="replace")
            if re.search(r"Mixin apply failed|InjectionError|InvalidMixinException|ReportedException|Exception in thread", content):
                break
            if process.poll() is not None:
                break
            if re.search(r'Done \([^)]+\)! For help', content):
                ready = ready or time.monotonic()
                if time.monotonic() - ready >= 5:
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
print("Assembled TrueVanillaBackport server startup passed")
