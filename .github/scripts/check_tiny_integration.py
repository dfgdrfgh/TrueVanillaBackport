"""Check that Tiny Takeover ships inside the fork, without a second mod container."""
import io
import json
import sys
import zipfile
from pathlib import Path

loader = sys.argv[1]
jars = list(Path('dist').glob('*.jar'))
assert len(jars) == 1, jars
with zipfile.ZipFile(jars[0]) as jar:
    names = set(jar.namelist())
    prefix = 'com/evandev/tiny_takeover_backport/'
    for name in ['CommonClass.class', 'config/ModConfig.class', 'registry/ModRegistry.class',
                 'client/ModBabyTextureRegistry.class']:
        assert prefix + name in names, name
    for name in ['assets/minecraft/blockstates/golden_dandelion.json',
                 'data/minecraft/recipe/golden_dandelion.json',
                 'data/minecraft/recipe/name_tag.json',
                 'META-INF/licenses/tiny-takeover.txt']:
        assert name in names, name
    mixins = json.loads(jar.read('vanillabackport-tiny.mixins.json'))
    for name in mixins['mixins'] + mixins['client']:
        assert mixins['package'].replace('.', '/') + '/' + name.replace('.', '/') + '.class' in names, name
    lang = json.loads(jar.read('assets/vanillabackport/lang/en_us.json'))
    assert lang['bundled_tab.tiny_takeover.title'] == 'Tiny Takeover'
    if loader == 'fabric':
        metadata = json.loads(jar.read('fabric.mod.json'))
        assert metadata['id'] == 'vanillabackport'
        assert 'tiny_takeover_backport' not in metadata.get('depends', {})
        assert 'vanillabackport-tiny.mixins.json' in metadata['mixins']
        assert prefix.replace('/', '.') + 'TinyTakeoverBackportFabric' in metadata['entrypoints']['main']
    else:
        metadata = jar.read('META-INF/neoforge.mods.toml').decode()
        assert 'modId = "tiny_takeover_backport"' not in metadata
        assert 'vanillabackport-tiny.mixins.json' in metadata
    for name in names:
        if name.endswith('.jar'):
            assert 'tiny' not in name.lower(), name
            with zipfile.ZipFile(io.BytesIO(jar.read(name))) as nested:
                if 'fabric.mod.json' in nested.namelist():
                    assert json.loads(nested.read('fabric.mod.json'))['id'] != 'tiny_takeover_backport'
                for descriptor in ['META-INF/neoforge.mods.toml', 'META-INF/mods.toml']:
                    if descriptor in nested.namelist():
                        assert 'modId = "tiny_takeover_backport"' not in nested.read(descriptor).decode()
print('Integrated Tiny Takeover content and dependency checks passed')
