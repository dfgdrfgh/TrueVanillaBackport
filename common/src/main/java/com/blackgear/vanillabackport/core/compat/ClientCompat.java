package com.blackgear.vanillabackport.core.compat;

import com.blackgear.vanillabackport.core.ModChecker;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.frog.Frog;

@Environment(EnvType.CLIENT)
public class ClientCompat {
    public static boolean shouldBypassBabyModel(LivingEntity entity) {
        if (ModChecker.TINY_TAKEOVER && entity.isBaby() && ModConfig.get().isModelEnabled(entity)) {
            return entity.getType() == EntityType.COW || entity.getType() == EntityType.CHICKEN || entity.getType() == EntityType.PIG;
        }
        
        return getNMLActiveRemodel(entity);
    }
    
    @ExpectPlatform
    public static boolean getNMLActiveRemodel(LivingEntity entity) {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static boolean hasQuarkCowTexture(Cow cow) {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static boolean hasQuarkPigTexture(Pig pig) {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static boolean hasQuarkChickenTexture(Chicken chicken) {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static boolean hasQuarkFrogTexture(Frog frog) {
        throw new AssertionError();
    }
}