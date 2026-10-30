package com.blackgear.vanillabackport.core.mixin.common.explorer_maps;

import com.blackgear.vanillabackport.common.level.items.maps.MapMarkerRotation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(MapItemSavedData.class)
public abstract class MapItemSavedDataMixin {
    @Shadow @Final public ResourceKey<Level> dimension;

    // 26.3 also uses elapsed game time for on-map Nether markers. Keeping the
    // old daylight clock here would make their direction jump at the map edge.
    @WrapOperation(method = "addDecoration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData;getDayTime()J"))
    private long vb$netherMarkerGameTime(LevelData data, Operation<Long> original) {
        return data.getGameTime();
    }

    @WrapOperation(method = "addDecoration", at = @At(value = "NEW", target = "(Lnet/minecraft/core/Holder;BBBLjava/util/Optional;)Lnet/minecraft/world/level/saveddata/maps/MapDecoration;"))
    private MapDecoration vb$offMapPlayerDirection(Holder<MapDecorationType> type, byte x, byte z, byte rotation,
            Optional<Component> name, Operation<MapDecoration> original,
            Holder<MapDecorationType> inputType, LevelAccessor level, String key,
            double worldX, double worldZ, double yaw, Component inputName) {
        if (type.is(MapDecorationTypes.PLAYER_OFF_MAP) || type.is(MapDecorationTypes.PLAYER_OFF_LIMITS)) {
            rotation = MapMarkerRotation.calculate(yaw, this.dimension == Level.NETHER && level != null,
                level == null ? 0L : level.getLevelData().getGameTime());
        }
        return original.call(type, x, z, rotation, name);
    }
}
