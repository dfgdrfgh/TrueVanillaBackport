package com.blackgear.vanillabackport.client.api.modules.mob_variants;

import com.blackgear.platform.core.BuiltInCoreRegistry;
import com.blackgear.platform.core.api.RegistryKey;
import com.blackgear.vanillabackport.client.level.model.entity.pig.ColdPigModel;
import com.blackgear.vanillabackport.client.level.model.entity.pig.PigVariantModel;
import com.blackgear.vanillabackport.client.registries.ModModelLayers;
import com.blackgear.vanillabackport.common.api.modules.mob_variant.VariantUtils;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.pig.PigVariant;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.pig.PigVariants;
import com.blackgear.vanillabackport.core.compat.ClientCompat;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class PigVariantRenderer extends AbstractVariantRenderer<Pig, PigModel<Pig>, PigVariant, PigVariant.ModelType> {
    private final PigModel<Pig> temperateModel;

    public PigVariantRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.temperateModel = new PigVariantModel<>(context.bakeLayer(ModModelLayers.TEMPERATE_PIG));
    }
    
    @Override
    protected Map<PigVariant.ModelType, PigModel<Pig>> bakeModels(EntityRendererProvider.Context context) {
        Map<PigVariant.ModelType, PigModel<Pig>> map = Maps.newEnumMap(PigVariant.ModelType.class);
        map.put(PigVariant.ModelType.NORMAL, null);
        map.put(PigVariant.ModelType.COLD, new ColdPigModel<>(context.bakeLayer(ModModelLayers.COLD_PIG)));
        return map;
    }
    
    @Override
    protected PigVariant.ModelType getModelType(PigVariant variant) {
        return variant.modelAndTexture().model();
    }
    
    @Override
    protected ResourceLocation getTexture(Pig pig, PigVariant variant) {
        if (ClientCompat.hasQuarkPigTexture(pig)) return null;
        return variant.modelAndTexture().asset().path();
    }
    
    @Override
    public Optional<PigModel<Pig>> getModel(Pig pig) {
        if (!pig.isBaby() && !ClientCompat.hasQuarkPigTexture(pig)
            && this.getVariant(pig)
                .filter(variant -> VariantUtils.matches(this.getRegistry(), variant, this.getDefaultVariant()))
                .isPresent()) {
            return Optional.of(this.temperateModel);
        }
        return super.getModel(pig);
    }

    @Override
    public Optional<ResourceLocation> getTexture(Pig pig) {
        if (pig.isBaby()) return super.getTexture(pig);
        return this.getVariant(pig).map(variant -> this.getTexture(pig, variant));
    }

    @Override
    protected BuiltInCoreRegistry<PigVariant> getRegistry() {
        return PigVariants.REGISTRIES;
    }
    
    @Override
    protected RegistryKey<PigVariant> getDefaultVariant() {
        return PigVariants.TEMPERATE;
    }
}