"""Check the vanilla plateau selection, config toggle, and optional-mod boundary."""
import os
import subprocess
import tempfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
java = str(Path(os.environ['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in os.environ else 'java'
fixtures = {
    'net.minecraft.resources.ResourceLocation': 'public record ResourceLocation(String value) { public String getNamespace() { return value.split(":", 2)[0]; } }',
    'net.minecraft.resources.ResourceKey': 'public record ResourceKey<T>(String id) { public ResourceLocation location() { return new ResourceLocation(id); } }',
    'net.minecraft.world.level.biome.Biome': 'public class Biome {}',
    'net.minecraft.world.level.biome.Biomes': '''public class Biomes {
        public static final net.minecraft.resources.ResourceKey<Biome> DARK_FOREST = new net.minecraft.resources.ResourceKey<>("minecraft:dark_forest");\n        public static final net.minecraft.resources.ResourceKey<Biome> TAIGA = new net.minecraft.resources.ResourceKey<>("minecraft:taiga");
    }''',
    'net.minecraft.world.level.biome.Climate': 'public class Climate { public record Parameter(long min, long max) {} }',
    'com.blackgear.vanillabackport.core.ModChecker': 'public class ModChecker { public static boolean WWOO = true; }',
    'com.blackgear.vanillabackport.core.VanillaBackport': '''public class VanillaBackport {
        public static final Config COMMON_CONFIG = new Config();
        public static class Config { public final Flag hasPaleGarden = new Flag(); }
        public static class Flag { public boolean value = true; public boolean get() { return value; } }
    }''',
    'com.blackgear.vanillabackport.common.registries.worldgen.ModBiomes': '''public class ModBiomes {
        public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> PALE_GARDEN = new net.minecraft.resources.ResourceKey<>("pale_garden");
    }''',
    'test.PlacementTest': '''
        import net.minecraft.resources.ResourceKey;
        import net.minecraft.world.level.biome.*;
        import com.blackgear.vanillabackport.core.*;
        import com.blackgear.vanillabackport.common.registries.worldgen.ModBiomes;
        import com.blackgear.vanillabackport.common.integrations.compat.wwoo.WwooBiomeCompatibility;
        public class PlacementTest {
            static void check(boolean value) { if (!value) throw new AssertionError(); }
            public static void main(String[] args) {
                var variant = new Climate.Parameter(1, 10000);
                for (int temperature = 0; temperature < 5; temperature++) {
                    for (int humidity = 0; humidity < 5; humidity++) {
                        var selected = WwooBiomeCompatibility.selectPlateauBiome(Biomes.DARK_FOREST, temperature, humidity, variant);
                        check(selected.equals(temperature == 2 && humidity == 4 ? ModBiomes.PALE_GARDEN : Biomes.DARK_FOREST));
                    }
                }
                // WWOO can return spruce/taiga instead of the vanilla dark-forest key.
                check(WwooBiomeCompatibility.selectPlateauBiome(Biomes.TAIGA, 2, 4, variant).equals(ModBiomes.PALE_GARDEN));
                check(WwooBiomeCompatibility.selectPlateauBiome(Biomes.TAIGA, 2, 3, variant).equals(Biomes.TAIGA));
                check(WwooBiomeCompatibility.selectPlateauBiome(Biomes.TAIGA, 2, 4, new Climate.Parameter(-10000, -1)).equals(Biomes.TAIGA));
                check(WwooBiomeCompatibility.selectPlateauBiome(Biomes.DARK_FOREST, 2, 4, new Climate.Parameter(-10000, -1)).equals(Biomes.DARK_FOREST));
                check(WwooBiomeCompatibility.selectPlateauBiome(Biomes.DARK_FOREST, 2, 4, new Climate.Parameter(0, 0)).equals(ModBiomes.PALE_GARDEN));
                var otherMod = new ResourceKey<Biome>("another_mod:forest");
                check(WwooBiomeCompatibility.selectPlateauBiome(otherMod, 2, 4, variant).equals(otherMod));
                VanillaBackport.COMMON_CONFIG.hasPaleGarden.value = false;
                check(WwooBiomeCompatibility.selectPlateauBiome(Biomes.DARK_FOREST, 2, 4, variant).equals(Biomes.DARK_FOREST));
                VanillaBackport.COMMON_CONFIG.hasPaleGarden.value = true;
                ModChecker.WWOO = false;
                check(!WwooBiomeCompatibility.useNativePaleGardenPlacement());
                check(WwooBiomeCompatibility.selectPlateauBiome(Biomes.DARK_FOREST, 2, 4, variant).equals(Biomes.DARK_FOREST));
                System.out.println("PASS: Pale Garden plateau slot even with WWOO taiga substitutions, no added climate points, config off, WWOO absent, other mod biome preserved");
            }
        }
    ''',
}
with tempfile.TemporaryDirectory(prefix='wwoo-placement-') as directory:
    temp = Path(directory)
    sources = []
    for name, body in fixtures.items():
        path = temp / (name.replace('.', '/') + '.java')
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text('package ' + name.rsplit('.', 1)[0] + ';\n' + body)
        sources.append(str(path))
    sources.append(str(root / 'common/src/main/java/com/blackgear/vanillabackport/common/integrations/compat/wwoo/WwooBiomeCompatibility.java'))
    subprocess.run([java, '-m', 'jdk.compiler/com.sun.tools.javac.Main', '-d', str(temp / 'classes'), *sources], check=True)
    subprocess.run([java, '-cp', str(temp / 'classes'), 'test.PlacementTest'], check=True)
