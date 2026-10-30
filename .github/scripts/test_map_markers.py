"""Check official 26.3 marker sprites and the production direction calculation."""
import hashlib, os, subprocess, tempfile
from pathlib import Path
root=Path(__file__).resolve().parents[2]
expected={'player': 'e63b188b041f16f6223db0fe94e5f48b1e38c4190dc106e3fa542382705dc2bc', 'player_off_map': 'a491f58cad5c0f33ae7e25770e9c12c2f49b26b451346e9c25900dc565721d54', 'player_off_limits': 'e7dc862cb0e267f7534769eb74d152a3d0016df698719b6d55942d7a8f7b4652'}
for name,digest in expected.items():
 p=root/'common/src/main/resources/assets/minecraft/textures/map/decorations'/f'{name}.png'
 assert hashlib.sha256(p.read_bytes()).hexdigest()==digest,name
java=Path(os.environ['JAVA_HOME'])/'bin/java' if 'JAVA_HOME' in os.environ else Path('java')
source=root/'common/src/main/java/com/blackgear/vanillabackport/common/level/items/maps/MapMarkerRotation.java'
harness="""
import com.blackgear.vanillabackport.common.level.items.maps.MapMarkerRotation;
public class MarkerRotationTest {
 static void check(double yaw,boolean nether,long time,int expected){
  int actual=MapMarkerRotation.calculate(yaw,nether,time);
  if(actual!=expected)throw new AssertionError(yaw+" at "+time+": "+actual+" != "+expected);
 }
 public static void main(String[]args){
  check(0,false,0,0);check(90,false,0,4);check(180,false,0,8);check(270,false,0,12);
  check(-90,false,0,-4);check(-180,false,0,-8);check(360,false,0,16);
  check(0,true,0,0);check(90,true,0,0);check(0,true,10,3);check(180,true,10,3);
  check(0,true,100,10);check(0,true,1000,9);
  System.out.println("PASS: cardinal directions, negative yaw, wraparound and vanilla Nether spin");
 }
}
"""
with tempfile.TemporaryDirectory() as directory:
 p=Path(directory);test=p/'MarkerRotationTest.java';test.write_text(harness)
 subprocess.run([str(java),'-m','jdk.compiler/com.sun.tools.javac.Main','-d',str(p),str(source),str(test)],check=True)
 subprocess.run([str(java),'-cp',str(p),'MarkerRotationTest'],check=True)
print('PASS: all three player marker PNGs match official 26.3')
