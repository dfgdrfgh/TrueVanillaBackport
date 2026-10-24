package com.blackgear.vanillabackport.core.mixin.common.copper_age;

import com.blackgear.vanillabackport.common.level.blocks.ShelfBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Only the back of a shelf can supply its 1/2/4 slot-occupancy reading to a
 * comparator. Vanilla 1.21.1 comparators otherwise query all four faces.
 *
 * Only change reads directly targeting our shelf; all other comparators
 * retain their vanilla input and comparator-update semantics.
 */
@Mixin(ComparatorBlock.class)
public abstract class ShelfComparatorMixin {
    @Inject(method = "getInputSignal", at = @At("RETURN"), cancellable = true)
    private void vb$readShelfFromBackOnly(Level level, BlockPos pos, BlockState state,
                                           CallbackInfoReturnable<Integer> result) {
        Direction comparatorFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos behindComparator = pos.relative(comparatorFacing.getOpposite());
        BlockState behindState = level.getBlockState(behindComparator);
        if (behindState.getBlock() instanceof ShelfBlock
            && behindState.getValue(ShelfBlock.FACING) != comparatorFacing.getOpposite()) {
            result.setReturnValue(0);
        }
    }
}
