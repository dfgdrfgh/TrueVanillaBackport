package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.evandev.tiny_takeover_backport.entity.ModEntityData;
import com.evandev.tiny_takeover_backport.entity.ModifiableBaby;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tags.ItemTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import com.evandev.tiny_takeover_backport.entity.AgeLockable;

@Mixin(Dolphin.class)
public abstract class DolphinMixin extends WaterAnimal implements ModifiableBaby {

    @Unique
    private int tiny_takeover_backport$forcedAge;
    @Unique
    private int tiny_takeover_backport$forcedAgeTimer;

    protected DolphinMixin(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isBaby() {
        return this.entityData.get(ModEntityData.DOLPHIN_BABY_ID);
    }

    @Unique
    @Override
    public void tiny_takeover_backport$setBaby(boolean baby) {
        this.entityData.set(ModEntityData.DOLPHIN_BABY_ID, baby);
        if (baby) {
            ((AgeLockable) this).tiny_takeover_backport$setCustomAge(-24000);
        } else {
            ((AgeLockable) this).tiny_takeover_backport$setCustomAge(0);
        }
        this.refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (ModEntityData.DOLPHIN_BABY_ID.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void tiny_takeover_backport$addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("IsBaby", this.isBaby());
        compound.putInt("Age", ((AgeLockable) this).tiny_takeover_backport$getCustomAge());
        compound.putInt("ForcedAge", this.tiny_takeover_backport$forcedAge);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void tiny_takeover_backport$readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        this.tiny_takeover_backport$setBaby(compound.contains("Age") ? compound.getInt("Age") < 0 : compound.getBoolean("IsBaby"));
        if (compound.contains("Age")) {
            ((AgeLockable) this).tiny_takeover_backport$setCustomAge(compound.getInt("Age"));
        }
        this.tiny_takeover_backport$forcedAge = compound.getInt("ForcedAge");
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tiny_takeover_backport$growthParticles(CallbackInfo ci) {
        if (this.level().isClientSide() && this.tiny_takeover_backport$forcedAgeTimer > 0) {
            if (this.tiny_takeover_backport$forcedAgeTimer % 4 == 0) {
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            }
            this.tiny_takeover_backport$forcedAgeTimer--;
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!ModConfig.get().spawnBabyDolphin) return;
        Dolphin dolphin = (Dolphin) (Object) this;
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.getItem() instanceof SpawnEggItem egg && egg.spawnsEntity(itemstack, dolphin.getType())) {
            if (!dolphin.level().isClientSide()) {
                Dolphin babyDolphin = EntityType.DOLPHIN.create(dolphin.level());
                if (babyDolphin != null) {
                    ((ModifiableBaby) babyDolphin).tiny_takeover_backport$setBaby(true);
                    babyDolphin.moveTo(dolphin.getX(), dolphin.getY(), dolphin.getZ(), 0.0F, 0.0F);
                    dolphin.level().addFreshEntity(babyDolphin);
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                }
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(dolphin.level().isClientSide()));
            return;
        }

        if (itemstack.is(ItemTags.FISHES) && this.isBaby()) {
            if (((AgeLockable) this).tiny_takeover_backport$isAgeLocked()) {
                // Locked babies still accept fish through the vanilla treasure interaction.
                return;
            }

            if (!dolphin.level().isClientSide()) {
                this.playSound(SoundEvents.DOLPHIN_EAT, 1.0F, 1.0F);
            }
            // AgeableMob.ageUp(seconds, true), including client prediction and
            // the accumulated adult cooldown, without the treasure-finding event.
            AgeLockable ageable = (AgeLockable) this;
            int oldAge = dolphin.level().isClientSide() ? -1 : ageable.tiny_takeover_backport$getCustomAge();
            int seconds = (int)((float)(-oldAge / 20) * 0.1F);
            int age = Math.min(0, oldAge + seconds * 20);
            this.tiny_takeover_backport$forcedAge += age - oldAge;
            if (this.tiny_takeover_backport$forcedAgeTimer == 0) {
                this.tiny_takeover_backport$forcedAgeTimer = 40;
            }
            if (!dolphin.level().isClientSide()) {
                if (age == 0) {
                    this.tiny_takeover_backport$setBaby(false);
                    age = this.tiny_takeover_backport$forcedAge;
                }
                ageable.tiny_takeover_backport$setCustomAge(age);
            }
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(dolphin.level().isClientSide()));
        }
    }
}
