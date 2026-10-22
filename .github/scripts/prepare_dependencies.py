"""Prepare pinned dependencies for the reproducible parity builds."""
import re
import sys
from pathlib import Path

platform = Path(sys.argv[1])
version = sys.argv[2]

# The bundled Platform version exposes the live reload registries to loot modifiers.
properties = platform / "gradle.properties"
properties.write_text(re.sub(r"mod_version\s*=.*", "mod_version = 1.4.0.2638.1.1", properties.read_text()))
if version == "1.21.1":
    properties.write_text(re.sub(r"neoforge_version\s*=.*", "neoforge_version = 21.1.219", properties.read_text()))
    p = platform / "common/src/main/java/com/blackgear/platform/common/data/LootModifier.java"
    p.write_text(p.read_text().replace("public interface LootTableContext {", "public interface LootTableContext {\n        net.minecraft.core.HolderLookup.Provider registries();"))
    for loader, expression in [("fabric", "provider"), ("neoforge", "event.getRegistries()")]:
        p = platform / f"{loader}/src/main/java/com/blackgear/platform/common/data/{loader}/LootModifierImpl.java"
        p.write_text(p.read_text().replace("new LootModifier.LootTableContext() {", "new LootModifier.LootTableContext() {\n                    @Override\n                    public net.minecraft.core.HolderLookup.Provider registries() {\n                        return " + expression + ";\n                    }"))

    # Upstream 26w39a passes builders; retain the pinned Platform implementation.
    p = platform / "common/src/main/java/com/blackgear/platform/common/data/LootModifier.java"
    s = p.read_text()
    s = s.replace("        void addPool(LootPool.Builder pool);", """        void addPool(LootPool.Builder pool);

        default boolean addToPool(int index, LootPoolEntryContainer.Builder<?>... content) {
            return this.addToPool(index, java.util.Arrays.stream(content).map(LootPoolEntryContainer.Builder::build).toArray(LootPoolEntryContainer[]::new));
        }

        default boolean addToPool(LootPoolEntryContainer.Builder<?>... content) {
            return this.addToPool(0, content);
        }""")
    p.write_text(s)

# Avoid Forge's Maven mirror for unrelated Maven Central libraries.
# Forge's mirror has caused transient download timeouts on the CI runners.
settings = platform / "settings.gradle"
config = settings.read_text()
forge_repo = 'maven { url "https://files.minecraftforge.net/maven/" }'
filtered = '''maven {
            url "https://files.minecraftforge.net/maven/"
            content {
                excludeGroup "net.md-5"
                excludeGroup "com.google.guava"
            }
        }'''
if forge_repo not in config:
    raise RuntimeError("Pinned Platform settings.gradle changed; inspect its repositories")
config = config.replace(forge_repo, filtered)
# Maven Central holds SpecialSource and Guava. Prefer it before Forge.
config = config.replace('        gradlePluginPortal()', '        mavenCentral()\\n        gradlePluginPortal()'.replace('\\n','\n'))
settings.write_text(config)
