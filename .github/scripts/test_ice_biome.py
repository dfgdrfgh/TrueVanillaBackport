"""Validate true cave climate, noise bands, biome schema and official ore resources.

Optionally pass the official 26.4-snapshot-6 client JAR to compare every converted
ore size/discard/placement and every mob spawn against its primary source.
"""
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
WORLDGEN = ROOT / 'fabric/src/main/resources/data/minecraft/worldgen'
COMMON = ROOT / 'common/src/main/java/com/blackgear/vanillabackport'
FABRIC = ROOT / 'fabric/src/main/java/com/blackgear/vanillabackport/core/fabric'
biome = json.loads((WORLDGEN/'biome/ice_caves.json').read_text())
assert biome['temperature'] == 0 and biome['downfall'] == 0 and biome['has_precipitation']
assert len(biome['features']) == 11 and biome['carvers'] == {'air':['minecraft:cave','minecraft:cave_extra_underground','minecraft:canyon']}
assert biome['features'][7] == ['minecraft:ice_cave'], 'Decoration must be inside the actual biome'
expected = {'creeper':(100,4,4),'skeleton':(90,4,4),'stray':(10,4,4),'frostbite':(50,4,4),'slime':(100,4,4),'spider':(100,4,4),'zombie':(50,4,4),'enderman':(10,1,4),'witch':(5,1,1),'zombie_villager':(5,1,1)}
assert {s['type'].split(':')[1]:(s['weight'],s['minCount'],s['maxCount']) for s in biome['spawners']['monster']} == expected
assert biome['spawners']['ambient'] == [{'type':'minecraft:bat','weight':10,'minCount':8,'maxCount':8}]
assert biome['spawners']['underground_water_creature'] == [{'type':'minecraft:glow_squid','weight':10,'minCount':2,'maxCount':4}]
for key in ['water_color','water_fog_color','fog_color']:
    assert biome['effects'][key] == 0x14559b
assert biome['effects']['sky_color'] == 0x7fa1ff
assert json.loads((ROOT/'common/src/main/generated/data/minecraft/worldgen/noise/ice_cave_gradient.json').read_text()) == {'firstOctave':-4,'amplitudes':[1,0,1]}
assert 'getNoiseSampler3D(this.noise)' in (COMMON/'common/level/worldgen/surface/SpatialNoiseThresholdConditionSource.java').read_text()
assert not (ROOT/'common/src/main/generated/data/minecraft/worldgen/biome/ice_caves.json').exists(), 'Frostbite biome is Fabric-only'
assert not list((ROOT/'neoforge').rglob('ice_caves.json')), 'No unresolved Frostbite biome on NeoForge'
surface_tag = json.loads((WORLDGEN.parent/'tags/worldgen/biome/spawns_frostbite_surface.json').read_text())
assert set(surface_tag['values']) == {'minecraft:'+x for x in ['frozen_ocean','deep_frozen_ocean','frozen_river','frozen_peaks','ice_spikes','snowy_plains','snowy_slopes','jagged_peaks']}
configured = sorted((WORLDGEN/'configured_feature').glob('ice_cave_ore_*.json'))
placed = sorted((WORLDGEN/'placed_feature').glob('ice_cave_ore_*.json'))
assert len(configured) == 16 and len(placed) == 21, (len(configured),len(placed))
for path in configured:
    feature = json.loads(path.read_text());config = feature['config']
    if path.stem in ['ice_cave_ore_gravel','ice_cave_ore_diorite','ice_cave_ore_andesite']:
        assert feature['type'] == 'minecraft:ore'
        assert config['size'] == (33 if 'gravel' in path.stem else 64)
        assert [t['target'] for t in config['targets']] == [
            {'predicate_type':'minecraft:tag_match','tag':'minecraft:base_stone_overworld'},
            {'predicate_type':'minecraft:block_match','block':'minecraft:packed_ice'},
            {'predicate_type':'minecraft:block_match','block':'minecraft:calcite'}]
    else:
        assert feature['type'] == 'vanillabackport:ice_cave_ore'
        assert [t['target']['tag'] for t in config['targets']] == ['minecraft:stone_ore_replaceables','minecraft:deepslate_ore_replaceables']
        assert len(config['targets']) == 2, 'Original ore must not directly replace ice or calcite'
    assert all(set(t['state']) == {'Name'} for t in config['targets']), '1.21.1 BlockState codec'
