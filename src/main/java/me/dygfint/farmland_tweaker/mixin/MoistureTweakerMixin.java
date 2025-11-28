package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FarmlandBlock.class)
public abstract class MoistureTweakerMixin {
    @Shadow
    private static boolean hasCrop(BlockView world, BlockPos pos) {
        throw new AssertionError();
    }

    @Redirect(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/FarmlandBlock;hasCrop(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean farmland_tweaker$redirectHasCrop(BlockView world, BlockPos pos) {
        ModConfig.MoistureTweaker config = ModConfig.get().moistureTweaker;
        if (config.enableMoistureTweaker) {
            if (!config.preventCropFarmlandDryToDirt) {
                return false;
            }
        }

        return hasCrop(world, pos);
    }
}
