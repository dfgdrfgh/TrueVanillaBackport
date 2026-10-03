"""Exercise the real screen-open payload and listener against a queued client.

Small Minecraft fixtures isolate packet ordering without launching a renderer.
The queue deliberately defers execute(), as Minecraft's reentrant event loop
does while processing an already scheduled packet callback.
"""
import os
import subprocess
import tempfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
java = str(Path(os.environ["JAVA_HOME"]) / "bin/java") if os.environ.get("JAVA_HOME") else "java"
production = root / "common/src/main/java/com/blackgear/vanillabackport/core/network"
fixtures = {
    "net.fabricmc.api.EnvType": "public enum EnvType { CLIENT, SERVER }",
    "net.fabricmc.api.Environment": "public @interface Environment { EnvType value(); }",
    "net.minecraft.resources.ResourceLocation": "public record ResourceLocation(String id) {}",
    "com.blackgear.vanillabackport.core.VanillaBackport": """
        public class VanillaBackport {
            public static net.minecraft.resources.ResourceLocation resource(String path) {
                return new net.minecraft.resources.ResourceLocation(path);
            }
        }
    """,
    "net.minecraft.network.FriendlyByteBuf": """
        public class FriendlyByteBuf {
            public byte readByte() { return 0; }
            public int readVarInt() { return 0; }
            public int readInt() { return 0; }
            public void writeByte(int value) {}
            public void writeVarInt(int value) {}
            public void writeInt(int value) {}
        }
    """,
    "net.minecraft.network.RegistryFriendlyByteBuf": "public class RegistryFriendlyByteBuf extends FriendlyByteBuf {}",
    "net.minecraft.network.codec.StreamCodec": """
        public interface StreamCodec<B, T> {
            static <B, T> StreamCodec<B, T> ofMember(java.util.function.BiConsumer<T, B> writer,
                    java.util.function.Function<B, T> reader) { return null; }
        }
    """,
    "net.minecraft.network.protocol.common.custom.CustomPacketPayload": """
        public interface CustomPacketPayload {
            record Type<T extends CustomPacketPayload>(net.minecraft.resources.ResourceLocation id) {}
            Type<? extends CustomPacketPayload> type();
        }
    """,
    "net.minecraft.world.entity.Entity": "public class Entity {}",
    "com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus":
        "public class AbstractNautilus extends net.minecraft.world.entity.Entity {}",
    "net.minecraft.world.level.Level": """
        public class Level {
            public net.minecraft.world.entity.Entity entity;
            public boolean isClientSide() { return true; }
            public net.minecraft.world.entity.Entity getEntity(int id) { return id == 77 ? entity : null; }
        }
    """,
    "net.minecraft.world.SimpleContainer": """
        public class SimpleContainer {
            public final String[] items;
            public SimpleContainer(int size) { items = new String[size]; }
        }
    """,
    "net.minecraft.world.entity.player.Inventory": "public class Inventory {}",
    "net.minecraft.world.entity.player.Player": """
        public class Player {
            public com.blackgear.vanillabackport.common.level.inventory.NautilusInventoryMenu containerMenu;
            public Inventory getInventory() { return new Inventory(); }
            public net.minecraft.world.level.Level level() { return net.minecraft.client.Minecraft.getInstance().level; }
        }
    """,
    "com.blackgear.vanillabackport.common.level.inventory.NautilusInventoryMenu": """
        public class NautilusInventoryMenu {
            public final int containerId;
            public final net.minecraft.world.SimpleContainer container;
            public NautilusInventoryMenu(int id, net.minecraft.world.entity.player.Inventory inventory,
                    net.minecraft.world.SimpleContainer container,
                    com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus nautilus) {
                this.containerId = id;
                this.container = container;
            }
        }
    """,
    "com.blackgear.vanillabackport.client.level.gui.inventory.NautilusInventoryScreen": """
        public class NautilusInventoryScreen {
            public NautilusInventoryScreen(
                    com.blackgear.vanillabackport.common.level.inventory.NautilusInventoryMenu menu,
                    net.minecraft.world.entity.player.Inventory inventory,
                    com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus nautilus) {}
        }
    """,
    "net.minecraft.client.Minecraft": """
        public class Minecraft {
            private static final Minecraft INSTANCE = new Minecraft();
            public net.minecraft.world.entity.player.Player player;
            public net.minecraft.world.level.Level level;
            public boolean onClientThread = true;
            public Object screen;
            public final java.util.Queue<Runnable> tasks = new java.util.ArrayDeque<>();
            public static Minecraft getInstance() { return INSTANCE; }
            public boolean isSameThread() { return onClientThread; }
            public void execute(Runnable task) { tasks.add(task); }
            public void setScreen(Object screen) { this.screen = screen; }
            public void drain() {
                onClientThread = true;
                int count = 0;
                while (!tasks.isEmpty()) {
                    if (++count > 100) throw new AssertionError("Runaway task scheduling");
                    tasks.remove().run();
                }
            }
        }
    """,
    "com.blackgear.platform.core.networking.PayloadContext": """
        public interface PayloadContext {
            net.minecraft.world.entity.player.Player player();
            java.util.concurrent.CompletableFuture<Void> enqueueWork(Runnable work);
        }
    """,
    "test.NautilusOpenRegression": """
        import com.blackgear.platform.core.networking.PayloadContext;
        import com.blackgear.vanillabackport.core.network.ClientboundNautilusScreenOpenPacket;
        import com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus;
        import net.minecraft.client.Minecraft;
        import net.minecraft.world.entity.player.Player;
        import net.minecraft.world.level.Level;
        import java.util.concurrent.CompletableFuture;

        public class NautilusOpenRegression {
            static final Minecraft client = Minecraft.getInstance();
            static final PayloadContext context = new PayloadContext() {
                public Player player() { return client.player; }
                public CompletableFuture<Void> enqueueWork(Runnable work) {
                    client.execute(work);
                    return CompletableFuture.completedFuture(null);
                }
            };
            static void check(boolean condition, String message) {
                if (!condition) throw new AssertionError(message);
            }
            static void openAndSync(int id, String saddle) {
                client.execute(() -> ClientboundNautilusScreenOpenPacket.handler(
                    new ClientboundNautilusScreenOpenPacket(id, 2, 77), context));
                // Vanilla content sync ignores packets whose menu is not open yet.
                client.execute(() -> {
                    var menu = client.player.containerMenu;
                    if (menu != null && menu.containerId == id) menu.container.items[0] = saddle;
                });
                client.drain();
                check(client.player.containerMenu != null, "Inventory failed to open");
                check(java.util.Objects.equals(client.player.containerMenu.container.items[0], saddle),
                    "Saddle slot sync was lost while reopening inventory " + id);
            }
            public static void main(String[] args) {
                client.player = new Player();
                client.level = new Level();
                client.level.entity = new AbstractNautilus();
                openAndSync(1, null);
                client.player.containerMenu.container.items[0] = "saddle";
                for (int id = 2; id <= 6; id++) {
                    client.player.containerMenu = null; // Close, then reopen with the same server saddle.
                    openAndSync(id, "saddle");
                }
                client.player.containerMenu = null;
                openAndSync(7, null); // Removing the saddle must not restore a fabricated client item.

                client.player.containerMenu = null;
                client.onClientThread = false;
                ClientboundNautilusScreenOpenPacket.handler(new ClientboundNautilusScreenOpenPacket(8, 2, 77), context);
                check(client.player.containerMenu == null, "Opened inventory off the client thread");
                check(client.tasks.size() == 1, "Expected one client-thread handoff");
                client.drain();
                check(client.player.containerMenu.containerId == 8, "Off-thread packet failed to open");

                client.player.containerMenu = null;
                ClientboundNautilusScreenOpenPacket.handler(new ClientboundNautilusScreenOpenPacket(9, 2, 99), context);
                check(client.player.containerMenu == null, "Opened a missing nautilus");
                client.player = null;
                client.level = null;
                ClientboundNautilusScreenOpenPacket.handler(new ClientboundNautilusScreenOpenPacket(10, 2, 77), context);
                client.drain();
                System.out.println("PASS: equip/close/reopen slot sync, repeated opens, empty slot, thread handoff, missing entity, disconnect");
            }
        }
    """,
}

with tempfile.TemporaryDirectory(prefix="nautilus-open-test-") as directory:
    temp = Path(directory)
    sources = []
    for name, body in fixtures.items():
        package = name.rsplit(".", 1)[0]
        path = temp / (name.replace(".", "/") + ".java")
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text("package " + package + ";\n" + body)
        sources.append(str(path))
    sources.extend(str(production / path) for path in [
        "ClientboundNautilusScreenOpenPacket.java", "handlers/ClientboundPayloadListener.java"
    ])
    classes = temp / "classes"
    subprocess.run([java, "-m", "jdk.compiler/com.sun.tools.javac.Main", "-d", str(classes), *sources], check=True)
    subprocess.run([java, "-cp", str(classes), "test.NautilusOpenRegression"], check=True)
