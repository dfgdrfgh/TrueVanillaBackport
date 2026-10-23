"""Prepare pinned Platform 1.21.1 dependency and stable Maven resolution."""
import sys
from pathlib import Path

platform = Path(sys.argv[1])
version = sys.argv[2]

properties = (platform / "gradle.properties").read_text()
if "mod_version = 1.4.0.2640.1" not in properties:
    raise RuntimeError("Unexpected Platform version; update the pinned commit and workflow")

# This pinned Platform version already implements the registry accessor and
# dynamic recipe API. Do not overwrite upstream classes with compatibility stubs.
if version == "1.21.1":
    settings = platform / "settings.gradle"
    config = settings.read_text()
    forge_repo = 'maven { url "https://files.minecraftforge.net/maven/" }'
    filtered = """maven {
            url "https://files.minecraftforge.net/maven/"
            content {
                excludeGroup "net.md-5"
                excludeGroup "com.google.guava"
            }
        }"""
    if forge_repo not in config:
        raise RuntimeError("Pinned Platform settings.gradle changed unexpectedly")
    config = config.replace(forge_repo, filtered)
    config = config.replace("        gradlePluginPortal()", "        mavenCentral()\n        gradlePluginPortal()")
    settings.write_text(config)
