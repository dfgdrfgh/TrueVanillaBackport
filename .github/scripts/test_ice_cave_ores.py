"""Exercise the production ore wrapper/traversal in deterministic voxel fixtures.

The fixtures model section access and native ore entry, while the production
per-voxel traversal, target selection, two passes and random stream are retained.
"""
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'fabric/src/main/java/com/blackgear/vanillabackport/core/fabric/IceCaveOreFeature.java'
JAVA = str(Path(os.environ['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in os.environ else 'java'
fixtures = {
'net.minecraft.core.BlockPos': '''public class BlockPos {
 protected int x,y,z;public BlockPos(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
 public int getX(){return x;}public int getY(){return y;}public int getZ(){return z;}
 public static class MutableBlockPos extends BlockPos{public MutableBlockPos(){super(0,0,0);}public MutableBlockPos set(int x,int y,int z){this.x=x;this.y=y;this.z=z;return this;}}
}''',
'net.minecraft.core.SectionPos':'public class SectionPos {public static int sectionRelative(int i){return i&15;}}',
'net.minecraft.tags.BlockTags':'public class BlockTags {public static final String STONE_ORE_REPLACEABLES="stone",DEEPSLATE_ORE_REPLACEABLES="deepslate";}',
'net.minecraft.util.Mth':'''public class Mth {public static double lerp(double t,double a,double b){return a+t*(b-a);}public static float sin(float v){return (float)Math.sin(v);}public static int floor(double v){return (int)Math.floor(v);}}''',
'net.minecraft.util.RandomSource':'''public class RandomSource {private final java.util.Random value;public int calls;public RandomSource(long seed){value=new java.util.Random(seed);}public double nextDouble(){calls++;return value.nextDouble();}public float nextFloat(){calls++;return value.nextFloat();}}''',
'net.minecraft.world.level.block.Block':'''public record Block(String name){public net.minecraft.world.level.block.state.BlockState defaultBlockState(){return new net.minecraft.world.level.block.state.BlockState(this);}}''',
'net.minecraft.world.level.block.Blocks':'''public class Blocks {public static final Block STONE=new Block("stone"),DEEPSLATE=new Block("deepslate"),PACKED_ICE=new Block("packed_ice"),CALCITE=new Block("calcite"),TUFF=new Block("tuff"),IRON_ORE=new Block("iron_ore"),DEEPSLATE_IRON_ORE=new Block("deepslate_iron_ore"),BEDROCK=new Block("bedrock");}''',
'net.minecraft.world.level.block.state.BlockState':'''import net.minecraft.world.level.block.*;public record BlockState(Block block){public boolean is(Block b){return block==b;}public boolean is(String tag){return tag.equals("stone")?is(Blocks.STONE):is(Blocks.DEEPSLATE)||is(Blocks.TUFF);}}''',
'net.minecraft.world.level.WorldGenLevel':'''import net.minecraft.core.BlockPos;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.*;
 public class WorldGenLevel {
 public java.util.Map<String,BlockState> states=new java.util.HashMap<>();public java.util.List<String> writes=new java.util.ArrayList<>();public int mode;
 public String key(int x,int y,int z){return x+","+y+","+z;}public BlockState get(int x,int y,int z){return states.getOrDefault(key(x,y,z),(mode==1?Blocks.BEDROCK:mode==2?Blocks.TUFF:Math.floorMod(x,2)==0?Blocks.PACKED_ICE:Blocks.CALCITE).defaultBlockState());}
 public void set(int x,int y,int z,BlockState s){states.put(key(x,y,z),s);writes.add(key(x,y,z));}
 public boolean isOutsideBuildHeight(int y){return y< -64||y>=320;}public boolean ensureCanWrite(BlockPos p){return Math.abs(p.getX())<=8&&Math.abs(p.getZ())<=8;}
 }''',
'net.minecraft.world.level.chunk.ChunkGenerator':'public class ChunkGenerator {}',
'net.minecraft.world.level.chunk.BulkSectionAccess':'''import net.minecraft.core.BlockPos;import net.minecraft.world.level.WorldGenLevel;import net.minecraft.world.level.block.state.BlockState;
 public class BulkSectionAccess implements AutoCloseable {private final WorldGenLevel level;public BulkSectionAccess(WorldGenLevel l){level=l;}public LevelChunkSection getSection(BlockPos p){return new LevelChunkSection(level,p.getX()>>4,p.getY()>>4,p.getZ()>>4);}public BlockState getBlockState(BlockPos p){return level.get(p.getX(),p.getY(),p.getZ());}public void close(){}}''',
'net.minecraft.world.level.chunk.LevelChunkSection':'''import net.minecraft.world.level.WorldGenLevel;import net.minecraft.world.level.block.state.BlockState;
 public record LevelChunkSection(WorldGenLevel level,int x,int y,int z){public BlockState getBlockState(int a,int b,int c){return level.get(x*16+a,y*16+b,z*16+c);}public void setBlockState(int a,int b,int c,BlockState state,boolean ignored){level.set(x*16+a,y*16+b,z*16+c,state);}}''',
'net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest':'''import net.minecraft.world.level.block.state.BlockState;import net.minecraft.util.RandomSource;public record TagMatchTest(String tag){public boolean test(BlockState state,RandomSource r){return state.is(tag);}}''',
'net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration':'''import java.util.List;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
 public class OreConfiguration {public static final Object CODEC=new Object();public final List<TargetBlockState> targetStates;public final int size;public final float discardChanceOnAirExposure;
 public OreConfiguration(List<TargetBlockState> t,int s,float d){targetStates=t;size=s;discardChanceOnAirExposure=d;}public static TargetBlockState target(TagMatchTest t,BlockState s){return new TargetBlockState(t,s);}public static class TargetBlockState{public final TagMatchTest target;public final BlockState state;public TargetBlockState(TagMatchTest t,BlockState s){target=t;state=s;}}}''',
'net.minecraft.world.level.levelgen.feature.Feature':'''public abstract class Feature<C>{public Feature(Object codec){}public abstract boolean place(FeaturePlaceContext<C> c);}''',
'net.minecraft.world.level.levelgen.feature.FeaturePlaceContext':'''import net.minecraft.core.BlockPos;import net.minecraft.util.RandomSource;import net.minecraft.world.level.WorldGenLevel;import net.minecraft.world.level.chunk.ChunkGenerator;
 public record FeaturePlaceContext<C>(Object topFeature,WorldGenLevel level,ChunkGenerator chunkGenerator,RandomSource random,BlockPos origin,C config){}''',
'net.minecraft.world.level.levelgen.feature.OreFeature':'''import java.util.*;import java.util.function.Function;import net.minecraft.core.BlockPos;import net.minecraft.util.RandomSource;import net.minecraft.world.level.WorldGenLevel;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
 public class OreFeature extends Feature<OreConfiguration>{public static final List<FeaturePlaceContext<OreConfiguration>> calls=new ArrayList<>();public OreFeature(Object codec){super(codec);}
 public boolean place(FeaturePlaceContext<OreConfiguration> c){calls.add(c);var p=c.origin();return doPlace(c.level(),c.random(),c.config(),p.getX()-1,p.getX()+1,p.getZ(),p.getZ(),p.getY(),p.getY(),p.getX()-8,p.getY()-8,p.getZ()-8,16,16);}
 protected boolean doPlace(WorldGenLevel l,RandomSource r,OreConfiguration c,double a,double b,double d,double e,double f,double g,int h,int i,int j,int k,int m){throw new AssertionError("production traversal must run");}
 public static boolean canPlaceOre(BlockState s,Function<BlockPos,BlockState> read,RandomSource r,OreConfiguration c,OreConfiguration.TargetBlockState t,BlockPos.MutableBlockPos p){return t.target.test(s,r)&&(c.discardChanceOnAirExposure<=0||r.nextFloat()>=c.discardChanceOnAirExposure);}
 }''',
}
harness = '''import java.util.*;import com.blackgear.vanillabackport.core.fabric.IceCaveOreFeature;import net.minecraft.core.*;import net.minecraft.tags.*;import net.minecraft.util.*;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.levelgen.feature.*;import net.minecraft.world.level.levelgen.feature.configurations.*;import net.minecraft.world.level.levelgen.structure.templatesystem.*;
public class OreRegression {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  var ore=new OreConfiguration(List.of(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),Blocks.IRON_ORE.defaultBlockState()),OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),Blocks.DEEPSLATE_IRON_ORE.defaultBlockState())),9,0);
  int ores=0,hosts=0;boolean above=false,below=false;
  for(int mode=0;mode<=2;mode++)for(int seed=0;seed<100;seed++){
   var a=new WorldGenLevel();a.mode=mode;var b=new WorldGenLevel();b.mode=mode;var origin=new BlockPos(0,0,0);var random=new RandomSource(seed);OreFeature.calls.clear();
   boolean ok=new IceCaveOreFeature().place(new FeaturePlaceContext<>(null,a,null,random,origin,ore));
   if(mode==1){check(!ok&&a.writes.isEmpty(),"protected blocks cannot become hosts or ores");check(OreFeature.calls.size()==1,"failed pocket stops sequence");continue;}
   check(ok,"replaceable pocket should place");check(OreFeature.calls.size()==2,"two ore passes");
   var pocket=OreFeature.calls.get(0);var original=OreFeature.calls.get(1);
   check(pocket.config().size==20&&pocket.config().discardChanceOnAirExposure==0,"fixed host pocket");
   check(original.config()==ore,"preserve original ore configuration");check(pocket.random()==random&&original.random()==random,"shared random source");check(pocket.origin()==origin&&original.origin()==origin,"same origin, no offset");
   new IceCaveOreFeature().place(new FeaturePlaceContext<>(null,b,null,new RandomSource(seed),origin,ore));check(a.states.equals(b.states),"repeatable geometry and random stream");
   for(var e:a.states.entrySet()){
    int y=Integer.parseInt(e.getKey().split(",")[1]);var state=e.getValue();
    if(state.is(Blocks.IRON_ORE)||state.is(Blocks.DEEPSLATE_IRON_ORE)){ores++;check(state.is(y>=0?Blocks.IRON_ORE:Blocks.DEEPSLATE_IRON_ORE),"ore uses visited voxel y");above|=y>=0;below|=y<0;}
    else{hosts++;check(state.is(y>=0?Blocks.STONE:Blocks.DEEPSLATE),"pocket host uses visited voxel y");}
   }
  }
  check(ores>0&&hosts>ores&&above&&below,"ores are embedded in larger hosts across y=0");
  System.out.println("PASS: production two-pass ore traversal across 300 seeded packed-ice/calcite, tuff and protected worlds; y=0, RNG, hosts, determinism, sequence failure");
 }
}'''
with tempfile.TemporaryDirectory(prefix='ice-ores-') as directory:
    temp = Path(directory)
    sources = []
    for name, body in fixtures.items():
        path = temp / (name.replace('.', '/') + '.java')
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text('package ' + name.rsplit('.', 1)[0] + ';\n' + body)
        sources.append(str(path))
    path = temp / 'OreRegression.java'
    path.write_text(harness)
    subprocess.run([JAVA, '-m', 'jdk.compiler/com.sun.tools.javac.Main', '-d', str(temp/'classes'), *sources, str(SOURCE), str(path)], check=True)
    subprocess.run([JAVA, '-cp', str(temp/'classes'), 'OreRegression'], check=True)
