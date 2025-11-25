package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
public class MoistureTweakerMixin {
    @Inject(method = "hasCrop", at = @At("HEAD"), cancellable = true)
    private static void farmland_tweaker$modifyHasCrop(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ModConfig.MoistureTweaker config = ModConfig.get().moistureTweaker;
        if (!config.enableMoistureTweaker) return;

        if (!config.preventCropFarmlandDryToDirt) {
            cir.setReturnValue(false);
        }
    }
}
