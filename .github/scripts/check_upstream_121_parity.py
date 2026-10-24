"""Regression checks for the VanillaBackport 1.2.1 upstream corrections.

Runs on Fabric and NeoForge; verifies the committed game data and the Java
registration sources. This is not a substitute for full modpack gameplay tests.
"""
from pathlib import Path
import json
import re

root = Path(__file__).resolve().parents[2]
def java(path):
    return (root / "common/src/main/java/com/blackgear/vanillabackport" / path).read_text()

colors = ("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
          "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black")
data = root / "common/src/main/generated/data/minecraft"
for color in colors:
    for kind in ("wool", "concrete"):
        name = f"{color}_{kind}_slab"
        loot = json.loads((data / f"loot_table/blocks/{name}.json").read_text())
        assert len(loot["pools"]) == 1
        pool = loot["pools"][0]
        assert pool["entries"][0]["name"] == f"minecraft:{name}"
        functions = pool["entries"][0]["functions"]
        assert functions[0]["function"] == "minecraft:set_count"
        assert float(functions[0]["count"]) == 2
        assert functions[0]["conditions"] == [{
            "block": f"minecraft:{name}",
            "condition": "minecraft:block_state_property",
            "properties": {"type": "double"},
        }]
        assert functions[1]["function"] == "minecraft:explosion_decay"

    for suffix, group, tag in (
        ("wool_stairs", "wool_stairs", "wool_stairs"),
        ("wool_slab", "wool_slabs", "wool_slabs"),
        ("harness", "harness_dye", "harnesses"),
        ("cushion", "cushion_dye", "cushions"),
    ):
        recipe = json.loads((data / f"recipe/dye_{color}_{suffix}.json").read_text())
        assert recipe["group"] == group
        assert recipe["ingredients"] == [
            {"item": f"minecraft:{color}_dye"},
            {"tag": f"minecraft:{tag}"},
        ]
        assert recipe["result"]["id"] == f"minecraft:{color}_{suffix}"
        assert (data / f"tags/item/{tag}.json").exists()

loot_gen = (root / "fabric/src/main/java/com/blackgear/vanillabackport/data/server/loot/BlockLootGenerator.java").read_text()
assert len(re.findall(r"this\.add\(ModBlocks\.[A-Z_]+_(?:WOOL|CONCRETE)_SLAB\.get\(\), this::createSlabItemTable\);", loot_gen)) == 32
recipe_gen = (root / "fabric/src/main/java/com/blackgear/vanillabackport/data/server/recipe/RecipeGenerator.java").read_text()
assert recipe_gen.count("colorBlockWithTag(output, dyes,") == 4
assert ".requires(Ingredient.of(recolorable))" in recipe_gen

integration = java("common/integrations/BlockIntegrations.java")
common_setup = java("common/CommonSetup.java")
for name in ("POPLAR_PLANKS", "POPLAR_SLAB", "POPLAR_STAIRS", "POPLAR_LOG",
             "STRIPPED_POPLAR_LOG", "POPLAR_WOOD", "STRIPPED_POPLAR_WOOD",
             "RED_POPLAR_LEAVES", "ORANGE_POPLAR_LEAVES", "YELLOW_POPLAR_LEAVES",
             "POPLAR_FENCE", "POPLAR_FENCE_GATE", "POPLAR_SHELF"):
    assert f"event.registerFlammableBlock(ModBlocks.{name}.get()," in integration
assert "FireBlock fire =" not in common_setup
assert "|| !ModChecker.CAVERNS_AND_CHASMS" not in integration
assert integration.count("if (VanillaBackport.COMMON_CONFIG.hasWeatheringLightningRods.get())") == 2
assert "mod_version = 1.2.1" in (root / "gradle.properties").read_text()
assert common_setup.index("DynamicRecipeManager.register(") > common_setup.index("public static void asyncSetup(")

nether = java("core/mixin/common/happy_ghast/NetherFossilPieceMixin.java")
assert "level.getBiome(origin).is(BiomeTags.IS_NETHER)" in nether
assert "dimensionType().ultraWarm()" not in nether

for path in ("core/mixin/common/controllable_mounts/HuskMixin.java",
             "core/mixin/common/zombie_horses/ZombieHorseMixin.java",
             "core/mixin/common/spear_handler_mob/PiglinMixin.java",
             "core/mixin/common/spear_handler_mob/ZombifiedPiglinMixin.java"):
    src = java(path)
    assert "VanillaBackport.COMMON_CONFIG.hasSpears.get()" in src

creative = java("client/registries/ModBundledTabs.java")
mounts = creative.split("public static final BundledTabs MOUNTS_OF_MAYHEM", 1)[1].split(
    "public static final BundledTabs TINY_TAKEOVER", 1)[0]
assert "ModEnchantments.LUNGE" not in mounts, "Extra Lunge books must not be reinserted by bundled tab"

mixins = json.loads((root / "common/src/main/resources/vanillabackport.mixins.json").read_text())
for retired in ("common.registry.HolderReferenceMixin", "common.registry.MappedRegistryMixin"):
    assert retired not in mixins["mixins"], "Never hide duplicate registrations with a fallback mixin"

# Also guard upstream copper-tool tag generation and a discovered dye tag typo.
tags_source = (root / "fabric/src/main/java/com/blackgear/vanillabackport/data/server/tags/ItemTagGenerator.java").read_text()
assert ".add(ModItems.COPPER_PICKAXE.get());" in tags_source
assert "ModItems.GREEN_BUNDLE.get(), ModItems.GREEN_HARNESS.get(), ModItems.GREEN_CUSHION.get()" in tags_source
green_tag = json.loads((root / "common/src/main/generated/data/c/tags/item/dyed/green.json").read_text())
assert "minecraft:green_cushion" in green_tag["values"] and "minecraft:gray_cushion" not in green_tag["values"]
harvest_tag = json.loads((data / "tags/item/cluster_max_harvestables.json").read_text())
assert "minecraft:copper_pickaxe" in harvest_tag["values"]

print("PASS: VanillaBackport 1.2.1 upstream loot, dye, fire, ghast, spear, creative and registry corrections")

# Copper nugget tag, sheep undercoat layer and copper golem water behavior.
for recipe_id in ("copper_torch", "copper_lantern", "copper_chain"):
    recipe = json.loads((data / f"recipe/{recipe_id}.json").read_text())
    assert any(value == {"tag": "c:nuggets/copper"} for value in recipe["key"].values()), recipe_id
assert (root / "common/src/main/generated/data/c/tags/item/nuggets/copper.json").exists()
assert recipe_gen.count("ConventionalItemTags.NUGGETS_COPPER") == 3
layers = java("client/registries/ModModelLayers.java")
entity_rendering = java("client/integrations/rendering/EntityRendering.java")
undercoat = java("client/level/layer/SheepWoolUndercoatLayer.java")
assert 'SHEEP_WOOL_UNDERCOAT = register("sheep_wool_undercoat")' in layers
assert "event.register(ModModelLayers.SHEEP_WOOL_UNDERCOAT, SheepModel::createBodyLayer)" in entity_rendering
assert "models.bakeLayer(ModModelLayers.SHEEP_WOOL_UNDERCOAT)" in undercoat
assert "models.bakeLayer(ModelLayers.SHEEP)" not in undercoat
assert "public boolean canBreatheUnderwater()" in java("common/level/entities/mob/animal/golem/copper_golem/CopperGolem.java")
print("PASS: copper nugget tag, sheep undercoat model and copper golem breathing regressions")
