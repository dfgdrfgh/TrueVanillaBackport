"""Validate the pinned upstream Platform build without modifying its source.

The current upstream VanillaBackport uses Platform's V2 dynamic recipe API.
Older fork-time patches must not be injected into this Platform revision.
"""
import re
import sys
from pathlib import Path

platform = Path(sys.argv[1])
minecraft_version = sys.argv[2]
properties = (platform / "gradle.properties").read_text()
expected_platform_version = "1.4.0.2640.1"

def property_value(name):
    for line in properties.splitlines():
        if "=" not in line or line.lstrip().startswith("#"):
            continue
        key, value = line.split("=", 1)
        if key.strip() == name:
            return value.strip()
    raise SystemExit(f"Missing Platform property: {name}")

if property_value("minecraft_version") != minecraft_version:
    raise SystemExit("Platform checkout does not target the required Minecraft version")
if property_value("mod_version") != expected_platform_version:
    raise SystemExit("Platform checkout has the wrong version: " + property_value("mod_version"))
if not (platform / "common/src/main/java/com/blackgear/platform/common/v2/recipe/DynamicRecipeManager.java").is_file():
    raise SystemExit("Platform checkout is missing the dynamic recipe API")
print(f"Verified Platform {expected_platform_version} for Minecraft {minecraft_version}")
