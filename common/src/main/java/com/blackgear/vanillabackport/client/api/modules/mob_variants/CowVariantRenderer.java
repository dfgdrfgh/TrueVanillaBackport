package com.blackgear.vanillabackport.client.api.modules.mob_variants;

import com.blackgear.platform.core.BuiltInCoreRegistry;
import com.blackgear.platform.core.api.RegistryKey;
import com.blackgear.vanillabackport.client.level.model.entity.cow.ColdCowModel;
import com.blackgear.vanillabackport.client.level.model.entity.cow.WarmCowModel;
import com.blackgear.vanillabackport.client.level.model.entity.cow.CowVariantModel;
import com.blackgear.vanillabackport.client.registries.ModModelLayers;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.cow.CowVariant;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.cow.CowVariants;
import com.blackgear.vanillabackport.core.compat.ClientCompat;
import com.google.common.collect.Maps;
import com.evandev.tiny_takeover_backport.client.ModBabyTextureRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class CowVariantRenderer extends AbstractVariantRenderer<Cow, CowModel<Cow>, CowVariant, CowVariant.ModelType> {
    public CowVariantRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    protected Map<CowVariant.ModelType, CowModel<Cow>> bakeModels(EntityRendererProvider.Context context) {
        Map<CowVariant.ModelType, CowModel<Cow>> map = Maps.newEnumMap(CowVariant.ModelType.class);
        map.put(CowVariant.ModelType.NORMAL, new CowVariantModel<>(context.bakeLayer(ModModelLayers.TEMPERATE_COW)));
        map.put(CowVariant.ModelType.WARM, new WarmCowModel<>(context.bakeLayer(ModModelLayers.WARM_COW)));
        map.put(CowVariant.ModelType.COLD, new ColdCowModel<>(context.bakeLayer(ModModelLayers.COLD_COW)));
        return map;
    }
    
    @Override
    protected CowVariant.ModelType getModelType(CowVariant variant) {
        return variant.modelAndTexture().model();
    }
    
    @Override
    protected ResourceLocation getTexture(Cow cow, CowVariant variant) {
        if (ClientCompat.hasQuarkCowTexture(cow)) return null;
        return variant.modelAndTexture().asset().path();
    }
    
    @Override
    public Optional<CowModel<Cow>> getModel(Cow cow) {
        if (ClientCompat.hasQuarkCowTexture(cow)
            && this.getVariant(cow).map(this::getModelType)
                .filter(type -> type == CowVariant.ModelType.NORMAL).isPresent()) {
            return Optional.empty();
        }
        return super.getModel(cow);
    }

    // The temperate variant also has a backported asset; preserve baby textures
    // regardless of the order of the integrated renderer hooks.
    @Override
    public Optional<ResourceLocation> getTexture(Cow cow) {
        return this.getVariant(cow)
            .map(variant -> this.getTexture(cow, variant))
            .map(texture -> ModBabyTextureRegistry.getBabyTexture(cow, texture));
    }

    @Override
    protected BuiltInCoreRegistry<CowVariant> getRegistry() {
        return CowVariants.REGISTRIES;
    }
    
    @Override
    protected RegistryKey<CowVariant> getDefaultVariant() {
        return CowVariants.TEMPERATE;
    }
}