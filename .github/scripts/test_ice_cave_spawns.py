"""Exercise spawn scoping using cold surface, ordinary cave, and decorated cave worlds."""
import os
from pathlib import Path
import subprocess
import tempfile
root=Path(__file__).resolve().parents[2]
source=root/'.github/ice-caves-overrides/worldgen/IceCaveSpawnControl.java'
assert source.is_file(), 'Missing scoped Ice Caves spawn implementation'
java=str(Path(os.environ['JAVA_HOME'])/'bin/java') if 'JAVA_HOME' in os.environ else 'java'
fixtures={
'net.minecraft.class_2338': '''public record class_2338(int x,int y,int z){public int method_10263(){return x;}public int method_10264(){return y;}public int method_10260(){return z;}public class_2338 method_10084(){return new class_2338(x,y+1,z);}public class_2338 method_10074(){return new class_2338(x,y-1,z);}}''',
'net.minecraft.class_2248':'public class class_2248 {}',
'net.minecraft.class_2246':'''public class class_2246 {public static final class_2248 field_10124=new class_2248(),field_10225=new class_2248(),field_27114=new class_2248(),field_27879=new class_2248(),STONE=new class_2248();}''',
'net.minecraft.class_2680':'''public record class_2680(class_2248 block){public boolean method_26215(){return block==class_2246.field_10124;}public boolean method_27852(class_2248 b){return block==b;}public boolean method_26170(class_1936 l,class_2338 p,class_1299<?> t){return !method_26215();}}''',
'net.minecraft.class_1297':'public class class_1297 {}',
'net.minecraft.class_1308':'''public class class_1308 extends class_1297 {public class_1299<?> type;public class_2338 pos; public class_2338 method_24515(){return pos;}public class_1299<?> method_5864(){return type;}}''',
'net.minecraft.class_1299':'''public class class_1299<T> {public final String id;public class_1299(String id){this.id=id;}public static final class_1299<class_1308> field_6098=new class_1299<>("stray");}''',
'net.minecraft.class_1959':'public class class_1959 {}',
'net.minecraft.class_2960':'public record class_2960(String id){public static class_2960 method_60655(String ns,String p){return new class_2960(ns+":"+p);}}',
'net.minecraft.class_5321':'public class class_5321<T>{public static <T>class_5321<T> method_29179(Object r,class_2960 id){return new class_5321<>();}}',
'net.minecraft.class_6862':'public class class_6862<T>{public static <T>class_6862<T> method_40092(Object r,class_2960 id){return new class_6862<>();}}',
'net.minecraft.class_7924':'public class class_7924{public static Object field_41236=new Object();}',
'net.minecraft.class_7923':'''public class class_7923{public static final class_7923 field_41177=new class_7923();public class_2960 method_10221(class_1299<?> t){return new class_2960(t.id);}}''',
'net.minecraft.class_3730':'public enum class_3730{field_16459,field_16472,SPAWNER,CONVERSION,COMMAND,field_16463,field_16527,field_16474}',
'net.minecraft.class_5819':'public class class_5819 {}',
'net.minecraft.class_1936':'''public class class_1936{
 public boolean sky=false,tag=true,surface=true,trueBiome=false,floor=true,monsterRules=true;public int marker=0;
 public class_2680 method_8320(class_2338 p){
  if(p.y()==9)return new class_2680(floor?class_2246.STONE:class_2246.field_10124);
  if(marker==1&&p.x()==1&&p.y()==10)return new class_2680(org.zerw.net.icecaves.registry.ModBlocks.CRYSTAL);
  if(marker==2&&p.y()==10&&p.z()==0&&Math.abs(p.x())==1)return new class_2680(class_2246.field_10225);
  if(marker==2&&p.y()==11&&p.x()==0&&p.z()==0)return new class_2680(class_2246.field_27114);
  return new class_2680(class_2246.field_10124);
 }
 public boolean method_8311(class_2338 p){return sky;}public boolean method_22340(class_2338 p){return true;}
 public Holder method_23753(class_2338 p){return new Holder(tag,surface,trueBiome);}public record Holder(boolean tag,boolean surface,boolean trueBiome){public boolean method_40220(Object t){return t==org.zerw.net.icecaves.worldgen.IceCaveSpawnControl.FROSTBITE_SURFACE_BIOMES?surface:tag;}public boolean method_40225(Object k){return trueBiome;}}
}''',
'net.minecraft.class_5425':'public class class_5425 extends class_1936 {}',
'net.minecraft.class_1588':'''public class class_1588{public static boolean method_20680(class_1299<?> t,class_5425 l,class_3730 r,class_2338 p,class_5819 random){return l.monsterRules;}}''',
'org.zerw.net.icecaves.entity.FrostbiteZombie':'public class FrostbiteZombie extends net.minecraft.class_1308 {}',
'org.zerw.net.icecaves.registry.ModBlocks':'''public class ModBlocks{public static final net.minecraft.class_2248 CRYSTAL=new net.minecraft.class_2248(),ICICLE=new net.minecraft.class_2248(); public static net.minecraft.class_2248 iceCrystal(){return CRYSTAL;}public static net.minecraft.class_2248 icicle(){return ICICLE;}}''',
'org.zerw.net.icecaves.registry.ModEntities':'''public class ModEntities{public static final net.minecraft.class_2960 FROSTBITE_ID=new net.minecraft.class_2960("ice_caves:frostbite");}''',
}
harness='''package org.zerw.net.icecaves.worldgen;
import net.minecraft.*;import org.zerw.net.icecaves.entity.FrostbiteZombie;
public class SpawnTest{
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  var l=new class_5425();var p=new class_2338(0,10,0);var r=new class_5819();var natural=class_3730.field_16459;
  var frost=new class_1299<FrostbiteZombie>("ice_caves:frostbite");var creeper=new class_1299<class_1308>("creeper");
  check(!IceCaveSpawnControl.shouldBlockMonsterType(creeper,l,p,natural),"ordinary cold caves retain creepers");
  check(IceCaveSpawnControl.shouldBlockMonsterType(frost,l,p,natural),"ordinary cold caves reject frostbites");
  check(!IceCaveSpawnControl.checkFrostbiteSpawnRules(frost,l,natural,p,r),"registration predicate rejects ordinary cave");
  l.sky=true;check(IceCaveSpawnControl.checkFrostbiteSpawnRules(frost,l,natural,p,r),"cold surface permits frostbite");
  l.surface=false;check(!IceCaveSpawnControl.checkFrostbiteSpawnRules(frost,l,natural,p,r),"unlisted surface biome rejects frostbite even with sky");l.surface=true;
  l.sky=false;l.trueBiome=true;l.tag=false;
  check(IceCaveSpawnControl.checkFrostbiteSpawnRules(frost,l,natural,p,r),"true ice caves do not require legacy tag or decorations");
  check(IceCaveSpawnControl.shouldAllowCaveStraySpawn(class_1299.field_6098,l,p,natural,r),"true ice caves permit stray without decorations");
  l.trueBiome=false;l.tag=true;
  for(int marker=1;marker<=2;marker++){
   l.marker=marker;
   check(IceCaveSpawnControl.checkFrostbiteSpawnRules(frost,l,natural,p,r),"decorated cave permits frostbite");
   check(!IceCaveSpawnControl.shouldBlockMonsterType(creeper,l,p,natural),"decorated cave retains creeper");
   check(IceCaveSpawnControl.shouldAllowCaveStraySpawn(class_1299.field_6098,l,p,natural,r),"decorated cave permits stray");
  }
  l.floor=false;check(!IceCaveSpawnControl.checkFrostbiteSpawnRules(frost,l,natural,p,r),"no floating spawns");l.floor=true;
  l.monsterRules=false;check(!IceCaveSpawnControl.checkFrostbiteSpawnRules(frost,l,natural,p,r),"respect vanilla light difficulty rules");l.monsterRules=true;
  l.marker=0;check(!IceCaveSpawnControl.shouldBlockMonsterType(frost,l,p,class_3730.SPAWNER),"preserve spawners");
  check(!IceCaveSpawnControl.shouldBlockMonsterType(frost,l,p,class_3730.CONVERSION),"preserve conversion");
  l.marker=1;l.tag=false;check(!IceCaveSpawnControl.checkFrostbiteSpawnRules(frost,l,natural,p,r),"cave markers in untagged biome do not broaden natural spawn");
  System.out.println("PASS: ordinary cave hostiles, surface frostbites, decorated caves, floor and light rules, spawners, conversions, biome scope");
 }
}'''
with tempfile.TemporaryDirectory(prefix='ice-spawns-') as directory:
 temp=Path(directory);sources=[]
 for name,body in fixtures.items():
  path=temp/(name.replace('.','/')+'.java');path.parent.mkdir(parents=True,exist_ok=True)
  path.write_text('package '+name.rsplit('.',1)[0]+';\n'+body);sources.append(str(path))
 test=temp/'SpawnTest.java';test.write_text(harness);sources.extend([str(test),str(source)])
 subprocess.run([java,'-m','jdk.compiler/com.sun.tools.javac.Main','-d',str(temp/'classes'),*sources],check=True)
 subprocess.run([java,'-cp',str(temp/'classes'),'org.zerw.net.icecaves.worldgen.SpawnTest'],check=True)
