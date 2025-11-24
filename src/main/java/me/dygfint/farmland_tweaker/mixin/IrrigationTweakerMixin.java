package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(FarmlandBlock.class)
public class IrrigationTweakerMixin {
    @Unique private static final ModConfig CONFIG = ModConfig.get();

    @Unique private static final int RANGE_XZ = CONFIG.irrigationTweaker.rangeXZ;
    @Unique private static final int RANGE_Y_MIN = CONFIG.irrigationTweaker.rangeYmin;
    @Unique private static final int RANGE_Y_MAX = CONFIG.irrigationTweaker.rangeYmax;
    @Unique
    private static final Set<Identifier> EXTRA_BLOCK_IDS = CONFIG.irrigationTweaker.extraHydrationBlocks
            .stream()
            .map(Identifier::tryParse)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    @Inject(method = "isWaterNearby", at = @At("HEAD"), cancellable = true)
    private static void farmland_tweaker$modifyIsWaterNearby(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CONFIG.irrigationTweaker.enableIrrigationTweaker) return;

        if (EXTRA_BLOCK_IDS.contains(Identifier.of("minecraft","farmland"))) {
            cir.setReturnValue(true);
            return;
        }

        BlockPos.Mutable m = new BlockPos.Mutable();

        for (int dy = RANGE_Y_MIN; dy <= RANGE_Y_MAX; dy++) {
            for (int dx = -RANGE_XZ; dx <= RANGE_XZ; dx++) {
                for (int dz = -RANGE_XZ; dz <= RANGE_XZ; dz++) {
                    m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState state = world.getBlockState(m);
                    Identifier id = Registries.BLOCK.getId(state.getBlock());

                    if (world.getFluidState(m).isIn(FluidTags.WATER) || EXTRA_BLOCK_IDS.contains(id)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }

        cir.setReturnValue(false);
    }
}
