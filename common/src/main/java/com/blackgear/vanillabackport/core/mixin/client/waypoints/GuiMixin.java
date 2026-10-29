package com.blackgear.vanillabackport.core.mixin.client.waypoints;

import com.blackgear.vanillabackport.client.api.modules.waypoints.LocatorBarRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Render waypoint locator bar using vanilla HUD render, without removed
 * Platform HudElementRegistry (absent in published Platform 1.4.2).
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void vanillabackport$renderLocatorBar(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
        if (Minecraft.getInstance().options.hideGui) return;
        LocatorBarRenderer.INSTANCE.renderBackground(graphics, tracker);
        LocatorBarRenderer.INSTANCE.renderWaypoints(graphics, tracker);
    }
}
