package me.dygfint.farmland_tweaker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FarmlandBlock.class)
public abstract class MoistureTweakerMixin {
    @ModifyExpressionValue(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/FarmlandBlock;hasCrop(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean farmland_tweaker$redirectHasCrop(boolean original) {
        ModConfig.MoistureTweaker config = ModConfig.get().moistureTweaker;
        if (config.enableMoistureTweaker) {
            if (!config.preventCropFarmlandDryToDirt) {
                return false;
            }
        }

        return original;
    }
}
