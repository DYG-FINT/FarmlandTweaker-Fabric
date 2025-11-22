package io.dygfint.farmland_tweaker.mixin;

import io.dygfint.farmland_tweaker.config.ModConfig;
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
public class FarmlandBlockMixin {
    @Unique private static final ModConfig config = ModConfig.get();
    @Unique private static final int rangeXZ = config.rangeXZ;
    @Unique private static final int rangeYmin = config.rangeYmin;
    @Unique private static final int rangeYmax = config.rangeYmax;
    @Unique
    private static final Set<Identifier> extraBlockIds = config.extraHydrationBlocks
            .stream()
            .map(Identifier::tryParse)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    @Inject(method = "isWaterNearby", at = @At("HEAD"), cancellable = true)
    private static void farmland_tweaker$onIsNearWater(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!config.enable) return;

        BlockPos.Mutable m = new BlockPos.Mutable();

        for (int dy = rangeYmin; dy <= rangeYmax; dy++) {
            for (int dx = -rangeXZ; dx <= rangeXZ; dx++) {
                for (int dz = -rangeXZ; dz <= rangeXZ; dz++) {
                    m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState state = world.getBlockState(m);
                    Identifier id = Registries.BLOCK.getId(state.getBlock());

                    if (world.getFluidState(m).isIn(FluidTags.WATER) || extraBlockIds.contains(id)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }

        cir.setReturnValue(false);
    }
}
