"""Run the production effect and hooks against 1.21.1's freezing tick sequence.

Minecraft boundary fixtures supply entity state; the effect and helper are the
actual build-time overrides. Before the overrides exist, the pinned vendor
effect and the unmodified vanilla thaw/damage paths reproduce the regression.
"""
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
OVERRIDES = ROOT / ".github/ice-caves-overrides"
effect = OVERRIDES / "effect/FreezingMobEffect.java"
if not effect.exists():
    effect = ROOT / "vendor/ice-caves/src/main/java/org/zerw/net/icecaves/effect/FreezingMobEffect.java"
helper = OVERRIDES / "effect/FreezingLogic.java"
mixin = OVERRIDES / "mixin/LivingEntityFreezingMixin.java"
java = str(Path(os.environ["JAVA_HOME"]) / "bin/java") if "JAVA_HOME" in os.environ else "java"

fixtures = {
    "net.minecraft.class_4081": "public class class_4081 { public static final class_4081 field_18272 = new class_4081(); }",
    "net.minecraft.class_1291": """public class class_1291 {
        public class_1291(class_4081 category, int color) {}
        public void method_52520(class_1309 mob, int amplifier) {}
        public boolean method_5572(class_1309 mob, int amplifier) { return true; }
        public boolean method_5552(int duration, int amplifier) { return false; }
    }""",
    "net.minecraft.class_6880": "public class class_6880<T> { public static class class_6883<T> extends class_6880<T> {} }",
    "net.minecraft.class_1309": """public class class_1309 {
        public int frozen;
        public boolean canFreeze = true, freezing;
        public int method_32312() { return frozen; }
        public int method_32315() { return 140; }
        public void method_32317(int value) { frozen = value; }
        public boolean method_32316() { return canFreeze; }
        public boolean method_6059(class_6880<class_1291> effect) { return freezing; }
    }""",
    "net.minecraft.class_5321": "public class class_5321<T> {}",
    "net.minecraft.class_8110": "public class class_8110 {}",
    "net.minecraft.class_8111": "public class class_8111 { public static final class_5321<class_8110> field_42354 = new class_5321<>(); }",
    "net.minecraft.class_1282": """public record class_1282(boolean freeze) {
        public boolean method_49708(class_5321<class_8110> key) { return freeze && key == class_8111.field_42354; }
    }""",
    "org.zerw.net.icecaves.registry.ModEffects": """import java.util.Optional;
        import net.minecraft.*;
        public class ModEffects {
            public static Optional<class_6880.class_6883<class_1291>> freezing() {
                return Optional.of(new class_6880.class_6883<>());
            }
        }""",
}
if not helper.exists():
    fixtures["org.zerw.net.icecaves.effect.FreezingLogic"] = """import net.minecraft.*;
        public class FreezingLogic {
            public static int thawTicks(class_1309 mob, int proposed) { return proposed; }
            public static float freezeDamage(class_1309 mob, class_1282 source, float proposed) { return proposed; }
        }"""

