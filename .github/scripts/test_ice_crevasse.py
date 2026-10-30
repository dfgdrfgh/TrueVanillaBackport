"""Exercise the actual 1.21.1 crevasse port against deterministic voxel worlds."""
import os
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
source = root / '.github/ice-caves-overrides/worldgen/VanillaCrevasse.java'
assert source.is_file(), 'Missing vanilla crevasse implementation'
java = str(Path(os.environ['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in os.environ else 'java'
fixtures = {
'class_2338': '''public record class_2338(int x,int y,int z) {
 public int method_10263(){return x;} public int method_10264(){return y;} public int method_10260(){return z;}
}''',
'class_2248': '''public class class_2248 {
 final String name; public class_2248(String name){this.name=name;} public class_2680 method_9564(){return new class_2680(this);}
}''',
'class_2680': '''public record class_2680(class_2248 block) {
 public boolean method_26215(){return block==class_2246.field_10124;}
 public boolean method_27852(class_2248 b){return block==b;}
 public boolean method_26164(Object tag){return block==class_2246.STONE;}
 public class_2248 method_26204(){return block;}
 public boolean method_51366(){return !method_26215() && block!=class_2246.field_10382;}
}''',
'class_2246': '''public class class_2246 {
 public static final class_2248 field_10124=new class_2248("air"),field_10382=new class_2248("water"),field_10225=new class_2248("packed_ice"),field_27114=new class_2248("calcite"),field_27165=new class_2248("tuff"),STONE=new class_2248("stone"),PROTECTED=new class_2248("protected"),BRITTLE=new class_2248("brittle_age0");
}''',
'class_3481': 'public class class_3481 {public static final Object field_28992=new Object(),field_28993=new Object(),field_41282=new Object();}',
'class_3610': 'public record class_3610(boolean empty) {public boolean method_15769(){return empty;}}',
'class_5819': '''public class class_5819 {
 private final java.util.Random r; public class_5819(long s){r=new java.util.Random(s);}
 public int method_43048(int n){return r.nextInt(n);} public float method_43057(){return r.nextFloat();} public double method_43059(){return r.nextGaussian();}
}''',
'class_5281': '''public class class_5281 {
 public final java.util.Map<class_2338,class_2680> writes=new java.util.HashMap<>();
 public final java.util.Map<class_2338,class_2680> initial=new java.util.HashMap<>();
 public boolean tiny=false; public int minY=-64;
 public class_2680 method_8320(class_2338 p){
  if(writes.containsKey(p))return writes.get(p); if(initial.containsKey(p))return initial.get(p);
  return ((tiny && (Math.abs(p.x())>1||Math.abs(p.z())>1)) || p.y()>20 ? class_2246.field_10124 : class_2246.field_10225).method_9564();
 }
 public boolean method_8652(class_2338 p,class_2680 s,int flags){if(!method_37368(p))throw new AssertionError("out of generation bounds");writes.put(p,s);return true;}
 public int method_31607(){return minY;}
 public boolean method_37368(class_2338 p){return p.y()>=minY&&p.y()<320&&Math.abs(p.x())<=32&&Math.abs(p.z())<=32;}
 public class_3610 method_8316(class_2338 p){return new class_3610(!method_8320(p).method_27852(class_2246.field_10382));}
}''',
}
harness = '''package org.zerw.net.icecaves.worldgen;
import net.minecraft.*;
public class CrevasseTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  long totalWater=0;
  for(int seed=0;seed<100;seed++){
   var a=new class_5281();var b=new class_5281();var origin=new class_2338(0,20,0);
   check(VanillaCrevasse.place(a,origin,new class_5819(seed),class_2246.BRITTLE),"flat floor must accept seed "+seed);
   check(VanillaCrevasse.place(b,origin,new class_5819(seed),class_2246.BRITTLE),"repeat placement");
   check(a.writes.equals(b.writes),"deterministic random traversal");
   long caps=a.writes.entrySet().stream().filter(e->e.getValue().method_27852(class_2246.BRITTLE)).count();
   check(caps>=10,"connected minimum footprint");
   long water=a.writes.entrySet().stream().filter(e->e.getValue().method_27852(class_2246.field_10382)).count();
   totalWater+=water; // Vanilla can be dry when fillDepth exceeds the sampled carve depth.
   for(var e:a.writes.entrySet()){
    var p=e.getKey();var state=e.getValue();
    check(p.y()<=20&&p.y()>=10,"bounded depth and no ceiling mutation");
    if(state.method_27852(class_2246.BRITTLE))check(p.y()==20,"fresh cap at cave floor");
    if(state.method_27852(class_2246.field_10382)){
     check(!a.method_8320(new class_2338(p.x(),p.y()-1,p.z())).method_26215(),"sealed water bottom");
     for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})
      check(!a.method_8320(new class_2338(p.x()+d[0],p.y(),p.z()+d[1])).method_26215(),"sealed water sides");
    }
   }
  }
  check(totalWater>0,"vanilla fill-depth sampling produces water across seeds");
  var tiny=new class_5281();tiny.tiny=true;
  check(!VanillaCrevasse.place(tiny,new class_2338(0,20,0),new class_5819(0),class_2246.BRITTLE),"footprint below ten rejected");
  check(tiny.writes.isEmpty(),"rejection is atomic");
  var low=new class_5281();low.minY=16;
  check(!VanillaCrevasse.place(low,new class_2338(0,20,0),new class_5819(0),class_2246.BRITTLE),"bottom boundary rejected");
  check(low.writes.isEmpty(),"no bottom boundary writes");
  var protectedWorld=new class_5281();var protectedPos=new class_2338(0,18,0);
  protectedWorld.initial.put(protectedPos,class_2246.PROTECTED.method_9564());
  VanillaCrevasse.place(protectedWorld,new class_2338(0,20,0),new class_5819(0),class_2246.BRITTLE);
  check(protectedWorld.method_8320(protectedPos).method_27852(class_2246.PROTECTED),"never carve nonreplaceable block");
  System.out.println("PASS: 100 seeded crevasses with vanilla water sampling, deterministic geometry, sealed fluid, caps, footprint rejection, world bounds, protected blocks");
 }
}'''
with tempfile.TemporaryDirectory(prefix='ice-crevasse-') as directory:
    temp=Path(directory)
    sources=[]
    for name,body in fixtures.items():
        path=temp/'net/minecraft'/f'{name}.java'
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text('package net.minecraft;\n'+body)
        sources.append(str(path))
    test=temp/'CrevasseTest.java'; test.write_text(harness); sources.extend([str(test),str(source)])
    subprocess.run([java,'-m','jdk.compiler/com.sun.tools.javac.Main','-d',str(temp/'classes'),*sources],check=True)
    subprocess.run([java,'-cp',str(temp/'classes'),'org.zerw.net.icecaves.worldgen.CrevasseTest'],check=True)
