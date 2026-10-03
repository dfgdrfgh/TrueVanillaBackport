"""Prepare pinned dependencies for the reproducible parity builds."""
import json
import re
import sys
from pathlib import Path

platform, tiny = map(Path, sys.argv[1:3])
version = sys.argv[3]

# The bundled Platform version exposes the live reload registries to loot modifiers.
properties = platform / "gradle.properties"
properties.write_text(re.sub(r"mod_version\s*=.*", "mod_version = 1.4.0.2638.1.1", properties.read_text()))
if version == "1.21.1":
    p = platform / "common/src/main/java/com/blackgear/platform/common/data/LootModifier.java"
    p.write_text(p.read_text().replace("public interface LootTableContext {", "public interface LootTableContext {\n        net.minecraft.core.HolderLookup.Provider registries();"))
    for loader, expression in [("fabric", "provider"), ("neoforge", "event.getRegistries()")]:
        p = platform / f"{loader}/src/main/java/com/blackgear/platform/common/data/{loader}/LootModifierImpl.java"
        p.write_text(p.read_text().replace("new LootModifier.LootTableContext() {", "new LootModifier.LootTableContext() {\n                    @Override\n                    public net.minecraft.core.HolderLookup.Provider registries() {\n                        return " + expression + ";\n                    }"))

# Keep the embedded Tiny Takeover implementation independent of a new config GUI dependency.
# Its defaults and JSON configuration remain available; only the optional YACL screen is removed.
java = tiny / "src/main/java/com/evandev/tiny_takeover_backport"
p = java / "config/ModConfig.java"
s = p.read_text()
start, end = s.index("    public static Screen createScreen"), s.index("    public boolean isModelEnabled(Entity entity)")
s = s[:start] + s[end:]
s = re.sub(r"^import (?:dev\.isxander\.yacl3\..*|net\.minecraft\.client\.gui\.screens\.Screen);\n", "", s, flags=re.M)
p.write_text(s)
for path in ["compat/ModMenuIntegration.java", "client/ClientConfigSetup.java"]:
    (java / path).unlink()
p = java / "TinyTakeoverBackportForge.java"
p.write_text(p.read_text().replace("import com.evandev.tiny_takeover_backport.client.ClientConfigSetup;\n", "").replace("            ClientConfigSetup.register(modContainer);\n", ""))
p = tiny / "src/main/resources/fabric.mod.json"
d = json.loads(p.read_text())
d["entrypoints"].pop("modmenu", None)
d["depends"].pop("yet_another_config_lib_v3", None)
p.write_text(json.dumps(d, indent=2) + "\n")
for name in ["mods.toml", "neoforge.mods.toml"]:
    p = tiny / "src/main/resources/META-INF" / name
    s = p.read_text()
    blocks = re.split(r"(?=\[\[dependencies\.)", s)
    p.write_text("".join(block for block in blocks if 'modId = "yet_another_config_lib_v3"' not in block))
for p in tiny.glob("build.*.gradle.kts"):
    s = p.read_text()
    s = "\n".join(line for line in s.splitlines() if not ("modImplementation(" in line or "implementation(" in line) or not any(dependency in line for dependency in ["maven.modrinth:yacl:", "com.terraformersmc:modmenu:"])) + "\n"
    p.write_text(s)
