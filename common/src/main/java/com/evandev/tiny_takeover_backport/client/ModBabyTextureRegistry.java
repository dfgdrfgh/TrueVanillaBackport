package com.evandev.tiny_takeover_backport.client;

import com.evandev.tiny_takeover_backport.Constants;
import com.evandev.tiny_takeover_backport.Platform;
import com.evandev.tiny_takeover_backport.compat.VanillaBackportCompat;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ModBabyTextureRegistry {
    private static String getRabbitTextureName(String path) {
        return path.substring("textures/entity/rabbit/".length(), path.length() - 4);
    }

    public static ResourceLocation getBabyTexture(LivingEntity entity, ResourceLocation original) {
        if (!original.getNamespace().equals("minecraft")) {
            return original;
        }

        String path = original.getPath();

        if (!entity.isBaby()) {
            if (ModConfig.get().isAdultRabbitReplaced(entity)
                    && path.startsWith("textures/entity/rabbit/")) {
                return Constants.vanillaLocation(
                        "textures/entity/rabbit/rabbit_" + getRabbitTextureName(path) + ".png");
            }
            return original;
        }

        if (!ModConfig.get().isModelEnabled(entity)) {
            return original;
        }

        if (path.endsWith("_baby.png") || path.endsWith("snifflet.png") || path.contains("/baby/")) {
            return original;
        }

        if (Platform.isModLoaded("vanillabackport") && VanillaBackportCompat.hasFarmAnimalVariants()) {
            switch (path) {
                case "textures/entity/cow/warm_cow.png" -> {
                    return Constants.vanillaLocation("textures/entity/cow/cow_warm_baby.png");
                }
                case "textures/entity/cow/cold_cow.png" -> {
                    return Constants.vanillaLocation("textures/entity/cow/cow_cold_baby.png");
                }
                case "textures/entity/chicken/warm_chicken.png" -> {
                    return Constants.vanillaLocation("textures/entity/chicken/chicken_warm_baby.png");
                }
                case "textures/entity/chicken/cold_chicken.png" -> {
                    return Constants.vanillaLocation("textures/entity/chicken/chicken_cold_baby.png");
                }
                case "textures/entity/pig/warm_pig.png" -> {
                    return Constants.vanillaLocation("textures/entity/pig/pig_warm_baby.png");
                }
                case "textures/entity/pig/cold_pig.png" -> {
                    return Constants.vanillaLocation("textures/entity/pig/pig_cold_baby.png");
                }
            }
        }

        if (path.equals("textures/entity/llama/trader_llama.png")) {
            return Constants.vanillaLocation("textures/entity/llama/trader_llama_baby.png");
        }

        switch (path) {
            case "textures/entity/chicken.png" -> {
                return Constants.vanillaLocation("textures/entity/chicken/chicken_temperate_baby.png");
            }
            case "textures/entity/cow/cow.png" -> {
                return Constants.vanillaLocation("textures/entity/cow/cow_temperate_baby.png");
            }
            case "textures/entity/cow/brown_mooshroom.png" -> {
                return Constants.vanillaLocation("textures/entity/cow/mooshroom_brown_baby.png");
            }
            case "textures/entity/cow/red_mooshroom.png" -> {
                return Constants.vanillaLocation("textures/entity/cow/mooshroom_red_baby.png");
            }
            case "textures/entity/pig/pig.png" -> {
                return Constants.vanillaLocation("textures/entity/pig/pig_temperate_baby.png");
            }
            case "textures/entity/turtle/big_sea_turtle.png" -> {
                return Constants.vanillaLocation("textures/entity/turtle/turtle_baby.png");
            }
            case "textures/entity/bear/polarbear.png" -> {
                return Constants.vanillaLocation("textures/entity/bear/polarbear_baby.png");
            }
            case "textures/entity/dolphin.png" -> {
                return Constants.vanillaLocation("textures/entity/dolphin/dolphin_baby.png");
            }
            case "textures/entity/sniffer/sniffer.png" -> {
                return Constants.vanillaLocation("textures/entity/sniffer/snifflet.png");
            }
            case "textures/entity/armadillo.png" -> {
                return Constants.vanillaLocation("textures/entity/armadillo/armadillo_baby.png");
            }
            case "textures/entity/cat/tabby.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_tabby_baby.png");
            }
            case "textures/entity/cat/black.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_black_baby.png");
            }
            case "textures/entity/cat/red.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_red_baby.png");
            }
            case "textures/entity/cat/siamese.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_siamese_baby.png");
            }
            case "textures/entity/cat/british_shorthair.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_british_shorthair_baby.png");
            }
            case "textures/entity/cat/calico.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_calico_baby.png");
            }
            case "textures/entity/cat/persian.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_persian_baby.png");
            }
            case "textures/entity/cat/ragdoll.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_ragdoll_baby.png");
            }
            case "textures/entity/cat/white.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_white_baby.png");
            }
            case "textures/entity/cat/jellie.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_jellie_baby.png");
            }
            case "textures/entity/cat/all_black.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/cat_all_black_baby.png");
            }
            case "textures/entity/ocelot.png" -> {
                return Constants.vanillaLocation("textures/entity/cat/ocelot_baby.png");
            }
            case "textures/entity/llama/creamy.png" -> {
                return Constants.vanillaLocation("textures/entity/llama/llama_creamy_baby.png");
            }
            case "textures/entity/llama/white.png" -> {
                return Constants.vanillaLocation("textures/entity/llama/llama_white_baby.png");
            }
            case "textures/entity/llama/brown.png" -> {
                return Constants.vanillaLocation("textures/entity/llama/llama_brown_baby.png");
            }
            case "textures/entity/llama/gray.png" -> {
                return Constants.vanillaLocation("textures/entity/llama/llama_gray_baby.png");
            }
            case "textures/entity/fox/snow_fox.png" -> {
                return Constants.vanillaLocation("textures/entity/fox/fox_snow_baby.png");
            }
            case "textures/entity/fox/snow_fox_sleep.png" -> {
                return Constants.vanillaLocation("textures/entity/fox/fox_snow_sleep_baby.png");
            }
        }

        if (path.startsWith("textures/entity/rabbit/")) {
            return Constants.vanillaLocation(
                    "textures/entity/rabbit/rabbit_" + getRabbitTextureName(path) + "_baby.png");
        }

        if (path.equals("textures/entity/villager/villager.png")) {
            return Constants.vanillaLocation(
                    "textures/entity/villager/villager_baby.png");
        }

        if (path.startsWith("textures/entity/villager/type/")) {
            return Constants.vanillaLocation(
                    "textures/entity/villager/baby/"
                            + path.substring("textures/entity/villager/type/".length()));
        }

        if (path.equals("textures/entity/zombie_villager/zombie_villager.png")) {
            return Constants.vanillaLocation(
                    "textures/entity/zombie_villager/zombie_villager_baby.png");
        }

        if (path.startsWith("textures/entity/zombie_villager/type/")) {
            return Constants.vanillaLocation(
                    "textures/entity/zombie_villager/baby/"
                            + path.substring("textures/entity/zombie_villager/type/".length()));
        }

        if (path.endsWith(".png")) {
            return Constants.vanillaLocation(
                    path.substring(0, path.length() - 4) + "_baby.png");
        }

        return original;
    }
}