for path in placed:
    data = json.loads(path.read_text())
    assert data['placement'][-1] == {'type':'minecraft:biome'}
    assert (WORLDGEN/'configured_feature'/(data['feature'].split(':')[1]+'.json')).is_file()
if len(sys.argv) > 1:
    with zipfile.ZipFile(sys.argv[1]) as official:
        for path in placed:
            assert json.loads(path.read_text()) == json.loads(official.read('data/minecraft/worldgen/placed_feature/'+path.name)), path
        for path in configured:
            reference = json.loads(official.read('data/minecraft/worldgen/feature/'+path.name))
            if reference['type'] == 'minecraft:sequence':
                pocket = reference['features'][0]['feature']
                assert pocket['size'] == 20 and pocket['discard_chance_on_air_exposure'] == 0
                reference = reference['features'][1]['feature']
            config = json.loads(path.read_text())['config']
            assert (config['size'],config['discard_chance_on_air_exposure']) == (reference['size'],reference['discard_chance_on_air_exposure']), path
            assert [t['state']['Name'] for t in config['targets'][:2]] == [t['state'] for t in reference['targets'][:2]] or len(reference['targets']) == 1
        for cat,spawns in biome['spawners'].items():
            ref = json.loads(official.read('data/minecraft/worldgen/biome/ice_caves.json'))['attributes']['minecraft:gameplay/natural_mob_spawns']['argument']['spawns_by_category'][cat]
            for actual,expect in zip(spawns,ref,strict=True):
                count=expect['count'];lo=hi=count if isinstance(count,int) else None
                if isinstance(count,dict):lo=count['min_inclusive'];hi=count['max_inclusive']
                assert actual == {'type':expect['type'],'weight':expect['weight'],'minCount':lo,'maxCount':hi}

