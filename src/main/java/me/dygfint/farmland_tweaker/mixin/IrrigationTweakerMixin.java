package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(FarmlandBlock.class)
public class IrrigationTweakerMixin {
    @Inject(method = "isWaterNearby", at = @At("HEAD"), cancellable = true)
    private static void farmland_tweaker$modifyIsWaterNearby(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ModConfig config = ModConfig.get();
        if (!config.irrigationTweaker.enableIrrigationTweaker) return;

        int rangeXZ = config.irrigationTweaker.rangeXZ;
        int rangeYmin = config.irrigationTweaker.rangeYmin;
        int rangeYmax = config.irrigationTweaker.rangeYmax;
        Set<Identifier> EXTRA_BLOCK_IDS = config.irrigationTweaker.extraHydrationBlocks
                .stream()
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //? if >=1.21 {
        if (EXTRA_BLOCK_IDS.contains(Identifier.of("minecraft","farmland"))) {
        //?} else {
        /*if (EXTRA_BLOCK_IDS.contains(new Identifier("minecraft","farmland"))) { *///?}
            cir.setReturnValue(true);
            return;
        }

        BlockPos.Mutable m = new BlockPos.Mutable();

        for (int dy = rangeYmin; dy <= rangeYmax; dy++) {
            for (int dx = -rangeXZ; dx <= rangeXZ; dx++) {
                for (int dz = -rangeXZ; dz <= rangeXZ; dz++) {
                    m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState state = world.getBlockState(m);
                    //? if >= 1.19.3 {
                    Identifier id = net.minecraft.registry.Registries.BLOCK.getId(state.getBlock());
                    if (world.getFluidState(m).isIn(net.minecraft.registry.tag.FluidTags.WATER) || EXTRA_BLOCK_IDS.contains(id)) {
                    //?} else {
                    /*Identifier id = net.minecraft.util.registry.Registry.BLOCK.getId(state.getBlock());
                    if (world.getFluidState(m).isIn(net.minecraft.tag.FluidTags.WATER) || EXTRA_BLOCK_IDS.contains(id)) { *///?}
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }

        cir.setReturnValue(false);
    }
}
