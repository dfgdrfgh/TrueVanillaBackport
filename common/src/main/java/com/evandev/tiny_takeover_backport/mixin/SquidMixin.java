package com.evandev.tiny_takeover_backport.mixin;

import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.evandev.tiny_takeover_backport.entity.AgeLockable;
import com.evandev.tiny_takeover_backport.entity.ModEntityData;
import com.evandev.tiny_takeover_backport.entity.ModifiableBaby;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Squid.class)
public abstract class SquidMixin extends WaterAnimal implements ModifiableBaby {

    @ModifyArg(method = "spawnInk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"), index = 0)
    private double tiny_takeover_backport$babyInkSpread(double spread) {
        return this.isBaby() ? spread - 0.3 + 0.1F : spread;
    }

    protected SquidMixin(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isBaby() {
        return this.entityData.get(ModEntityData.SQUID_BABY_ID);
    }

    @Unique
    @Override
    public void tiny_takeover_backport$setBaby(boolean baby) {
        this.entityData.set(ModEntityData.SQUID_BABY_ID, baby);
        if (baby) {
            ((AgeLockable) this).tiny_takeover_backport$setCustomAge(-24000);
        } else {
            ((AgeLockable) this).tiny_takeover_backport$setCustomAge(0);
        }
        this.refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (ModEntityData.SQUID_BABY_ID.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsBaby", this.isBaby());
        tag.putInt("Age", ((AgeLockable) this).tiny_takeover_backport$getCustomAge());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.tiny_takeover_backport$setBaby(tag.contains("Age") ? tag.getInt("Age") < 0 : tag.getBoolean("IsBaby"));
        if (tag.contains("Age")) {
            ((AgeLockable) this).tiny_takeover_backport$setCustomAge(tag.getInt("Age"));
        }
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!ModConfig.get().spawnBabySquid) return super.mobInteract(player, hand);
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.getItem() instanceof SpawnEggItem egg && egg.spawnsEntity(itemstack, this.getType())) {
            if (!this.level().isClientSide()) {
                Squid babySquid = (Squid) this.getType().create(this.level());
                if (babySquid != null) {
                    ((ModifiableBaby) babySquid).tiny_takeover_backport$setBaby(true);
                    babySquid.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                    this.level().addFreshEntity(babySquid);
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                }
            }

            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }
}
