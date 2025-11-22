package io.dygfint.irrigation_tweaker.mixin;

import io.dygfint.irrigation_tweaker.config.ModConfig;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @Unique private static final ModConfig config = ModConfig.get();
    @Unique private static final int rangeXZ = config.rangeXZ;
    @Unique private static final int rangeYmin = config.rangeYmin;
    @Unique private static final int rangeYmax = config.rangeYmax;

    @Inject(method = "isWaterNearby", at = @At("HEAD"), cancellable = true)
    private static void irrigation_tweaker$onIsNearWater(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!config.enable) return;

        BlockPos.Mutable m = new BlockPos.Mutable();

        for (int dy = rangeYmin; dy <= rangeYmax; dy++) {
            for (int dx = -rangeXZ; dx <= rangeXZ; dx++) {
                for (int dz = -rangeXZ; dz <= rangeXZ; dz++) {
                    m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);

                    if (world.getFluidState(m).isIn(FluidTags.WATER)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }

        cir.setReturnValue(false);
    }
}
