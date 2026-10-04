package com.evandev.tiny_takeover_backport.mixin;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    @ModifyConstant(method = "updateTrades", constant = @Constant(intValue = 2))
    private int tiny_takeover_backport$masterLibrarianOffers(int count) {
        Villager villager = (Villager) (Object) this;
        return villager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN
                && villager.getVillagerData().getLevel() == 5 ? 3 : count;
    }
}
