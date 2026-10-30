"""Run the production sulfur archetype codec and cold factory on real 1.21.1 classes.

Only mod sound/attribute/tag dependencies are replaced with vanilla holders; the
production archetype registry, codec, factory, predicates and effect are retained.
Pass a file containing the common Minecraft/Platform compile classpath.
"""
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
source = root / "common/src/main/java/com/blackgear/vanillabackport"
factory = source / "common/level/entities/mob/monster/sulfur_cube/SulfurCubeArchetypes.java"
classpath = Path(sys.argv[1]).read_text()
java = Path(os.environ["JAVA_HOME"]) / "bin/java"
sounds = sorted(set(re.findall(r"ModSoundEvents\.(\w+)", factory.read_text())))
tags = sorted(set(re.findall(r"ModItemTags\.(\w+)", factory.read_text())))
fixtures = {
    "com.blackgear.vanillabackport.core.VanillaBackport":
        'public class VanillaBackport { public static final String NAMESPACE = "minecraft"; }',
    "com.blackgear.vanillabackport.client.registries.ModSoundEvents": """
        import net.minecraft.core.Holder;
        import net.minecraft.core.registries.BuiltInRegistries;
        import net.minecraft.sounds.*;
        public class ModSoundEvents {
            %s
        }
    """ % "\n".join(f'public static final Holder<SoundEvent> {name} = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SLIME_JUMP);' for name in sounds),
    "com.blackgear.vanillabackport.common.registries.entities.ModAttributes": """
        import net.minecraft.core.Holder;
        import net.minecraft.world.entity.ai.attributes.*;
        public class ModAttributes {
            public static final Holder<Attribute> BOUNCINESS = Attributes.LUCK;
            public static final Holder<Attribute> FRICTION_MODIFIER = Attributes.MOVEMENT_SPEED;
            public static final Holder<Attribute> AIR_DRAG_MODIFIER = Attributes.MAX_HEALTH;
        }
    """,
    "com.blackgear.vanillabackport.common.registries.entities.ModDamageTypes": """
        import net.minecraft.resources.ResourceKey;
        import net.minecraft.world.damagesource.*;
        public class ModDamageTypes {
            public static final ResourceKey<DamageType> SULFUR_CUBE_HOT = DamageTypes.IN_FIRE;
        }
    """,
    "com.blackgear.vanillabackport.core.data.tags.ModItemTags": """
        import net.minecraft.core.registries.Registries;
        import net.minecraft.resources.ResourceLocation;
        import net.minecraft.tags.TagKey;
        import net.minecraft.world.item.Item;
        public class ModItemTags {
            %s
        }
    """ % "\n".join(f'public static final TagKey<Item> {name} = TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("{name.lower()}"));' for name in tags),
    "SulfurColdRegression": r"""
        import com.blackgear.vanillabackport.common.level.entities.mob.monster.sulfur_cube.SulfurCubeArchetype;
        import com.blackgear.vanillabackport.common.level.entities.mob.monster.sulfur_cube.SulfurCubeArchetypes;
        import com.google.gson.*;
        import com.mojang.serialization.JsonOps;
        import net.minecraft.SharedConstants;
        import net.minecraft.server.Bootstrap;
        import net.minecraft.core.*;
        import net.minecraft.core.registries.BuiltInRegistries;
        import net.minecraft.resources.RegistryOps;
        import net.minecraft.util.RandomSource;
        import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
        import net.minecraft.world.level.block.Blocks;
        import net.minecraft.world.level.gameevent.GameEvent;

        public class SulfurColdRegression {
            private static void require(boolean value, String message) {
                if (!value) throw new AssertionError(message);
            }
            private static JsonObject object(JsonObject value, String key) { return value.getAsJsonObject(key); }
            private static String string(JsonObject value, String key) { return value.get(key).getAsString(); }
            public static void main(String[] args) {
                SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
                var access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
                var ops = RegistryOps.create(JsonOps.INSTANCE, access);
                var cold = SulfurCubeArchetypes.REGISTRIES.get(SulfurCubeArchetypes.COLD);
                require(cold != null && cold.locationBasedEffects().size() == 1, "Cold archetype has no location effect");
                var conditional = cold.locationBasedEffects().getFirst();
                require(conditional.effect() instanceof ReplaceDisk, "Cold effect is not ReplaceDisk");
                var disk = (ReplaceDisk)conditional.effect();
                require(disk.radius().calculate(1) == 3 && disk.radius().calculate(5) == 3, "Cold radius is not constant 3");
                require(disk.height().calculate(1) == 1, "Cold height is not 1");
                require(disk.offset().equals(new Vec3i(0, -1, 0)), "Cold offset is not below cube");
                require(disk.blockState().getState(RandomSource.create(1), BlockPos.ZERO).is(Blocks.FROSTED_ICE), "Cold replacement is not frosted ice");
                require(disk.triggerGameEvent().orElseThrow().is(GameEvent.BLOCK_PLACE), "Cold block placement event missing");
                var encoded = SulfurCubeArchetype.CODEC.encodeStart(ops, cold).getOrThrow().getAsJsonObject();
                var decoded = SulfurCubeArchetype.CODEC.parse(ops, encoded).getOrThrow();
                require(decoded.locationBasedEffects().size() == 1, "Codec discarded location_based_effects");
                var encodedAgain = SulfurCubeArchetype.CODEC.encodeStart(ops, decoded).getOrThrow();
                require(encoded.equals(encodedAgain), "Cold effects do not survive codec roundtrip");
                var effect = encoded.getAsJsonArray("location_based_effects").get(0).getAsJsonObject();
                var predicate = object(object(effect, "effect"), "predicate");
                require(string(predicate, "type").equals("minecraft:all_of"), "Cold predicates are not conjunctive");
                var terms = predicate.getAsJsonArray("predicates");
                require(terms.size() == 4, "Cold block predicate count changed");
                JsonObject air = null, blocks = null, fluids = null;
                boolean unobstructed = false;
                for (var term : terms) {
                    var part = term.getAsJsonObject();
                    switch (string(part, "type")) {
                        case "minecraft:matching_block_tag" -> air = part;
                        case "minecraft:matching_blocks" -> blocks = part;
                        case "minecraft:matching_fluids" -> fluids = part;
                        case "minecraft:unobstructed" -> unobstructed = true;
                        default -> throw new AssertionError("Unexpected block predicate " + part);
                    }
                }
                require(air != null && string(air, "tag").equals("minecraft:air")
                    && air.get("offset").equals(JsonParser.parseString("[0,1,0]")), "Must require air above water");
                require(blocks != null && string(blocks, "blocks").equals("minecraft:water"), "Must require water block");
                require(fluids != null && string(fluids, "fluids").equals("minecraft:water"), "Must require source water fluid");
                require(unobstructed, "Must check collision before replacing water");
                var requirements = object(effect, "requirements");
                require(string(requirements, "condition").equals("minecraft:all_of"), "Entity restrictions are not conjunctive");
                var requirementsTerms = requirements.getAsJsonArray("terms");
                require(requirementsTerms.size() == 2, "Cold entity restrictions changed");
                boolean grounded = false, noVehicle = false;
                for (var term : requirementsTerms) {
                    var part = term.getAsJsonObject();
                    if (string(part, "condition").equals("minecraft:entity_properties")) {
                        require(string(part, "entity").equals("this"), "Ground restriction targets wrong entity");
                        grounded = object(object(part, "predicate"), "flags").get("is_on_ground").getAsBoolean();
                    } else if (string(part, "condition").equals("minecraft:inverted")) {
                        var inverted = object(part, "term");
                        require(string(inverted, "condition").equals("minecraft:entity_properties")
                            && string(inverted, "entity").equals("this"), "Passenger restriction targets wrong entity");
                        noVehicle = object(inverted, "predicate").has("vehicle");
                    }
                }
                require(grounded && noVehicle, "Cold effect requires grounded cube without a vehicle");
                var legacy = encoded.deepCopy(); legacy.remove("location_based_effects");
                var legacyDecoded = SulfurCubeArchetype.CODEC.parse(ops, legacy).getOrThrow();
                require(legacyDecoded.locationBasedEffects().isEmpty(), "Legacy archetype requires the new field");
                var regular = SulfurCubeArchetypes.REGISTRIES.get(SulfurCubeArchetypes.REGULAR);
                require(regular.locationBasedEffects().isEmpty(), "Regular archetype inherited cold effects");
                System.out.println("PASS: production cold factory, replace disk, air/source-water/collision predicates, grounded/non-passenger restrictions, legacy optional field and codec roundtrip");
            }
        }
    """,
}
with tempfile.TemporaryDirectory() as directory:
    folder = Path(directory)
    files = []
    for name, body in fixtures.items():
        path = folder / (name.replace(".", "/") + ".java")
        path.parent.mkdir(parents=True, exist_ok=True)
        package = name.rsplit(".", 1)[0] if "." in name else None
        path.write_text((f"package {package};\n" if package else "") + body)
        files.append(str(path))
    files += [str(factory), str(factory.with_name("SulfurCubeArchetype.java")),
              str(source / "core/util/AdditionalCodecs.java"), str(source / "core/util/Utilities.java")]
    subprocess.run([str(java.with_name("javac")), "-proc:none", "--release", "21", "-cp", classpath,
                    "-d", str(folder), *files], check=True)
    subprocess.run([str(java), "-cp", str(folder) + os.pathsep + classpath, "SulfurColdRegression"], check=True)
