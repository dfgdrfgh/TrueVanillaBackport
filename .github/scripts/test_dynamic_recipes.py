"""Run the production recipe provider/codecs with real Minecraft recipe builders.

Small registry fixtures replace mod startup, so this checks all eight config
combinations independently of either loader. Production startup checks separately
exercise both reload mixins and the actual mod registries.
"""
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
source = root / "common/src/main/java/com/blackgear/vanillabackport"
classpath = Path(sys.argv[1]).read_text()
java = Path(os.environ["JAVA_HOME"]) / "bin/java"
provider = source / "common/level/recipes/VanillaDynamicRecipes.java"
items = sorted(set(re.findall(r"ModItems\.(\w+)\.get\(\)", provider.read_text())))
fixtures = {
    "com.blackgear.vanillabackport.common.registries.items.ModItems": """
        import net.minecraft.core.Registry;
        import net.minecraft.core.registries.BuiltInRegistries;
        import net.minecraft.resources.ResourceLocation;
        import net.minecraft.world.item.Item;
        public class ModItems {
            public record Entry(Item value) { public Item get() { return value; } }
            private static Entry register(String name) {
                return new Entry(Registry.register(BuiltInRegistries.ITEM,
                    ResourceLocation.withDefaultNamespace(name), new Item(new Item.Properties())));
            }
            %s
        }
    """ % "\n".join(f'public static final Entry {item} = register("{item.lower()}");' for item in items),
    "com.blackgear.vanillabackport.common.registries.blocks.ModBlocks": """
        import net.minecraft.world.level.block.Blocks;
        public class ModBlocks {
            public record Entry(net.minecraft.world.level.block.Block value) {
                public net.minecraft.world.level.block.Block get() { return value; }
            }
            public static final Entry SHELF_MUSHROOM = new Entry(Blocks.BROWN_MUSHROOM);
        }
    """,
    "com.blackgear.vanillabackport.core.VanillaBackport": """
        public class VanillaBackport {
            public record Flag(boolean enabled) { public boolean get() { return enabled; } }
            public record Config(Flag hasCopperNuggets, Flag hasCopperToolSet, Flag hasSpears) {}
            public static Config COMMON_CONFIG;
        }
    """.replace("public record Config(Flag hasCopperNuggets, Flag hasCopperToolSet, Flag hasSpears) {}", """
        public static class Config {
            public final Flag hasCopperNuggets, hasCopperToolSet, hasSpears;
            public Config(boolean nuggets, boolean copper, boolean spears) {
                hasCopperNuggets = new Flag(nuggets); hasCopperToolSet = new Flag(copper); hasSpears = new Flag(spears);
            }
        }
    """),
    "com.blackgear.vanillabackport.core.data.tags.ModItemTags": """
        import net.minecraft.core.registries.Registries;
        import net.minecraft.resources.ResourceLocation;
        import net.minecraft.tags.TagKey;
        import net.minecraft.world.item.Item;
        public class ModItemTags {
            public static final TagKey<Item> EGGS = TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("eggs"));
            public static final TagKey<Item> MUSHROOMS = TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("mushrooms"));
        }
    """,
    "DynamicRecipeTest": r"""
        import com.blackgear.vanillabackport.common.level.recipes.DynamicRecipeResources;
        import com.blackgear.vanillabackport.common.registries.items.ModItems;
        import com.blackgear.vanillabackport.core.VanillaBackport;
        import com.blackgear.vanillabackport.core.data.tags.ModItemTags;
        import com.google.gson.JsonElement;
        import net.minecraft.SharedConstants;
        import net.minecraft.server.Bootstrap;
        import net.minecraft.core.RegistryAccess;
        import net.minecraft.core.registries.BuiltInRegistries;
        import net.minecraft.resources.ResourceLocation;
        import net.minecraft.world.item.Items;
        import java.util.*;

        public class DynamicRecipeTest {
            private static ResourceLocation id(String path) { return ResourceLocation.withDefaultNamespace(path); }
            private static void require(boolean condition, String message) {
                if (!condition) throw new AssertionError(message);
            }
            public static void main(String[] args) throws Exception {
                SharedConstants.tryDetectVersion();
                Bootstrap.bootStrap();
                // Reopen only the fixture item registry to register the recipe outputs.
                var registry = BuiltInRegistries.ITEM;
                var frozen = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
                frozen.setAccessible(true); frozen.set(registry, false);
                var intrusive = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
                intrusive.setAccessible(true); intrusive.set(registry, new IdentityHashMap<>());
                ModItems.COPPER_PICKAXE.get();
                registry.freeze();
                registry.bindTags(Map.of(
                    ModItemTags.EGGS, List.of(registry.wrapAsHolder(Items.EGG)),
                    ModItemTags.MUSHROOMS, List.of(registry.wrapAsHolder(Items.BROWN_MUSHROOM), registry.wrapAsHolder(Items.RED_MUSHROOM))));
                var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
                for (int flags = 0; flags < 8; flags++) {
                    boolean nuggets = (flags & 1) != 0, copper = (flags & 2) != 0, spears = (flags & 4) != 0;
                    VanillaBackport.COMMON_CONFIG = new VanillaBackport.Config(nuggets, copper, spears);
                    Map<ResourceLocation, JsonElement> recipes = DynamicRecipeResources.addRecipes(Map.of(), registries);
                    var advancements = DynamicRecipeResources.addAdvancements(Map.of(), registries);
                    for (String path : List.of("lead", "bundle", "saddle", "lodestone", "cake", "mushroom_stew")) {
                        require(recipes.containsKey(id(path)), "Missing vanilla replacement " + path);
                    }
                    require(recipes.containsKey(id("copper_pickaxe")) == copper, "Copper config ignored");
                    require(recipes.containsKey(id("wooden_spear")) == spears, "Spear config ignored");
                    require(recipes.containsKey(id("copper_spear")) == (spears && copper), "Copper spear gates ignored");
                    require(recipes.containsKey(id("copper_nugget")) == nuggets, "Nugget config ignored");
                    for (String metal : List.of("iron", "gold")) {
                        for (String furnace : List.of("smelting", "blasting")) {
                            var recipe = recipes.get(id(metal + "_nugget_from_" + furnace));
                            String vanillaItem = metal.equals("iron") ? "iron_pickaxe" : "golden_pickaxe";
                            require(recipe.toString().contains("minecraft:" + vanillaItem), "Vanilla recycling lost: " + metal);
                        }
                    }
                    for (var entry : advancements.entrySet()) {
                        var rewards = entry.getValue().getAsJsonObject().getAsJsonObject("rewards").getAsJsonArray("recipes");
                        for (var reward : rewards) require(recipes.containsKey(ResourceLocation.parse(reward.getAsString())), "Unlock points to missing recipe");
                    }
                    require(advancements.containsKey(id("recipes/tools/lead")), "Lead has no unlock");
                    require(advancements.containsKey(id("recipes/tools/copper_pickaxe")) == copper, "Copper unlock config ignored");
                    var reloaded = DynamicRecipeResources.addRecipes(Map.of(), registries);
                    require(recipes.equals(reloaded), "Data reload is not stable");
                }
                System.out.println("PASS: eight recipe config combinations, vanilla recycling, recipe unlocks, stable reload generation");
            }
        }
    """,
}
with tempfile.TemporaryDirectory() as directory:
    folder = Path(directory)
    files = []
    for name, body in fixtures.items():
        path = folder / (name.replace(".", "/") + ".java")
        path.parent.mkdir(parents=True, exist_ok=True)
        package = name.rsplit(".", 1)[0] if "." in name else None
        path.write_text((f"package {package};\n" if package else "") + body)
        files.append(str(path))
    files += [str(provider), str(source / "common/level/recipes/DynamicRecipeResources.java")]
    subprocess.run([str(java.with_name("javac")), "-proc:none", "--release", "21", "-cp", classpath,
                    "-d", str(folder), *files], check=True)
    subprocess.run([str(java), "-cp", str(folder) + os.pathsep + classpath, "DynamicRecipeTest"], check=True)
