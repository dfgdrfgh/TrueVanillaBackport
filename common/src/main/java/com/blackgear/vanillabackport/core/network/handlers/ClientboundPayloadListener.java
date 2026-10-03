package com.blackgear.vanillabackport.core.network.handlers;

import com.blackgear.vanillabackport.client.level.gui.inventory.NautilusInventoryScreen;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus;
import com.blackgear.vanillabackport.common.level.inventory.NautilusInventoryMenu;
import com.blackgear.vanillabackport.core.network.ClientboundNautilusScreenOpenPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ClientboundPayloadListener {
    public static void handleNautilusScreenOpen(ClientboundNautilusScreenOpenPacket packet) {
        Minecraft client = Minecraft.getInstance();
        if (!client.isSameThread()) {
            client.execute(() -> handleNautilusScreenOpen(packet));
            return;
        }

        // Fabric and NeoForge already deliver this payload on the client thread.
        // Queueing again here can let the initial slot contents overtake the menu.
        Player player = client.player;
        Level level = client.level;
        if (player == null || level == null) return;

        if (level.getEntity(packet.entityId()) instanceof AbstractNautilus nautilus) {
            SimpleContainer container = new SimpleContainer(packet.size());
            NautilusInventoryMenu menu = new NautilusInventoryMenu(packet.containerId(), player.getInventory(), container, nautilus);
            player.containerMenu = menu;
            client.setScreen(new NautilusInventoryScreen(menu, player.getInventory(), nautilus));
        }
    }
}
