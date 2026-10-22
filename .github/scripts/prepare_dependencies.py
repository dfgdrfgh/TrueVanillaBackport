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
