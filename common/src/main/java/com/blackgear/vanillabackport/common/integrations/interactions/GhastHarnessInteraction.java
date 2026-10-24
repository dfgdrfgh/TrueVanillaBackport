package com.blackgear.vanillabackport.common.integrations.interactions;

import com.blackgear.platform.common.integration.MobInteraction;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.happy_ghast.HappyGhast;
import com.blackgear.vanillabackport.core.data.tags.ModItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public class GhastHarnessInteraction implements MobInteraction {
    @Override
    public InteractionResult onInteract(Player player, Entity target, InteractionHand hand) {
        if (target instanceof HappyGhast ghast && target.isAlive()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(ModItemTags.HARNESSES)) return InteractionResult.PASS;

            if (!ghast.isHarnessed() && ghast.canBeHarnessed()) {
                if (!player.level().isClientSide) {
                    ghast.equipHarness();
                    ghast.setItemSlot(EquipmentSlot.CHEST, stack.copyWithCount(1));
                    // Vanilla always drops a Happy Ghast's equipped harness.
                    ghast.setDropChance(EquipmentSlot.CHEST, 2.0F);
                    target.level().gameEvent(target, GameEvent.EQUIP, target.position());
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }

                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        return InteractionResult.PASS;
    }
}
