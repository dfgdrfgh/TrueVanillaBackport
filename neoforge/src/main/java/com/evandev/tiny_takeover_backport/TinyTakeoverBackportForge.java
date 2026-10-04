package com.evandev.tiny_takeover_backport;

import com.evandev.tiny_takeover_backport.client.ForgeClientEvents;
import com.evandev.tiny_takeover_backport.config.NameTagRecipeEnabledCondition;
import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

public class TinyTakeoverBackportForge {

    public TinyTakeoverBackportForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onRegister);
        modEventBus.addListener(this::addCreative);

        if (FMLEnvironment.dist.isClient()) {
            ForgeClientEvents.init(modEventBus);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CommonClass.init();
        event.enqueueWork(() -> {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(
                    Constants.vanillaLocation("golden_dandelion"),
                    () -> ModRegistry.POTTED_GOLDEN_DANDELION
            );
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModRegistry.GOLDEN_DANDELION_ITEM);
        }
    }

    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            ModRegistry.BLOCKS.forEach((id, block) -> event.register(Registries.BLOCK, id, () -> block));
        } else if (event.getRegistryKey().equals(Registries.ITEM)) {
            ModRegistry.ITEMS.forEach((id, item) -> event.register(Registries.ITEM, id, () -> item));
        } else if (event.getRegistryKey().equals(Registries.SOUND_EVENT)) {
            ModRegistry.SOUND_EVENTS.forEach((id, sound) -> event.register(Registries.SOUND_EVENT, id, () -> sound));
        } else if (event.getRegistryKey().equals(Registries.PARTICLE_TYPE)) {
            ModRegistry.PARTICLES.forEach((id, particle) -> event.register(Registries.PARTICLE_TYPE, id, () -> particle));
        }
        else if (event.getRegistryKey().equals(NeoForgeRegistries.Keys.CONDITION_CODECS)) {
            event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, Constants.location(Constants.MOD_ID, "nametag_recipe_enabled"), () -> NameTagRecipeEnabledCondition.CODEC);
        }
    }
}
