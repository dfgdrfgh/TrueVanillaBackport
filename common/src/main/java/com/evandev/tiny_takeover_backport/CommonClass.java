package com.evandev.tiny_takeover_backport;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.evandev.tiny_takeover_backport.entity.ModEntityData;
import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import com.evandev.tiny_takeover_backport.registry.ModTrades;

public class CommonClass {

    public static void init() {
        ModConfig.load();
        ModRegistry.init();
        ModEntityData.init();
        ModTrades.init();
    }
}
