"""Normalize the compiled Ice Caves source for the built-in Minecraft namespace.

Runs only on the pinned CI checkout, before building the Fabric 1.21.1 module.
Original upstream source remains unchanged for standalone users.
"""
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[2]
SOURCE=ROOT / "vendor/ice-caves/src/main/java/org/zerw/net/icecaves"
MAIN=SOURCE / "IceCavesFabric.java"
CLIENT=SOURCE / "client/IceCavesFabricClient.java"
TABS=SOURCE / "registry/ModCreativeTabs.java"

for path in sorted(SOURCE.rglob("*.java")):
    if path.name.endswith(("_forge.java", "_neoforge.java")) or "forgelike" in path.parts:
        continue
    content=path.read_text()
    changed=content.replace('method_60655("ice_caves",', 'method_60655("minecraft",')
    changed=changed.replace('"ice_caves:', '"minecraft:')
    if changed != content:
        path.write_text(changed)

java=MAIN.read_text()
# Stop registering the non-vanilla Ice Door. It is not a 26.4 Ice Caves feature.
door_lines=[
    '   public static class_2248 ICE_DOOR;\n',
    '   public static class_1747 ICE_DOOR_ITEM;\n',
    '      ICE_DOOR = (class_2248)class_2378.method_10230(class_7923.field_41175, ModBlocks.ICE_DOOR_ID, ModBlocks.createIceDoor());\n',
    '      ICE_DOOR_ITEM = (class_1747)class_2378.method_10230(class_7923.field_41178, ModItems.ICE_DOOR_ID, ModItems.createIceDoorItem(ICE_DOOR));\n',
    '      class_2378.method_10230(class_7923.field_41172, ModSounds.ICE_DOOR_OPEN_ID, ModSounds.ICE_DOOR_OPEN);\n',
    '      class_2378.method_10230(class_7923.field_41172, ModSounds.ICE_DOOR_CLOSE_ID, ModSounds.ICE_DOOR_CLOSE);\n',
    '                     ICE_DOOR_ITEM,\n',
]
for line in door_lines:
    if java.count(line)!=1:
        raise RuntimeError(f"Ice Door source changed: {line.strip()} count={java.count(line)}")
    java=java.replace(line,"")
# This content is integrated into TVB; normal vanilla categories are inserted
# by TVB with anchored addAfter rather than appended at the ends by the old mod.
hit=java.find("ItemGroupEvents.modifyEntriesEvent(")
start=java.rfind("\n", 0, hit) + 1 if hit >= 0 else -1
end=java.find("BiomeModifications.addFeature(",hit)
end=java.rfind("\n", 0, end) + 1 if end >= 0 else -1
if start < 0 or end < 0 or java[start:end].count("ItemGroupEvents.modifyEntriesEvent")!=4:
    raise RuntimeError("Expected exactly four old unsorted creative events")
java=java[:start]+java[end:]
MAIN.write_text(java)

client=CLIENT.read_text()
line='      BlockRenderLayerMap.INSTANCE.putBlock(IceCavesFabric.ICE_DOOR, class_1921.method_23581());\n'
if client.count(line)!=1:
    raise RuntimeError("Ice Door render hook changed")
CLIENT.write_text(client.replace(line,""))

tabs=TABS.read_text()
for line in ('      class_1935 iceDoor,\n','         new class_1799(iceDoor),\n'):
    if tabs.count(line)!=1:
        raise RuntimeError("Standalone creative method changed")
    tabs=tabs.replace(line,"")
TABS.write_text(tabs)
assert "ICE_DOOR_ITEM" not in MAIN.read_text()
assert "ICE_DOOR" not in CLIENT.read_text()
assert "iceDoor" not in TABS.read_text()
print("PASS: minecraft: Ice Caves registry IDs; Ice Door and old creative insertion removed")
