"""Regression checks for isolated 1.21.1 common gameplay parity changes.

These source-level checks catch accidental reverts; build/smoke tests still
exercise compilation, startup, and WWOO placement separately.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "common/src/main/java/com/blackgear/vanillabackport"

def source(path: str) -> str:
    target = JAVA / path
    assert target.is_file(), target
    return target.read_text()

bundle_use = source("core/mixin/common/bundle_ui/BundleItemMixin.java")
assert "public int getUseDuration(ItemStack stack, LivingEntity entity)" in bundle_use
assert "BundleFeatures.onBundleUpdate() ? 72000" in bundle_use
assert 'method = "use", at = @At("RETURN")' in bundle_use
assert "player.startUsingItem(hand)" in bundle_use
assert "remainingUseDuration % 8 == 0" in bundle_use
assert "BundleItemHandler.dropContents(stack, player)" in bundle_use

eyeblossom = source("common/level/blocks/EyeblossomBlock.java")
assert "if (!level.dimensionType().natural()) return false;" in eyeblossom, (
    "Eyeblossoms must not change states in the Nether or End"
)
assert "&& this.type.open\n" in eyeblossom, (
    "Only OPEN eyeblossoms should poison bees"
)

firefly = source("common/level/blocks/FireflyBushBlock.java")
assert "if (level.getMaxLocalRawBrightness(pos) <= FIREFLY_SPAWN_MAX_BRIGHTNESS_LEVEL)" in firefly
assert "FIREFLY_SPAWN_MAX_BRIGHTNESS_LEVEL = 13;" in firefly

mixin = source("core/mixin/common/copper_age/ShelfComparatorMixin.java")
assert "@Mixin(ComparatorBlock.class)" in mixin
assert "behindState.getBlock() instanceof ShelfBlock" in mixin
assert "behindState.getValue(ShelfBlock.FACING) != comparatorFacing.getOpposite()" in mixin
import json
mixins = json.loads((ROOT / "common/src/main/resources/vanillabackport.mixins.json").read_text())
assert "common.copper_age.ShelfComparatorMixin" in mixins["mixins"]

# 26.4 stew accepts any two mushroom-tag items, not one red plus one brown.
stew_recipe = ROOT / "common/src/main/resources/data/minecraft/recipe/suspicious_stew_from_golden_dandelion.json"
import json
stew_ingredients = json.loads(stew_recipe.read_text())["ingredients"]
assert sum(ingredient.get("tag") == "minecraft:mushrooms" for ingredient in stew_ingredients) == 2
assert not any(ingredient.get("item") in ("minecraft:red_mushroom", "minecraft:brown_mushroom") for ingredient in stew_ingredients)

sulfur_spikes = source("common/level/blocks/SpeleothemBlock.java")
assert "level.getBlockState(pos.above()).is(state.getBlock())" in sulfur_spikes
assert "level.getBlockState(pos.above()).is(Blocks.POINTED_DRIPSTONE)" not in sulfur_spikes

fast_slide = ROOT / "common/src/main/generated/data/minecraft/tags/item/sulfur_cube_archetype/fast_sliding.json"
import json
fast_slide_items = json.loads(fast_slide.read_text())["values"]
for required_item in ("minecraft:packed_ice_bricks", "minecraft:chiseled_packed_ice"):
    assert {"id": required_item, "required": False} in fast_slide_items, (
        "Fabric-only ice brick items must be optional in the shared tag for NeoForge"
    )

nautilus_menu = source("common/level/inventory/NautilusInventoryMenu.java")
assert "int i = 2;" in nautilus_menu, "Two visible Nautilus equipment slots precede player slots"
assert "this.horseContainer.getContainerSize() + 1" not in nautilus_menu
assert "int k = i + 27;" in nautilus_menu
assert "int m = k + 9;" in nautilus_menu

copper_chest = source("common/level/blocks/CopperChestBlock.java")
for copper, waxed_chest in (
    ("WAXED_COPPER_BLOCK", "WAXED_COPPER_CHEST"),
    ("WAXED_EXPOSED_COPPER", "WAXED_EXPOSED_COPPER_CHEST"),
    ("WAXED_WEATHERED_COPPER", "WAXED_WEATHERED_COPPER_CHEST"),
    ("WAXED_OXIDIZED_COPPER", "WAXED_OXIDIZED_COPPER_CHEST"),
):
    assert f"Blocks.{copper}, ModBlocks.{waxed_chest}" in copper_chest, (
        f"{copper} must become the waxed chest with matching weather stage"
    )

fireflies = source("client/level/particle/FireflyParticle.java")
assert "if (!this.level.getBlockState" not in fireflies, (
    "Fireflies must survive non-solid blocks, leaving collision to Particle.move"
)
assert "this.random.nextFloat()" in fireflies

golem = source("common/level/entities/mob/animal/golem/copper_golem/CopperGolemPathNavigation.java")
assert "columnPos.getY() < this.level.getMaxBuildHeight()" in golem
assert "columnPos.getY() >= this.level.getMaxBuildHeight() &&" not in golem

harness = source("common/integrations/interactions/GhastHarnessInteraction.java")
assert "stack.copyWithCount(1)" in harness, "Harness must retain data components"
assert "new ItemStack(stack.getItem())" not in harness

ghast = source("common/level/entities/mob/animal/happy_ghast/HappyGhast.java")
sneak = "if (player.isShiftKeyDown()) return InteractionResult.PASS;"
assert sneak in ghast
assert "if (input.lengthSqr() > 1.0D)" in ghast and "input = input.normalize();" in ghast, "Mounted Happy Ghast diagonals must not exceed the single-axis input speed"
assert ghast.index(sneak) < ghast.index("player.startRiding(this)"), (
    "Sneaking should bypass mounting"
)

# Tree-ground solidity must be queried at the support block's position,
# not at the position above it (which affects generated log placement).
fallen_tree = source("common/level/worldgen/features/FallenTreeFeature.java")
assert "isFaceSturdy(level, pos.below(), Direction.UP)" in fallen_tree
assert "isFaceSturdy(level, pos, Direction.UP)" not in fallen_tree

# The undercoat is an adult model; Tiny Takeover supplies the baby's fur.
sheep = source("client/level/layer/SheepWoolUndercoatLayer.java")
assert "sheep.isBaby()" in sheep and "!sheep.isBaby()" in sheep

# Match newer vanilla's Dennis data definition. The 1.21.1 painting tooltip
# implementation also requires separate runtime testing for title/author display.
import json
painting_file = ROOT / "common/src/main/generated/data/minecraft/painting_variant/dennis.json"
painting = json.loads(painting_file.read_text())
assert painting["title"] == {"color": "yellow", "translate": "painting.minecraft.dennis.title"}
assert painting["author"] == {"color": "gray", "translate": "painting.minecraft.dennis.author"}
language_file = ROOT / "common/src/main/generated/assets/vanillabackport/lang/en_us.json"
translations = json.loads(language_file.read_text())
assert translations["painting.minecraft.dennis.title"] == "Dennis"
assert translations["painting.minecraft.dennis.author"] == "Sarah Boeving"

print("PASS: Garden Awakens, Spring to Life, Copper Age, and Chase the Skies parity guards")
