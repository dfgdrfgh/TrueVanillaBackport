package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.entity.AgeLockable;
import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin extends PathfinderMob {

    protected AgeableMobMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AgeableMob;setAge(I)V"))
    private void wrapSetAge(AgeableMob instance, int age, Operation<Void> original) {
        if (instance.getAge() >= 0 || !((AgeLockable) this).tiny_takeover_backport$isAgeLocked()) {
            original.call(instance, age);
        }
    }

    @Inject(method = "ageUp(IZ)V", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$preventAgeUp(int amount, boolean forced, CallbackInfo ci) {
        if (this.isBaby() && ((AgeLockable) this).tiny_takeover_backport$isAgeLocked()) {
            ci.cancel();
        }
    }
}
