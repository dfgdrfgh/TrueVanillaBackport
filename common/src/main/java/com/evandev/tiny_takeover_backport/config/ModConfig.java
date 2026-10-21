package com.evandev.tiny_takeover_backport.config;

import com.evandev.tiny_takeover_backport.Constants;
import com.evandev.tiny_takeover_backport.Platform;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = Platform.configDir().resolve("tiny_takeover_backport.json").toFile();
    private static final List<String> DEFAULT_MODEL_BLACKLIST = List.of("rottencreatures:burned");
    private static ModConfig INSTANCE;

    public boolean enableNameTagRecipe = true;
    public boolean spawnBabyDolphin = true;
    public boolean spawnBabySquid = true;
    public boolean enableTrumpetNoteBlocks = true;
    public boolean enableAnimalSoundVariants = true;

    public boolean enableArmadillo = true;
    public boolean enableAxolotl = true;
    public boolean enableBee = true;
    public boolean enableCamel = true;
    public boolean enableCat = true;
    public boolean enableChicken = true;
    public boolean enableCow = true;
    public boolean enableDolphin = true;
    public boolean enableDonkey = true;
    public boolean enableDrowned = true;
    public boolean enableFox = true;
    public boolean enableGoat = true;
    public boolean enableHoglin = true;
    public boolean enableHorse = true;
    public boolean enableHusk = true;
    public boolean enableLlama = true;
    public boolean enableMule = true;
    public boolean enableOcelot = true;
    public boolean enablePanda = true;
    public boolean enablePig = true;
    public boolean enablePiglin = true;
    public boolean enablePolarBear = true;
    public boolean enableRabbit = true;
    public boolean enableSheep = true;
    public boolean enableSniffer = true;
    public boolean enableSquid = true;
    public boolean enableStrider = true;
    public boolean enableTurtle = true;
    public boolean enableVillager = true;
    public boolean enableWolf = true;
    public boolean enableZombie = true;
    public boolean enableZombieVillager = true;
    public boolean enableZombifiedPiglin = true;

    public boolean replaceAdultRabbit = true;
    public boolean rabbitBoundingBox = true;

    public boolean enableModdedMobs = false;
    public List<String> modelBlacklist = new ArrayList<>(DEFAULT_MODEL_BLACKLIST);

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                Constants.LOG.error("Failed to load tiny_takeover_backport.json", e);
                INSTANCE = new ModConfig();
                save();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save tiny_takeover_backport.json", e);
        }
    }

    public boolean isModelEnabled(Entity entity) {
        return isModelEnabled(entity.getType());
    }

    public boolean isModelEnabled(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return appliesTo(id) && isModelEnabled(id.getPath());
    }

    public boolean appliesTo(Entity entity) {
        return appliesTo(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    private boolean appliesTo(ResourceLocation id) {
        if (modelBlacklist != null && modelBlacklist.contains(id.toString())) {
            return false;
        }
        return enableModdedMobs || id.getNamespace().equals("minecraft");
    }

    public boolean isAdultRabbitReplaced(Entity entity) {
        return replaceAdultRabbit && entity.getType() == EntityType.RABBIT && appliesTo(entity);
    }

    public boolean isModelEnabled(String name) {
        return switch (name) {
            case "armadillo" -> enableArmadillo;
            case "axolotl" -> enableAxolotl;
            case "bee" -> enableBee;
            case "camel" -> enableCamel;
            case "cat" -> enableCat;
            case "chicken" -> enableChicken;
            case "cow", "mooshroom" -> enableCow;
            case "dolphin" -> enableDolphin;
            case "donkey" -> enableDonkey;
            case "drowned" -> enableDrowned;
            case "fox" -> enableFox;
            case "goat" -> enableGoat;
            case "hoglin", "zoglin" -> enableHoglin;
            case "horse", "skeleton_horse", "zombie_horse" -> enableHorse;
            case "husk" -> enableHusk;
            case "llama", "trader_llama" -> enableLlama;
            case "mule" -> enableMule;
            case "ocelot" -> enableOcelot;
            case "panda" -> enablePanda;
            case "pig" -> enablePig;
            case "piglin" -> enablePiglin;
            case "polar_bear" -> enablePolarBear;
            case "rabbit" -> enableRabbit;
            case "sheep" -> enableSheep;
            case "sniffer" -> enableSniffer;
            case "squid", "glow_squid" -> enableSquid;
            case "strider" -> enableStrider;
            case "turtle" -> enableTurtle;
            case "villager" -> enableVillager;
            case "wolf" -> enableWolf;
            case "zombie" -> enableZombie;
            case "zombie_villager" -> enableZombieVillager;
            case "zombified_piglin" -> enableZombifiedPiglin;
            default -> true;
        };
    }
}
