package com.blackgear.vanillabackport.core;

import com.blackgear.platform.core.Environment;

public class ModChecker {
    public static final boolean BETTER_COMBAT = Environment.hasModLoaded("bettercombat");
    public static final boolean COPPER_AGE_BACKPORT = Environment.hasModLoaded("copperagebackport");
    public static final boolean CAVERNS_AND_CHASMS = Environment.hasModLoaded("caverns_and_chasms");
    public static final boolean WWOO = Environment.hasModLoaded("wwoo");
    public static final boolean MIXED_LITTER = Environment.hasModLoaded("mixed_litter");
    public static final boolean QUARK = Environment.hasModLoaded("quark");
    public static final boolean SABLE = Environment.hasModLoaded("sable");
    public static final boolean EVERY_COMPAT = Environment.hasModLoaded("everycomp");
    // Tiny Takeover is part of this fork; do not require a separate mod ID.
    public static final boolean TINY_TAKEOVER = true;
}