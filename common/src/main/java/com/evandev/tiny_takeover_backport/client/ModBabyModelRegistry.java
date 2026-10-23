package com.evandev.tiny_takeover_backport.client;

import com.evandev.tiny_takeover_backport.client.model.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public class ModBabyModelRegistry {
    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity, M extends EntityModel<T>> EntityModel<T> createBabyModel(
            LivingEntityRenderer<T, M> renderer, EntityRendererProvider.Context context, M adultModel) {

        EntityModelSet modelSet = context.getModelSet();

        if (adultModel instanceof WitchModel || adultModel instanceof IllagerModel) {
            return null;
        }

        if (adultModel instanceof DrownedModel) {
            return (EntityModel<T>) new BabyDrownedModel(modelSet.bakeLayer(ModModelLayers.DROWNED_BABY));
        }
        if (adultModel instanceof ZombieVillagerModel) {
            return (EntityModel<T>) new BabyZombieVillagerModel(modelSet.bakeLayer(ModModelLayers.ZOMBIE_VILLAGER_BABY));
        }
        if (adultModel instanceof ChestedHorseModel) {
            return (EntityModel<T>) new BabyDonkeyModel(modelSet.bakeLayer(ModModelLayers.DONKEY_BABY));
        }
        if (adultModel instanceof PiglinModel && renderer.getClass().getSimpleName().contains("Zombified")) {
            return (EntityModel<T>) new BabyZombifiedPiglinModel(modelSet.bakeLayer(ModModelLayers.ZOMBIFIED_PIGLIN_BABY));
        }

        if (adultModel instanceof ArmadilloModel) {
            return (EntityModel<T>) new BabyArmadilloModel(modelSet.bakeLayer(ModModelLayers.ARMADILLO_BABY));
        }
        if (adultModel instanceof AxolotlModel) {
            return (EntityModel<T>) new BabyAxolotlModel(modelSet.bakeLayer(ModModelLayers.AXOLOTL_BABY));
        }
        if (adultModel instanceof BeeModel) {
            return (EntityModel<T>) new BabyBeeModel(modelSet.bakeLayer(ModModelLayers.BEE_BABY));
        }
        if (adultModel instanceof CamelModel) {
            return (EntityModel<T>) new BabyCamelModel(modelSet.bakeLayer(ModModelLayers.CAMEL_BABY));
        }
        if (adultModel instanceof CatModel) {
            return (EntityModel<T>) new BabyCatModel(modelSet.bakeLayer(ModModelLayers.CAT_BABY));
        }
        if (adultModel instanceof OcelotModel) {
            return (EntityModel<T>) new BabyOcelotModel(modelSet.bakeLayer(ModModelLayers.OCELOT_BABY));
        }
        if (adultModel instanceof ChickenModel) {
            return (EntityModel<T>) new BabyChickenModel(modelSet.bakeLayer(ModModelLayers.CHICKEN_BABY));
        }
        if (adultModel instanceof CowModel) {
            return (EntityModel<T>) new BabyCowModel(modelSet.bakeLayer(ModModelLayers.COW_BABY));
        }
        if (adultModel instanceof DolphinModel) {
            return (EntityModel<T>) new BabyDolphinModel(modelSet.bakeLayer(ModModelLayers.DOLPHIN_BABY));
        }
        if (adultModel instanceof FoxModel) {
            return (EntityModel<T>) new BabyFoxModel(modelSet.bakeLayer(ModModelLayers.FOX_BABY));
        }
        if (adultModel instanceof GoatModel) {
            return (EntityModel<T>) new BabyGoatModel(modelSet.bakeLayer(ModModelLayers.GOAT_BABY));
        }
        if (adultModel instanceof HoglinModel) {
            return (EntityModel<T>) new BabyHoglinModel(modelSet.bakeLayer(ModModelLayers.HOGLIN_BABY));
        }
        if (adultModel instanceof HorseModel) {
            return (EntityModel<T>) new BabyHorseModel(modelSet.bakeLayer(ModModelLayers.HORSE_BABY));
        }
        if (adultModel instanceof LlamaModel) {
            return (EntityModel<T>) new BabyLlamaModel(modelSet.bakeLayer(ModModelLayers.LLAMA_BABY));
        }
        if (adultModel instanceof PandaModel) {
            return (EntityModel<T>) new BabyPandaModel(modelSet.bakeLayer(ModModelLayers.PANDA_BABY));
        }
        if (adultModel instanceof PigModel) {
            return (EntityModel<T>) new BabyPigModel(modelSet.bakeLayer(ModModelLayers.PIG_BABY));
        }
        if (adultModel instanceof PiglinModel) {
            return (EntityModel<T>) new BabyPiglinModel(modelSet.bakeLayer(ModModelLayers.PIGLIN_BABY));
        }
        if (adultModel instanceof PolarBearModel) {
            return (EntityModel<T>) new BabyPolarBearModel(modelSet.bakeLayer(ModModelLayers.POLAR_BEAR_BABY));
        }
        if (adultModel instanceof RabbitModel) {
            return (EntityModel<T>) new BabyRabbitModel(modelSet.bakeLayer(ModModelLayers.RABBIT_BABY));
        }
        if (adultModel instanceof SheepModel) {
            return (EntityModel<T>) new BabySheepModel(modelSet.bakeLayer(ModModelLayers.SHEEP_BABY));
        }
        if (adultModel instanceof SquidModel) {
            return (EntityModel<T>) new BabySquidModel(modelSet.bakeLayer(ModModelLayers.SQUID_BABY));
        }
        if (adultModel instanceof StriderModel) {
            return (EntityModel<T>) new BabyStriderModel(modelSet.bakeLayer(ModModelLayers.STRIDER_BABY));
        }
        if (adultModel instanceof TurtleModel) {
            return (EntityModel<T>) new BabyTurtleModel(modelSet.bakeLayer(ModModelLayers.TURTLE_BABY));
        }
        if (adultModel instanceof VillagerModel) {
            return (EntityModel<T>) new BabyVillagerModel(modelSet.bakeLayer(ModModelLayers.VILLAGER_BABY));
        }
        if (adultModel instanceof WolfModel) {
            return (EntityModel<T>) new BabyWolfModel(modelSet.bakeLayer(ModModelLayers.WOLF_BABY));
        }
        if (adultModel instanceof ZombieModel) {
            return (EntityModel<T>) new BabyZombieModel(modelSet.bakeLayer(ModModelLayers.ZOMBIE_BABY));
        }
        if (adultModel instanceof SnifferModel) {
            return (EntityModel<T>) new SniffletModel(modelSet.bakeLayer(ModModelLayers.SNIFFER_BABY));
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity, M extends EntityModel<T>> EntityModel<T> createAdultModel(
            LivingEntityRenderer<T, M> renderer, EntityRendererProvider.Context context, M adultModel) {

        EntityModelSet modelSet = context.getModelSet();

        if (adultModel instanceof RabbitModel) {
            return (EntityModel<T>) new NewAdultRabbitModel<>(modelSet.bakeLayer(ModModelLayers.RABBIT_ADULT));
        }

        return null;
    }
}
