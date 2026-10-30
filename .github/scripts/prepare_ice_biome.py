"""Keep the pinned decoration feature from repainting the true biome's noise palette.

The decoration placement remains a 1.21.1 adaptation. Existing marked legacy
caves retain their fallback behavior; the new climate biome owns its terrain.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[2]
source = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / 'vendor/ice-caves/src/main/java/org/zerw/net/icecaves/worldgen/IceCaveFeature.java'
text = source.read_text()
old = 'if (isReplaceableCaveBlock(neighbor)) {'
new = 'if (!IceCaveSpawnControl.isTrueIceCaves(level, cursor) && isReplaceableCaveBlock(neighbor)) {'
assert text.count(old) == 1, 'Pinned cave wall decorator changed'
text = text.replace(old, new)
# Do not decorate neighboring surface biomes when a feature centered in the true
# cave biome searches its radius across the biome boundary.
old = 'if (level.method_37368(cursor)) {'
new = ('if (level.method_37368(cursor) && (!IceCaveSpawnControl.isTrueIceCaves(level, origin) '
       '|| IceCaveSpawnControl.isTrueIceCaves(level, cursor))) {')
assert text.count(old) == 1, 'Pinned decoration bounds changed'
source.write_text(text.replace(old, new))
print('PASS: true Ice Caves retain their noise palette; decorations stay inside the biome')