harness = """import net.minecraft.*;
import org.zerw.net.icecaves.effect.*;
public class FreezingRegression {
    static final FreezingMobEffect EFFECT = new FreezingMobEffect();
    static final class_1282 FREEZE = new class_1282(true), DROWNING = new class_1282(false);
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    // LivingEntity.baseTick ticks effects before aiStep. aiStep then handles
    // powder-snow accumulation OR two-tick thaw, followed by damage every 40 ticks.
    static float tick(class_1309 mob, int amplifier, int tick, boolean powderSnow) {
        if (mob.freezing) EFFECT.method_5572(mob, amplifier);
        if (powderSnow && mob.method_32316()) {
            mob.method_32317(Math.min(mob.method_32315(), mob.method_32312() + 1));
        } else {
            int proposed = Math.max(0, mob.method_32312() - 2);
            mob.method_32317(FreezingLogic.thawTicks(mob, proposed));
        }
        return tick % 40 == 0 && mob.method_32312() >= mob.method_32315() && mob.method_32316()
            ? FreezingLogic.freezeDamage(mob, FREEZE, 1.0F) : 0.0F;
    }
    public static void main(String[] args) {
        class_1309 mob = new class_1309(); mob.freezing = true;
        EFFECT.method_52520(mob, 0);
        for (int t = 1; t <= 10; t++) tick(mob, 0, t, false);
        check(mob.frozen == 10, "Freezing must grow by one per complete living tick outside powder snow; got " + mob.frozen);
        class_1309 added = new class_1309(); added.freezing = true;
        EFFECT.method_52520(added, 8);
        check(added.frozen == 0, "Effect addition must not freeze before its first tick");
        check(EFFECT.method_5552(1, 0) && EFFECT.method_5552(200, 12), "Freezing must apply every tick");
        for (int t = 11; t <= 500; t++) tick(mob, 0, t, false);
        check(mob.frozen == 140, "Freezing must cap at the vanilla threshold");
        class_1309 amplified = new class_1309(); amplified.freezing = true;
        for (int t = 1; t <= 200; t++) tick(amplified, 99, t, false);
        check(amplified.frozen == 140, "Amplification must not raise the cap");
        amplified.frozen = 0; tick(amplified, 99, 1, false);
        check(amplified.frozen == 1, "Amplification must not increase growth");
        mob.freezing = false; tick(mob, 0, 501, false);
        check(mob.frozen == 138, "Vanilla two-tick thaw must resume after the effect");
        class_1309 ordinary = new class_1309(); ordinary.frozen = 10;
        tick(ordinary, 0, 1, false);
        check(ordinary.frozen == 8, "Ordinary thaw changed");
        ordinary.frozen = 0; tick(ordinary, 0, 1, true);
        check(ordinary.frozen == 1, "Ordinary powder-snow accumulation changed");
        ordinary.freezing = true; ordinary.frozen = 0; tick(ordinary, 0, 1, true);
        check(ordinary.frozen == 2, "Powder snow and the effect must each add one tick");
        class_1309 immune = new class_1309(); immune.canFreeze = false; immune.freezing = true;
        EFFECT.method_52520(immune, 0);
        for (int t = 1; t <= 200; t++) check(tick(immune, 8, t, false) == 0.0F, "Immune mob took freeze damage");
        check(immune.frozen == 0, "Immune mob accumulated freezing");
        immune.frozen = 10; tick(immune, 8, 201, false);
        check(immune.frozen == 8, "Immune mob must still thaw while affected");
        mob.freezing = true; mob.frozen = 139;
        check(tick(mob, 0, 40, false) == 1.5F, "Affected fully frozen mob must take 1.5 damage");
        check(tick(mob, 0, 41, false) == 0.0F, "Freeze damage interval changed");
        check(tick(mob, 0, 80, false) == 1.5F, "Freeze damage must repeat every 40 ticks");
        check(FreezingLogic.freezeDamage(mob, DROWNING, 1.0F) == 1.0F, "Freezing changed drowning damage");
        mob.freezing = false;
        check(FreezingLogic.freezeDamage(mob, FREEZE, 1.0F) == 1.0F, "Ordinary freezing damage changed");
        System.out.println("PASS: production freezing growth, cap, amplifier, immunity, thaw, powder snow, damage amount/interval and drowning isolation");
    }
}
"""

with tempfile.TemporaryDirectory(prefix="ice-freezing-") as directory:
    temp = Path(directory)
    sources = []
    for name, body in fixtures.items():
        path = temp / (name.replace(".", "/") + ".java")
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text("package " + name.rsplit(".", 1)[0] + ";\n" + body)
        sources.append(str(path))
    test = temp / "FreezingRegression.java"
    test.write_text(harness)
    sources.extend([str(test), str(effect)])
    if helper.exists():
        sources.append(str(helper))
    # Compile the real ordinary-Mixin declarations too when the game runtime is
    # available locally. This catches unsupported injector signatures/dependencies.
    mixin_jars = sorted((ROOT.parent / "testing/libraries/net/fabricmc/sponge-mixin").glob("*/*.jar"))
    command = [java, "-m", "jdk.compiler/com.sun.tools.javac.Main", "-proc:none", "-d", str(temp / "classes")]
    if mixin.exists() and mixin_jars:
        command.extend(["-cp", str(mixin_jars[-1])])
        sources.append(str(mixin))
    subprocess.run([*command, *sources], check=True)
    subprocess.run([java, "-cp", str(temp / "classes"), "FreezingRegression"], check=True)
