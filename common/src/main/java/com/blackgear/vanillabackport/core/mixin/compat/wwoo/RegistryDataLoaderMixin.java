package com.blackgear.vanillabackport.core.mixin.compat.wwoo;

import com.blackgear.vanillabackport.common.integrations.compat.wwoo.WwooBiomeCompatibility;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @WrapOperation(
        method = "loadContentsFromManager",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/FileToIdConverter;listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;")
    )
    private static Map<ResourceLocation, Resource> vb$preserveBackportedBiomes(
        FileToIdConverter converter, ResourceManager manager, Operation<Map<ResourceLocation, Resource>> original
    ) {
        return WwooBiomeCompatibility.resolveBiomes(original.call(converter, manager), manager);
    }
}
