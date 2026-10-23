package com.evandev.tiny_takeover_backport.client;

import com.evandev.tiny_takeover_backport.client.particle.SimpleVerticalParticle;
import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class ForgeClientEvents {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ForgeClientEvents::onRegisterLayerDefinitions);
        modEventBus.addListener(ForgeClientEvents::onRegisterParticleProviders);
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModClientRegistry.init();
        ModClientRegistry.LAYER_DEFINITIONS.forEach(event::registerLayerDefinition);
    }

    private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModRegistry.PAUSE_MOB_GROWTH, SimpleVerticalParticle.PauseMobGrowthProvider::new);
        event.registerSpriteSet(ModRegistry.RESET_MOB_GROWTH, SimpleVerticalParticle.ResetMobGrowthProvider::new);
    }
}
