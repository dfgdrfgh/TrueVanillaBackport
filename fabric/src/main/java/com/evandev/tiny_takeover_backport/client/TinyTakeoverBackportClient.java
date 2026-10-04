package com.evandev.tiny_takeover_backport.client;

import com.evandev.tiny_takeover_backport.client.particle.SimpleVerticalParticle;
import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.RenderType;

public class TinyTakeoverBackportClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Fabric does not read the Forge render_type field in block models.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                ModRegistry.GOLDEN_DANDELION, ModRegistry.POTTED_GOLDEN_DANDELION);

        ModClientRegistry.init();
        ModClientRegistry.LAYER_DEFINITIONS.forEach((layer, supplier) -> {
            EntityModelLayerRegistry.registerModelLayer(layer, supplier::get);
        });

        ParticleFactoryRegistry.getInstance().register(ModRegistry.PAUSE_MOB_GROWTH, SimpleVerticalParticle.PauseMobGrowthProvider::new);
        ParticleFactoryRegistry.getInstance().register(ModRegistry.RESET_MOB_GROWTH, SimpleVerticalParticle.ResetMobGrowthProvider::new);
    }
}
