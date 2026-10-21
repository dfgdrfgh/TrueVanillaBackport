package com.evandev.tiny_takeover_backport;
import com.blackgear.platform.core.Environment;
import java.nio.file.Path;
public class Platform {
    public static boolean isModLoaded(String modId) { return Environment.hasModLoaded(modId); }
    public static Path configDir() { return Environment.getConfigDir(); }
}
