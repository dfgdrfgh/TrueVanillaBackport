package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.evandev.tiny_takeover_backport.entity.*;
import com.evandev.tiny_takeover_backport.registry.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import com.evandev.tiny_takeover_backport.registry.ModTags;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin implements AgeLockable, SoundVariantHolder {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$protectFoxesFromCubs(LivingEntity target, CallbackInfo ci) {
        if ((Object) this instanceof PolarBear bear && bear.isBaby() && target instanceof Fox) {
            ci.cancel();
        }
    }

    @Unique
    private static final EntityDataAccessor<Boolean> tiny_takeover_backport$DATA_AGE_LOCKED =
            SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<String> tiny_takeover_backport$DATA_SOUND_VARIANT =
            SynchedEntityData.defineId(Mob.class, EntityDataSerializers.STRING);
    @Unique
    private int tiny_takeover_backport$ageLockParticleTimer = 0;
    @Unique
    private int tiny_takeover_backport$age = 0;

    @Override
    public boolean tiny_takeover_backport$isAgeLocked() {
        return ((Mob) (Object) this).getEntityData().get(tiny_takeover_backport$DATA_AGE_LOCKED);
    }

    @Override
    public void tiny_takeover_backport$setAgeLocked(boolean ageLocked) {
        ((Mob) (Object) this).getEntityData().set(tiny_takeover_backport$DATA_AGE_LOCKED, ageLocked);
    }

    @Override
    public int tiny_takeover_backport$getAgeLockParticleTimer() {
        return this.tiny_takeover_backport$ageLockParticleTimer;
    }

    @Override
    public void tiny_takeover_backport$setAgeLockParticleTimer(int timer) {
        this.tiny_takeover_backport$ageLockParticleTimer = timer;
    }

    @Override
    public int tiny_takeover_backport$getCustomAge() {
        return this.tiny_takeover_backport$age;
    }

    @Override
    public void tiny_takeover_backport$setCustomAge(int age) {
        this.tiny_takeover_backport$age = age;
    }

    @Override
    public String tiny_takeover_backport$getSoundVariant() {
        return ((Mob) (Object) this).getEntityData().get(tiny_takeover_backport$DATA_SOUND_VARIANT);
    }

    @Override
    public void tiny_takeover_backport$setSoundVariant(String variant) {
        ((Mob) (Object) this).getEntityData().set(tiny_takeover_backport$DATA_SOUND_VARIANT, variant);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void tiny_takeover_backport$defineBabyData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(tiny_takeover_backport$DATA_AGE_LOCKED, false);
        builder.define(tiny_takeover_backport$DATA_SOUND_VARIANT, AnimalSoundVariants.CLASSIC);
        if ((Object) this instanceof Squid) {
            builder.define(ModEntityData.SQUID_BABY_ID, false);
        } else if ((Object) this instanceof Dolphin) {
            builder.define(ModEntityData.DOLPHIN_BABY_ID, false);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("AgeLocked", this.tiny_takeover_backport$isAgeLocked());
        if ((Object) this instanceof Dolphin || (Object) this instanceof Squid) {
            tag.putInt("Age", this.tiny_takeover_backport$age);
        }
        if (AnimalSoundVariants.hasVariants(((Mob) (Object) this).getType())) {
            tag.putString("sound_variant", this.tiny_takeover_backport$getSoundVariant());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        this.tiny_takeover_backport$setAgeLocked(tag.getBoolean("AgeLocked"));
        if (((Object) this instanceof Dolphin || (Object) this instanceof Squid) && tag.contains("Age")) {
            this.tiny_takeover_backport$age = tag.getInt("Age");
        }
        if (tag.contains("sound_variant")) {
            this.tiny_takeover_backport$setSoundVariant(tag.getString("sound_variant"));
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (this.tiny_takeover_backport$ageLockParticleTimer > 0) {
            if (mob.level().isClientSide() && this.tiny_takeover_backport$ageLockParticleTimer % 2 == 0) {
                boolean locked = this.tiny_takeover_backport$isAgeLocked();
                float yOffset = locked ? 0.2F : 0.0F;
                double x = mob.getRandomX(1.0);
                double y = mob.getY() + (1.0 + (2.0 * mob.getRandom().nextDouble() - 1.0) * 0.2) * mob.getBbHeight() + yOffset;
                double z = mob.getRandomZ(1.0);
                mob.level().addParticle(
                        locked ? ModRegistry.PAUSE_MOB_GROWTH : ModRegistry.RESET_MOB_GROWTH,
                        x, y, z,
                        0.0, 0.0, 0.0
                );
            }
            this.tiny_takeover_backport$ageLockParticleTimer--;
        }

        if (!mob.level().isClientSide() && mob.isAlive() && (mob instanceof Dolphin || mob instanceof Squid)) {
            if (mob.isBaby() && !this.tiny_takeover_backport$isAgeLocked()) {
                this.tiny_takeover_backport$age++;
                if (this.tiny_takeover_backport$age >= 0) {
                    ((ModifiableBaby) mob).tiny_takeover_backport$setBaby(false);
                }
            } else if (this.tiny_takeover_backport$age > 0) {
                this.tiny_takeover_backport$age--;
            }
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void tiny_takeover_backport$handleInteractions(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Mob mob = (Mob) (Object) this;
        ItemStack itemStack = player.getItemInHand(hand);

        if ((mob instanceof AgeableMob || mob instanceof Dolphin || mob instanceof Squid) && mob instanceof AgeLockable lockable) {
            if (mob.getType().is(ModTags.CANNOT_BE_AGE_LOCKED)) return;

            if (itemStack.is(ModRegistry.GOLDEN_DANDELION_ITEM) && mob.isBaby() && lockable.tiny_takeover_backport$getAgeLockParticleTimer() == 0) {
                lockable.tiny_takeover_backport$setAgeLocked(!lockable.tiny_takeover_backport$isAgeLocked());
                // Both pausing and resuming growth restart the vanilla baby timer.
                if (mob instanceof AgeableMob ageable) {
                    ageable.setAge(mob instanceof Sniffer ? -48000 : -24000);
                } else {
                    lockable.tiny_takeover_backport$setCustomAge(-24000);
                }
                if (lockable.tiny_takeover_backport$isAgeLocked()) {
                    mob.setPersistenceRequired();
                }
                itemStack.consume(1, player);
                lockable.tiny_takeover_backport$setAgeLockParticleTimer(40);

                mob.level().playSound(
                        null,
                        mob.blockPosition(),
                        lockable.tiny_takeover_backport$isAgeLocked() ? ModRegistry.GOLDEN_DANDELION_USE : ModRegistry.GOLDEN_DANDELION_UNUSE,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                cir.setReturnValue(InteractionResult.sidedSuccess(mob.level().isClientSide()));
            }
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"), cancellable = true)
    private void tiny_takeover_backport$onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob mob = (Mob) (Object) this;
        String soundVariant = AnimalSoundVariants.pickRandom(mob.getType(), level.getRandom());
        if (soundVariant != null) {
            this.tiny_takeover_backport$setSoundVariant(soundVariant);
        }

        boolean dolphin = mob instanceof Dolphin;
        if (dolphin && ModConfig.get().spawnBabyDolphin || mob instanceof Squid && ModConfig.get().spawnBabySquid) {
            AgeableMob.AgeableMobGroupData aquaticGroup = groupData instanceof AgeableMob.AgeableMobGroupData ageableGroup
                    ? ageableGroup : new AgeableMob.AgeableMobGroupData(dolphin ? 0.10F : 0.05F);
            if (aquaticGroup.isShouldSpawnBaby() && aquaticGroup.getGroupSize() > 0 && level.getRandom().nextFloat() <= aquaticGroup.getBabySpawnChance()) {
                ((ModifiableBaby) mob).tiny_takeover_backport$setBaby(true);
            }
            aquaticGroup.increaseGroupSizeByOne();
            cir.setReturnValue(aquaticGroup);
        }
    }
}
