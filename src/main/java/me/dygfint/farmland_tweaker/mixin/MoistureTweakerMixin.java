package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
public class MoistureTweakerMixin {
    @Unique private static final ModConfig CONFIG = ModConfig.get();

    @Unique private static final boolean PREVENT_CROP_FARMLAND_DRY_TO_DIRT = CONFIG.moistureTweaker.preventCropFarmlandDryToDirt;

    @Inject(method = "hasCrop", at = @At("HEAD"), cancellable = true)
    private static void farmland_tweaker$modifyHasCrop(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CONFIG.moistureTweaker.enableMoistureTweaker) return;

        if (!PREVENT_CROP_FARMLAND_DRY_TO_DIRT) {
            cir.setReturnValue(false);
        }
    }
}
