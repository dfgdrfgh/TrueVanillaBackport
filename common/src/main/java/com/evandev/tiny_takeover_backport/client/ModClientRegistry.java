package com.evandev.tiny_takeover_backport.client;

import com.evandev.tiny_takeover_backport.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModClientRegistry {
    public static final Map<ModelLayerLocation, Supplier<LayerDefinition>> LAYER_DEFINITIONS = new LinkedHashMap<>();

    public static void init() {
        register(ModModelLayers.ARMADILLO_BABY, BabyArmadilloModel::createBodyLayer);
        register(ModModelLayers.AXOLOTL_BABY, BabyAxolotlModel::createBodyLayer);
        register(ModModelLayers.BEE_BABY, BabyBeeModel::createBodyLayer);
        register(ModModelLayers.CAMEL_BABY, BabyCamelModel::createBodyLayer);
        register(ModModelLayers.OCELOT_BABY, BabyOcelotModel::createBodyLayer);
        register(ModModelLayers.CAT_BABY, BabyCatModel::createBodyLayer);
        register(ModModelLayers.CAT_BABY_COLLAR, BabyCatModel::createCollarLayer);
        register(ModModelLayers.CHICKEN_BABY, BabyChickenModel::createBodyLayer);
        register(ModModelLayers.COW_BABY, BabyCowModel::createBodyLayer);
        register(ModModelLayers.DOLPHIN_BABY, BabyDolphinModel::createBodyLayer);
        register(ModModelLayers.DONKEY_BABY, BabyDonkeyModel::createBodyLayer);
        register(ModModelLayers.DROWNED_BABY, BabyDrownedModel::createBodyLayer);
        register(ModModelLayers.DROWNED_BABY_OUTER_LAYER, () -> BabyZombieModel.createBodyLayer(new CubeDeformation(0.25F)));
        register(ModModelLayers.FOX_BABY, BabyFoxModel::createBodyLayer);
        register(ModModelLayers.GOAT_BABY, BabyGoatModel::createBodyLayer);
        register(ModModelLayers.HOGLIN_BABY, BabyHoglinModel::createBodyLayer);
        register(ModModelLayers.HORSE_BABY, BabyHorseModel::createBodyLayer);
        register(ModModelLayers.LLAMA_BABY, BabyLlamaModel::createBodyLayer);
        register(ModModelLayers.LLAMA_BABY_DECOR, () -> BabyLlamaModel.createDecorLayer(new CubeDeformation(0.2F)));
        register(ModModelLayers.TRADER_LLAMA_BABY_DECOR, () -> BabyLlamaModel.createTraderDecorLayer(new CubeDeformation(0.2F)));
        register(ModModelLayers.PANDA_BABY, BabyPandaModel::createBodyLayer);
        register(ModModelLayers.PIGLIN_BABY, BabyPiglinModel::createBodyLayer);
        register(ModModelLayers.PIG_BABY, BabyPigModel::createBodyLayer);
        register(ModModelLayers.POLAR_BEAR_BABY, BabyPolarBearModel::createBodyLayer);
        register(ModModelLayers.RABBIT_BABY, BabyRabbitModel::createBodyLayer);
        register(ModModelLayers.SHEEP_BABY, BabySheepModel::createBodyLayer);
        register(ModModelLayers.SHEEP_BABY_WOOL, BabySheepModel::createBodyLayer);
        register(ModModelLayers.SQUID_BABY, BabySquidModel::createBodyLayer);
        register(ModModelLayers.STRIDER_BABY, BabyStriderModel::createBodyLayer);
        register(ModModelLayers.TURTLE_BABY, BabyTurtleModel::createBodyLayer);
        register(ModModelLayers.VILLAGER_BABY, BabyVillagerModel::createBodyLayer);
        register(ModModelLayers.VILLAGER_BABY_NO_HAT, BabyVillagerModel::createNoHatLayer);
        register(ModModelLayers.WOLF_BABY, BabyWolfModel::createBodyLayer);
        register(ModModelLayers.ZOMBIE_BABY, BabyZombieModel::createBodyLayer);
        register(ModModelLayers.ZOMBIE_BABY_INNER_ARMOR, () -> BabyZombieModel.createArmorLayer(new CubeDeformation(-0.1F, 0.3F, 0.3F)));
        register(ModModelLayers.ZOMBIE_BABY_OUTER_ARMOR, () -> BabyZombieModel.createArmorLayer(new CubeDeformation(-0.1F, 0.5F, 0.3F)));
        register(ModModelLayers.ZOMBIE_VILLAGER_BABY, BabyZombieVillagerModel::createBodyLayer);
        register(ModModelLayers.ZOMBIFIED_PIGLIN_BABY, BabyZombifiedPiglinModel::createBodyLayer);
        register(ModModelLayers.PIGLIN_BABY_INNER_ARMOR, () -> BabyPiglinModel.createArmorLayer(new CubeDeformation(0.7F)));
        register(ModModelLayers.PIGLIN_BABY_OUTER_ARMOR, () -> BabyPiglinModel.createArmorLayer(new CubeDeformation(0.7F)));
        register(ModModelLayers.SNIFFER_BABY, SniffletModel::createBodyLayer);
        register(ModModelLayers.RABBIT_ADULT, NewAdultRabbitModel::createBodyLayer);
    }

    public static void register(ModelLayerLocation layer, Supplier<LayerDefinition> definition) {
        LAYER_DEFINITIONS.put(layer, definition);
    }
}
