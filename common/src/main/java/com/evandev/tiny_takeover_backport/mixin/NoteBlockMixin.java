package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import com.evandev.tiny_takeover_backport.registry.ModTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin {

    @Unique
    private static SoundEvent tiny_takeover_backport$getTrumpet(BlockState below) {
        if (below.is(ModTags.PLAYS_TRUMPET)) return ModRegistry.NOTE_BLOCK_TRUMPET;
        if (below.is(ModTags.PLAYS_TRUMPET_EXPOSED)) return ModRegistry.NOTE_BLOCK_TRUMPET_EXPOSED;
        if (below.is(ModTags.PLAYS_TRUMPET_WEATHERED)) return ModRegistry.NOTE_BLOCK_TRUMPET_WEATHERED;
        if (below.is(ModTags.PLAYS_TRUMPET_OXIDIZED)) return ModRegistry.NOTE_BLOCK_TRUMPET_OXIDIZED;
        return null;
    }

    @WrapOperation(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/properties/NoteBlockInstrument;getSoundEvent()Lnet/minecraft/core/Holder;"))
    private Holder<SoundEvent> tiny_takeover_backport$playTrumpet(NoteBlockInstrument instrument, Operation<Holder<SoundEvent>> original, BlockState state, Level level, BlockPos pos, int id, int param) {
        if (instrument == NoteBlockInstrument.HARP && ModConfig.get().enableTrumpetNoteBlocks) {
            SoundEvent trumpet = tiny_takeover_backport$getTrumpet(level.getBlockState(pos.below()));
            if (trumpet != null) {
                return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(trumpet);
            }
        }
        return original.call(instrument);
    }
}
