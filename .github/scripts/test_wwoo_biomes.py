import os
from pathlib import Path
import tempfile,subprocess
root=Path(__file__).resolve().parents[2]
java=str(Path(os.environ['JAVA_HOME'])/'bin/java') if 'JAVA_HOME' in os.environ else 'java'
fixtures={
'net.minecraft.resources.ResourceLocation': '''public record ResourceLocation(String id) { public static ResourceLocation withDefaultNamespace(String path) { return new ResourceLocation("minecraft:"+path); } }''',
'net.minecraft.server.packs.resources.Resource': '''public record Resource(String sourcePackId) {}''',
'net.minecraft.server.packs.resources.ResourceManager': '''public interface ResourceManager { java.util.List<Resource> getResourceStack(net.minecraft.resources.ResourceLocation id); }''',
'com.blackgear.vanillabackport.core.VanillaBackport': '''public final class VanillaBackport { public static final Logger LOGGER = new Logger(); public static class Logger { public void info(String s,Object... args) {} } }''',
 'test.CompatibilityTest': '''
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import com.blackgear.vanillabackport.common.integrations.compat.wwoo.WwooBiomeCompatibility;
public class CompatibilityTest {
 static void check(boolean ok) { if (!ok) throw new AssertionError(); }
 public static void main(String[] args) {
  var pale=ResourceLocation.withDefaultNamespace("worldgen/biome/pale_garden.json");
  var older=ResourceLocation.withDefaultNamespace("worldgen/biome/forest.json");
  var otherRegistry=ResourceLocation.withDefaultNamespace("worldgen/placed_feature/pale_garden.json");
  var base=new Resource("mod/vanillabackport"); var wwoo=new Resource("wwoo:resources/vanilla_backport_compat");
  var user=new Resource("file/custom-biomes");
  Map<ResourceLocation,Resource> input=Map.of(pale,wwoo,older,wwoo,otherRegistry,wwoo);
  var result=WwooBiomeCompatibility.resolveBiomes(input,id->List.of(base,wwoo));
  check(result.get(pale)==base); check(result.get(older)==wwoo); check(result.get(otherRegistry)==wwoo); check(input.get(pale)==wwoo);
  result=WwooBiomeCompatibility.resolveBiomes(input,id->List.of(base,user,wwoo)); check(result.get(pale)==user);
  var custom=Map.of(pale,user); check(WwooBiomeCompatibility.resolveBiomes(custom,id->{throw new AssertionError();})==custom);
  check(WwooBiomeCompatibility.resolveBiomes(input,id->List.of(wwoo))==input);
  check(WwooBiomeCompatibility.resolveBiomes(Map.of(pale,base),id->{throw new AssertionError();}).get(pale)==base);
  for(String name:List.of("dappled_forest","sulfur_caves")) { var id=ResourceLocation.withDefaultNamespace("worldgen/biome/"+name+".json"); check(WwooBiomeCompatibility.resolveBiomes(Map.of(id,wwoo),key->List.of(base,wwoo)).get(id)==base); }
  System.out.println("PASS: WWOO override, older biomes, other registries, user datapack priority, absent fallback, no WWOO, and all backported biomes");
 }
}'''}
with tempfile.TemporaryDirectory() as directory:
 tmp=Path(directory); sources=[]
 for name,body in fixtures.items():
  p=tmp/(name.replace('.','/')+'.java');p.parent.mkdir(parents=True,exist_ok=True);p.write_text('package '+name.rsplit('.',1)[0]+';\n'+body);sources.append(str(p))
 sources.append(str(root/'common/src/main/java/com/blackgear/vanillabackport/common/integrations/compat/wwoo/WwooBiomeCompatibility.java'))
 subprocess.run([java,'-m','jdk.compiler/com.sun.tools.javac.Main','-d',str(tmp/'classes'),*sources],check=True)
 subprocess.run([java,'-cp',str(tmp/'classes'),'test.CompatibilityTest'],check=True)
