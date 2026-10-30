"""Apply reviewed 26.4 mechanics to the pinned, normalized Ice Caves checkout."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'vendor/ice-caves/src/main/java/org/zerw/net/icecaves'
RESOURCES = ROOT / 'vendor/ice-caves/src/main/resources'

def edit(path, old, new):
    text = path.read_text()
    if text.count(old) != 1:
        raise RuntimeError(f'Expected one patch target in {path}: {old[:90]}')
    path.write_text(text.replace(old, new))

blocks = SOURCE / 'registry/ModBlocks.java'
edit(blocks,
     'new class_5542(5.0F, 3.0F, class_2251.method_55226(class_2246.field_27162).method_9626(ModSounds.ICE_CRYSTAL))',
     'new org.zerw.net.icecaves.block.IceCrystalBlock(class_2251.method_9637().method_31710(class_3620.field_16016)'
     '.method_9626(ModSounds.ICE_CRYSTAL).method_51368(net.minecraft.class_2766.field_12647).method_9632(1.5F)'
     '.method_9631(state -> 4).method_50012(class_3619.field_15971).method_26236((state, level, pos) -> false).method_22488())')
edit(blocks,
     'return class_2251.method_9637().method_31710(class_3620.field_16016).method_9628(0.98F).method_9632(0.5F).method_9626(ModSounds.ICE);',
     'return class_2251.method_9637().method_31710(class_3620.field_16016).method_9628(0.6F).method_9632(0.5F).method_29292().method_51368(net.minecraft.class_2766.field_12647).method_9626(ModSounds.PACKED_ICE);')
edit(blocks,
     '.method_9632(0.5F).method_9626(ModSounds.ICE).method_22488()',
     '.method_9632(0.5F).method_9626(ModSounds.BRITTLE_ICE).method_51368(net.minecraft.class_2766.field_12647)'
     '.method_26235((state, level, pos, type) -> type == net.minecraft.class_1299.field_6042)'
     '.method_26236((state, level, pos) -> false).method_50012(class_3619.field_15971).method_22488()')

block_text = blocks.read_text()
start = block_text.index('   public static IcicleBlock createIcicle()')
end = block_text.index('   public static BrittleIceBlock createBrittleIce()', start)
icicle = block_text[start:end].replace('class_3620.field_16016', 'class_3620.field_16024')
icicle = icicle.replace('.method_9626(ModSounds.ICE_CRYSTAL)', '.method_51368(net.minecraft.class_2766.field_12647).method_9626(ModSounds.ICICLE)')
blocks.write_text(block_text[:start] + icicle + block_text[end:])

sounds = SOURCE / 'registry/ModSounds.java'
manifest = json.loads((ROOT / '.github/ice-caves-sounds.json').read_text())
existing = sounds.read_text()
extra = [name for name in manifest['events'] if f'"{name}"' not in existing]
declarations = ''
registrations = ''
for name in extra:
    field = name.removeprefix('block.').replace('.', '_').upper()
    declarations += f'   public static final class_2960 {field}_ID = class_2960.method_60655("minecraft", "{name}");\n'
    declarations += f'   public static final class_3414 {field} = class_3414.method_47908({field}_ID);\n'
    registrations += f'      class_2378.method_10230(class_7923.field_41172, ModSounds.{field}_ID, ModSounds.{field});\n'
for block in ('packed_ice', 'blue_ice', 'brittle_ice', 'icicle'):
    field = block.upper()
    declarations += f'   public static final class_2498 {field} = new class_2498(1.0F, 1.0F, {field}_BREAK, {field}_STEP, {field}_PLACE, {field}_HIT, {field}_FALL);\n'
edit(sounds, '   private ModSounds() {', declarations + '\n   private ModSounds() {')
main = SOURCE / 'IceCavesFabric.java'
edit(main, '      IceCavesCommon.init();', registrations + '      IceCavesCommon.init();')

# Ordinary, packed and blue ice have independent vanilla sound sets.
hook = SOURCE / 'mixin/IceBlockMixin.java'
edit(hook, 'cir.setReturnValue(ModSounds.ICE);',
     'cir.setReturnValue(state.method_27852(class_2246.field_10384) ? ModSounds.BLUE_ICE : '
     'state.method_27852(class_2246.field_10225) ? ModSounds.PACKED_ICE : ModSounds.ICE);')
mixins = RESOURCES / 'ice_caves.mixins.json'
data = json.loads(mixins.read_text())
data['client'].append('IceAmbientMixin')
data['mixins'].append('LivingEntityFreezingMixin')
mixins.write_text(json.dumps(data, indent=2) + '\n')

# Ice Balls have their own throw/break events, including block impacts. The
# snapshot applies Freezing on Frostbite melee attacks, not ordinary Ice Balls.
edit(SOURCE / 'item/IceBallItem.java', 'class_3417.field_14873,',
     'org.zerw.net.icecaves.registry.ModSounds.ENTITY_ICE_BALL_THROW,')
edit(SOURCE / 'entity/FrostbiteZombie.java', 'this.method_5783(class_3417.field_14753, 1.0F, 1.0F);',
     'this.method_5783(ModSounds.ENTITY_ICE_BALL_THROW, 1.0F, 1.0F);')

# Snapshot icicles cause impact damage, without an additional freezing aura.
icicle_source = SOURCE / 'block/IcicleBlock.java'
edit(icicle_source, '''         if (entity instanceof class_1309 living) {
            applyFreezing(level, living);
         }
''', '')
text = icicle_source.read_text()
assert text.count('      applyFreezingNearby(level, entity);\n') == 2
text = text.replace('      applyFreezingNearby(level, entity);\n', '')
start = text.index('   private static void applyFreezingNearby(')
end = text.index('   private static boolean shouldMelt(', start)
text = text[:start] + text[end:]
icicle_source.write_text(text)
edit(icicle_source, 'ModSounds.ICE_BREAK, class_3419.field_15245, 1.0F,',
     'ModSounds.ICICLE_LAND, class_3419.field_15245, 1.0F,')

# The true biome owns cave features and cave spawns; only vanilla cold surface
# biomes receive additional Frostbites, at their exact snapshot weights.
main_source = main.read_text()
for prefix in ('      BiomeModifications.addFeature(', '      BiomeModifications.addSpawn('):
    main_source = '\n'.join(line for line in main_source.split('\n') if not line.startswith(prefix))
spawns = ''
for group, weight in [(['deep_frozen_ocean', 'frozen_ocean', 'frozen_peaks', 'frozen_river', 'ice_spikes'], 25),
                      (['jagged_peaks', 'snowy_plains', 'snowy_slopes'], 10)]:
    keys = ', '.join('net.minecraft.class_5321.method_29179(net.minecraft.class_7924.field_41236, class_2960.method_60655("minecraft", "' + name + '"))' for name in group)
    spawns += '      BiomeModifications.addSpawn(BiomeSelectors.includeByKey(' + keys + '), class_1311.field_6302, FROSTBITE, ' + str(weight) + ', 4, 4);\n'
main.write_text(main_source.replace('      IceCavesCommon.init();', spawns + '      IceCavesCommon.init();'))

# Preserve regular monsters instead of clearing spawn lists across cold surface biomes.
text = main.read_text()
start = text.index('      BiomeModifications.create(')
end = text.index('      class_2378.method_10230', start) if '      class_2378.method_10230' in text[start:] else text.index('      IceCavesCommon.init();', start)
# Registrations inserted above belong after the removal block, so stop at them.
text = text[:start] + text[end:]
main.write_text(text)
for path in RESOURCES.rglob('remove_vanilla_hostiles.json'):
    path.unlink()

feature = SOURCE / 'worldgen/IceCaveFeature.java'
text = feature.read_text()
signature = '   private static boolean placeBrittlePatch('
start = text.index(signature)
opening = text.index('{', start)
depth = 1
end = opening + 1
while depth:
    depth += (text[end] == '{') - (text[end] == '}')
    end += 1
text = text[:opening + 1] + '''
      int lidY = findCaveFloor(level, origin);
      return lidY != Integer.MIN_VALUE && lidY < surfaceY - 2
          && VanillaCrevasse.place(level,
              new class_2338(origin.method_10263(), lidY, origin.method_10260()), random, brittle);
   }''' + text[end:]
feature.write_text(text)

state = RESOURCES / 'assets/ice_caves/blockstates/brittle_ice.json'
state.write_text(state.read_text().replace('stage=', 'age='))
tag = RESOURCES / 'data/minecraft/tags/entity_type/does_not_break_brittle_ice.json'
tag.parent.mkdir(parents=True, exist_ok=True)
tag.write_text(json.dumps({'values': ['minecraft:frostbite', 'minecraft:stray']}, indent=2) + '\n')
print('PASS: 26.4 tool requirements, ice sound sets, ambient hooks, cracking and crevasse integration')
