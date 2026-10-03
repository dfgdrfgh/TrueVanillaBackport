"""Start the backport and bundled Tiny Takeover together on a dedicated server."""
import os
import re
import signal
import subprocess
import sys
import time
from pathlib import Path

loader = sys.argv[1]
for directory in [Path(loader) / "run", Path(loader) / "runs/server"]:
    directory.mkdir(parents=True, exist_ok=True)
    (directory / "eula.txt").write_text("eula=true\n")
    (directory / "server.properties").write_text("online-mode=false\nlevel-type=minecraft:flat\nview-distance=2\nsimulation-distance=2\n")
log = Path("smoke-server.log")
passed = False
with log.open("w") as output:
    process = subprocess.Popen(["bash", "gradlew", f":{loader}:runServer", "--stacktrace"],
        stdin=subprocess.PIPE, stdout=output, stderr=subprocess.STDOUT, start_new_session=True)
    deadline = time.monotonic() + 600
    ready = None
    try:
        while time.monotonic() < deadline:
            content = log.read_text(errors="replace")
            if re.search(r"Mixin apply failed|InjectionError|InvalidMixinException|ReportedException|BUILD FAILED", content):
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
    raise SystemExit("Bundled server startup did not pass")
print("Backport and Tiny Takeover server startup passed")
