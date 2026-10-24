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

print("PASS: Garden Awakens, Spring to Life, Copper Age, and Chase the Skies parity guards")
