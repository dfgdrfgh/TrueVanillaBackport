package com.blackgear.vanillabackport.core.mixin.compat.wwoo;

import com.blackgear.vanillabackport.common.integrations.compat.wwoo.WwooBiomeCompatibility;
import com.blackgear.vanillabackport.core.VanillaBackport;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {
    @Unique private boolean vb$loggedWwooPlacement;

    @Inject(method = "pickPlateauBiome", at = @At("RETURN"), cancellable = true)
    private void vb$placePaleGarden(int temperature, int humidity, Climate.Parameter weirdness, CallbackInfoReturnable<ResourceKey<Biome>> cir) {
        ResourceKey<Biome> original = cir.getReturnValue();
        ResourceKey<Biome> selected = WwooBiomeCompatibility.selectPlateauBiome(original, temperature, humidity, weirdness);
        if (!selected.equals(original)) {
            cir.setReturnValue(selected);
            if (!this.vb$loggedWwooPlacement) {
                VanillaBackport.LOGGER.info("WWOO compatibility: using native Pale Garden plateau placement");
                this.vb$loggedWwooPlacement = true;
            }
        }
    }
}
