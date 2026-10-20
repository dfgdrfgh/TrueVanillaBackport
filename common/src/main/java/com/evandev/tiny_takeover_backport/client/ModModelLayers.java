package com.evandev.tiny_takeover_backport.client;

import com.evandev.tiny_takeover_backport.Constants;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class ModModelLayers {
    public static final ModelLayerLocation ARMADILLO_BABY = register("armadillo_baby");
    public static final ModelLayerLocation AXOLOTL_BABY = register("axolotl_baby");
    public static final ModelLayerLocation BEE_BABY = register("bee_baby");
    public static final ModelLayerLocation CAMEL_BABY = register("camel_baby");
    public static final ModelLayerLocation CAT_BABY = register("cat_baby");
    public static final ModelLayerLocation OCELOT_BABY = register("ocelot_baby");
    public static final ModelLayerLocation CAT_BABY_COLLAR = register("cat_baby", "collar");
    public static final ModelLayerLocation CHICKEN_BABY = register("chicken_baby");
    public static final ModelLayerLocation COW_BABY = register("cow_baby");
    public static final ModelLayerLocation DOLPHIN_BABY = register("dolphin_baby");
    public static final ModelLayerLocation DONKEY_BABY = register("donkey_baby");
    public static final ModelLayerLocation DROWNED_BABY = register("drowned_baby");
    public static final ModelLayerLocation DROWNED_BABY_OUTER_LAYER = register("drowned_baby", "outer");
    public static final ModelLayerLocation FOX_BABY = register("fox_baby");
    public static final ModelLayerLocation GOAT_BABY = register("goat_baby");
    public static final ModelLayerLocation HOGLIN_BABY = register("hoglin_baby");
    public static final ModelLayerLocation HORSE_BABY = register("horse_baby");
    public static final ModelLayerLocation LLAMA_BABY = register("llama_baby");
    public static final ModelLayerLocation LLAMA_BABY_DECOR = register("llama_baby", "decor");
    public static final ModelLayerLocation TRADER_LLAMA_BABY_DECOR = register("trader_llama_baby", "decor");
    public static final ModelLayerLocation PANDA_BABY = register("panda_baby");
    public static final ModelLayerLocation PIGLIN_BABY = register("piglin_baby");
    public static final ModelLayerLocation PIG_BABY = register("pig_baby");
    public static final ModelLayerLocation POLAR_BEAR_BABY = register("polar_bear_baby");
    public static final ModelLayerLocation RABBIT_BABY = register("rabbit_baby");
    public static final ModelLayerLocation SHEEP_BABY = register("sheep_baby");
    public static final ModelLayerLocation SHEEP_BABY_WOOL = register("sheep_baby", "wool");
    public static final ModelLayerLocation SQUID_BABY = register("squid_baby");
    public static final ModelLayerLocation STRIDER_BABY = register("strider_baby");
    public static final ModelLayerLocation TURTLE_BABY = register("turtle_baby");
    public static final ModelLayerLocation VILLAGER_BABY = register("villager_baby");
    public static final ModelLayerLocation VILLAGER_BABY_NO_HAT = register("villager_baby_no_hat");
    public static final ModelLayerLocation WOLF_BABY = register("wolf_baby");
    public static final ModelLayerLocation ZOMBIE_BABY = register("zombie_baby");
    public static final ModelLayerLocation ZOMBIE_BABY_INNER_ARMOR = register("zombie_baby", "inner_armor");
    public static final ModelLayerLocation ZOMBIE_BABY_OUTER_ARMOR = register("zombie_baby", "outer_armor");
    public static final ModelLayerLocation ZOMBIE_VILLAGER_BABY = register("zombie_villager_baby");
    public static final ModelLayerLocation ZOMBIFIED_PIGLIN_BABY = register("zombified_piglin_baby");
    public static final ModelLayerLocation PIGLIN_BABY_INNER_ARMOR = register("piglin_baby", "inner_armor");
    public static final ModelLayerLocation PIGLIN_BABY_OUTER_ARMOR = register("piglin_baby", "outer_armor");
    public static final ModelLayerLocation SNIFFER_BABY = register("sniffer_baby");
    public static final ModelLayerLocation RABBIT_ADULT = register("rabbit_adult");

    private static ModelLayerLocation register(String path) {
        return register(path, "main");
    }

    private static ModelLayerLocation register(String path, String layer) {
        return new ModelLayerLocation(Constants.location(Constants.MOD_ID, path), layer);
    }
}