fixtures = {
'com.mojang.datafixers.util.Pair':'public record Pair<A,B>(A first,B second){public static <A,B> Pair<A,B> of(A a,B b){return new Pair<>(a,b);}}',
'com.google.common.collect.ImmutableList':'''public class ImmutableList {public static <T>Builder<T> builder(){return new Builder<>();}public static class Builder<T>{private final java.util.List<T> list=new java.util.ArrayList<>();public void addAll(java.util.List<T> v){list.addAll(v);}public void add(T v){list.add(v);}public java.util.List<T> build(){return list;}}}''',
'net.fabricmc.api.ModInitializer':'public interface ModInitializer{void onInitialize();}',
'net.minecraft.resources.ResourceLocation':'public record ResourceLocation(String id){public static ResourceLocation withDefaultNamespace(String id){return new ResourceLocation("minecraft:"+id);}public static ResourceLocation fromNamespaceAndPath(String n,String p){return new ResourceLocation(n+":"+p);}}',
'net.minecraft.resources.ResourceKey':'public record ResourceKey<T>(ResourceLocation location){public static <T>ResourceKey<T> create(Object registry,ResourceLocation l){return new ResourceKey<>(l);}}',
'net.minecraft.core.registries.Registries':'public class Registries{public static final Object BIOME=new Object();}',
'net.minecraft.core.registries.BuiltInRegistries':'public class BuiltInRegistries{public static final Object FEATURE=new Object();}',
'net.minecraft.core.Registry':'public class Registry{public static <T>T register(Object r,Object k,T v){return v;}}',
'net.minecraft.world.level.biome.Biome':'public class Biome{}',
'net.minecraft.world.level.biome.Climate':'''public class Climate{public record Parameter(float min,float max){public static Parameter span(float a,float b){return new Parameter(a,b);}}public record Point(Parameter temperature,Parameter humidity,Parameter continentalness,Parameter erosion,Parameter depth,Parameter weirdness,float offset){}public static Point parameters(Parameter a,Parameter b,Parameter c,Parameter d,Parameter e,Parameter f,float g){return new Point(a,b,c,d,e,f,g);}}''',
'com.blackgear.platform.common.worldgen.placement.BiomePlacement':'''import java.util.*;import java.util.function.Consumer;import com.mojang.datafixers.util.Pair;public class BiomePlacement{public static class Event{public final List<Pair<?,?>> values=new ArrayList<>();public void add(Pair<?,?> p){values.add(p);}}public static void registerBiomePlacements(Consumer<Event> e){}}''',
'com.blackgear.vanillabackport.core.VanillaBackport':'''public class VanillaBackport{public static final Config COMMON_CONFIG=new Config();public static class Config{public Flag hasIceCaves=new Flag();}public static class Flag{public boolean value=true;public boolean get(){return value;}}}''',
'com.blackgear.vanillabackport.core.fabric.IceCaveOreFeature':'public class IceCaveOreFeature{}',
'net.minecraft.world.level.levelgen.synth.NormalNoise':'public class NormalNoise{public static class NoiseParameters{}}',
'com.blackgear.vanillabackport.common.registries.worldgen.ModNoises':'''import net.minecraft.resources.*;import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters;public class ModNoises{public static final ResourceKey<NoiseParameters> ICE_CAVE_GRADIENT=new ResourceKey<>(new ResourceLocation("ice")),SULFUR_CAVE_GRADIENT=new ResourceKey<>(new ResourceLocation("sulfur")),SMALL_PATCH=new ResourceKey<>(new ResourceLocation("patch"));}''',
'com.blackgear.vanillabackport.common.registries.worldgen.ModBiomes':'''import net.minecraft.resources.*;import net.minecraft.world.level.biome.Biome;public class ModBiomes{public static final ResourceKey<Biome> DAPPLED_FOREST=new ResourceKey<>(new ResourceLocation("dappled")),SULFUR_CAVES=new ResourceKey<>(new ResourceLocation("sulfur"));}''',
'net.minecraft.world.level.block.Block':'public record Block(String name){public String defaultBlockState(){return name;}}',
'net.minecraft.world.level.block.Blocks':'public class Blocks{public static final Block STONE=new Block("stone"),COARSE_DIRT=new Block("dirt"),BEDROCK=new Block("bedrock"),CALCITE=new Block("calcite"),PACKED_ICE=new Block("packed_ice");}',
'com.blackgear.vanillabackport.common.registries.blocks.ModBlocks':'''import net.minecraft.world.level.block.Block;public class ModBlocks{public record Value(Block get){}public static final Value CINNABAR=new Value(new Block("cinnabar")),SULFUR=new Value(new Block("sulfur"));}''',
'net.minecraft.world.level.levelgen.VerticalAnchor':'public class VerticalAnchor{public static VerticalAnchor bottom(){return new VerticalAnchor();}public static VerticalAnchor aboveBottom(int y){return new VerticalAnchor();}}',
'net.minecraft.world.level.levelgen.SurfaceRules':'''import net.minecraft.resources.ResourceKey;import net.minecraft.world.level.biome.Biome;public class SurfaceRules{
 public record Context(String biome,double noise,boolean above,boolean bedrock){}
 public interface ConditionSource{boolean test(Context c);}public interface RuleSource{String apply(Context c);}
 public static RuleSource state(String s){return c->s;}public static RuleSource sequence(RuleSource... rules){return c->{for(var r:rules){var s=r.apply(c);if(s!=null)return s;}return null;};}
 public static RuleSource ifTrue(ConditionSource q,RuleSource r){return c->q.test(c)?r.apply(c):null;}public static ConditionSource isBiome(ResourceKey<Biome> b){return c->c.biome().equals(b.location().id());}
 public static ConditionSource abovePreliminarySurface(){return Context::above;}public static ConditionSource verticalGradient(String n,VerticalAnchor a,VerticalAnchor b){return Context::bedrock;}public static ConditionSource noiseCondition(Object n,float threshold){return c->c.noise()>=threshold;}
}''',
'com.blackgear.vanillabackport.common.level.worldgen.surface.SpatialNoiseThresholdConditionSource':'''import net.minecraft.world.level.levelgen.SurfaceRules;public record SpatialNoiseThresholdConditionSource(Object key,double min,double max) implements SurfaceRules.ConditionSource{public boolean test(SurfaceRules.Context c){return c.noise()>=min&&c.noise()<=max;}}''',
}
harness = '''import com.blackgear.vanillabackport.core.fabric.IceCavesWorldgen;import com.blackgear.vanillabackport.core.VanillaBackport;import com.blackgear.platform.common.worldgen.placement.BiomePlacement;import com.blackgear.vanillabackport.common.worldgen.surface.ModSurfaceRuleData;import net.minecraft.world.level.biome.Climate;import net.minecraft.world.level.levelgen.SurfaceRules;
public class BiomeRegression{
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  var event=new BiomePlacement.Event();IceCavesWorldgen.bootstrap(event);check(event.values.size()==1,"one narrow cave slot");var point=(Climate.Point)event.values.getFirst().first();
  var actual=new Climate.Parameter[]{point.temperature(),point.humidity(),point.continentalness(),point.erosion(),point.depth(),point.weirdness()};float[][] expected={{-1,-.8f},{-1,.7f},{-1.05f,-.19f},{-1,1},{.2f,.9f},{.5f,1.1f}};
  for(int i=0;i<6;i++)check(actual[i].min()==expected[i][0]&&actual[i].max()==expected[i][1],"climate axis "+i);check(point.offset()==0,"zero offset");
  VanillaBackport.COMMON_CONFIG.hasIceCaves.value=false;event=new BiomePlacement.Event();IceCavesWorldgen.bootstrap(event);check(event.values.isEmpty(),"disabled config adds no slot");
  var rules=ModSurfaceRuleData.makeRules();double[] noise={-.7,-.6f,-.5f,-.4,.2f,.25,.3f,.4,.5f,.6,.7f,.8};String[] bands={null,"calcite","calcite","packed_ice","packed_ice","calcite","calcite","packed_ice","packed_ice","calcite","calcite",null};
  for(int i=0;i<noise.length;i++){
   var below=new SurfaceRules.Context("minecraft:ice_caves",noise[i],false,false);check(java.util.Objects.equals(rules.apply(below),bands[i]),"underground noise boundary "+noise[i]);
   var above=new SurfaceRules.Context("minecraft:ice_caves",noise[i],true,false);check(java.util.Objects.equals(rules.apply(above),bands[i]==null?"stone":bands[i]),"surface fallback "+noise[i]);
   check(rules.apply(new SurfaceRules.Context("minecraft:ice_caves",noise[i],false,true)).equals("bedrock"),"bedrock preserved");
   check(rules.apply(new SurfaceRules.Context("minecraft:plains",noise[i],false,false))==null,"ordinary biome untouched");
  }
  System.out.println("PASS: production climate slot/config, inclusive noise bands and endpoints, surface/underground fallback, bedrock and ordinary biomes");
 }
}'''
java = str(Path(os.environ['JAVA_HOME'])/'bin/java') if 'JAVA_HOME' in os.environ else 'java'
with tempfile.TemporaryDirectory(prefix='ice-biome-') as directory:
    temp=Path(directory);sources=[]
    for name,body in fixtures.items():
        path=temp/(name.replace('.','/')+'.java');path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text('package '+name.rsplit('.',1)[0]+';\n'+body);sources.append(str(path))
    path=temp/'BiomeRegression.java';path.write_text(harness)
    sources += [str(FABRIC/'IceCavesWorldgen.java'),str(COMMON/'common/registries/worldgen/IceCavesBiomes.java'),str(COMMON/'common/worldgen/surface/ModSurfaceRuleData.java'),str(path)]
    subprocess.run([java,'-m','jdk.compiler/com.sun.tools.javac.Main','-d',str(temp/'classes'),*sources],check=True)
    subprocess.run([java,'-cp',str(temp/'classes'),'BiomeRegression'],check=True)
print('PASS: Fabric-only 1.21.1 biome schema, complete spawn table, 16 ore configurations and 21 placements'+(' compared to official snapshot' if len(sys.argv)>1 else ''))

assert not (ROOT / "fabric/src/main/resources/data/minecraft/worldgen/noise/ice_cave_gradient.json").exists(), "Duplicate Fabric/common resource breaks processResources"
