package com.evandev.tiny_takeover_backport.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Strider.class)
public abstract class StriderMixin extends Animal {
    protected StriderMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Strider;setSuffocating(Z)V"))
    private void tiny_takeover_backport$inheritWarmth(Strider strider, boolean cold, Operation<Void> original) {
        boolean warm = this.level().getBlockState(this.blockPosition()).is(BlockTags.STRIDER_WARM_BLOCKS)
                || this.getBlockStateOnLegacy().is(BlockTags.STRIDER_WARM_BLOCKS)
                || this.getFluidHeight(FluidTags.LAVA) > 0.0;
        boolean onWarmStrider = this.getVehicle() instanceof Strider parent && !parent.isSuffocating();
        original.call(strider, !warm && !onWarmStrider);
    }
}
