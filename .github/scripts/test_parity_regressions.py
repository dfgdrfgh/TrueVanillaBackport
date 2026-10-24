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

golem = source("common/level/entities/mob/animal/golem/copper_golem/CopperGolemPathNavigation.java")
assert "columnPos.getY() < this.level.getMaxBuildHeight()" in golem
assert "columnPos.getY() >= this.level.getMaxBuildHeight() &&" not in golem

harness = source("common/integrations/interactions/GhastHarnessInteraction.java")
assert "stack.copyWithCount(1)" in harness, "Harness must retain data components"
assert "new ItemStack(stack.getItem())" not in harness

ghast = source("common/level/entities/mob/animal/happy_ghast/HappyGhast.java")
sneak = "if (player.isShiftKeyDown()) return InteractionResult.PASS;"
assert sneak in ghast
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
