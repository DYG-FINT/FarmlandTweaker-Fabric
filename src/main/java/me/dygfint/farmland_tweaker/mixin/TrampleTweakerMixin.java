package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.access.TrampleTweakerMixinAccess;
import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class TrampleTweakerMixin extends Block implements TrampleTweakerMixinAccess {
    @Unique
    private boolean isGlidingCollision = false;

    public TrampleTweakerMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onLandedUpon", at = @At("HEAD"), cancellable = true)
    private void farmland_tweaker$modifyLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
        if (world.isClient()) return;

        ModConfig.TrampleTweaker config = ModConfig.get().trampleTweaker;
        if (!(config.enableTrampleTweaker || isGlidingCollision)) return;

        float chanceValue = (float) ((fallDistance - (float) config.minTrampleFallHeight) / (float) config.trampleFallRange);
        float entityVolume = entity.getWidth() * entity.getWidth() * entity.getHeight();
        boolean hasCrop = world.getBlockState(pos.up()).isIn(BlockTags.MAINTAINS_FARMLAND);
        if (canTrampleFarmland(world, entity, config, chanceValue, entityVolume, hasCrop, isGlidingCollision)) {
            FarmlandBlock.setToDirt(entity, state, world, pos);
            if (config.farmlandTrampleSpread.enableSpread) {
                int radius = getRadius(config.farmlandTrampleSpread, fallDistance, entityVolume, isGlidingCollision);
                BlockPos.Mutable m = new BlockPos.Mutable();
                for (int dy = config.farmlandTrampleSpread.spreadRangeMinY; dy <= config.farmlandTrampleSpread.spreadRangeMaxY; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            if (dx * dx + dz * dz <= (radius + 0.5) * (radius + 0.5)) {
                                m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                                BlockState targetState = world.getBlockState(m);
                                if (targetState.getBlock() instanceof FarmlandBlock) {
                                    FarmlandBlock.setToDirt(entity, targetState, world, m);
                                }
                            }
                        }
                    }
                }
            }
            isGlidingCollision = false;
        }
        super.onLandedUpon(world, state, pos, entity, fallDistance);
        ci.cancel();
    }

    @Unique
    private static boolean canTrampleFarmland(World world, Entity entity, ModConfig.TrampleTweaker config,float chanceValue, float entityVolume, boolean hasCrop, boolean isGlidingCollision) {
        boolean passedTrampleChance = world.random.nextFloat() < chanceValue;
        boolean isLivingEntityRequirementMet = config.requireLivingEntityToTrample ? entity instanceof LivingEntity : true;
        boolean isTrampleAllowed = entity instanceof PlayerEntity ? config.allowPlayerTrample : config.allowMobTrample;
        boolean isLargeEnoughToTrample  = entityVolume > (float) config.trampleVolumeThreshold;
        boolean canTrampleCropFarmland = hasCrop ? config.allowTramplingFarmlandUnderCrops : true;

        return passedTrampleChance && isLivingEntityRequirementMet && isTrampleAllowed && isLargeEnoughToTrample && canTrampleCropFarmland;
    }

    @Unique
    private static int getRadius(ModConfig.TrampleTweaker.FarmlandTrampleSpread config, double fallDistance, float entityVolume, boolean isGlidingCollision) {
        int baseSpreadRadius = isGlidingCollision ? config.glideSpreadRadius.glideBaseSpreadRadius : config.defaultSpreadRadius.baseSpreadRadius;
        double minSpreadFallDistance = isGlidingCollision ? config.glideSpreadRadius.glideMinSpreadFallDistance : config.defaultSpreadRadius.minSpreadFallDistance;
        double spreadFallRange = isGlidingCollision ? config.glideSpreadRadius.glideSpreadFallRange : config.defaultSpreadRadius.spreadFallRange;
        double volumeCorrectionDivisor = isGlidingCollision ? config.glideSpreadRadius.glideVolumeCorrectionDivisor : config.defaultSpreadRadius.volumeCorrectionDivisor;

        double volumeFactor = getVolumeFactor(config, entityVolume, volumeCorrectionDivisor);

        double fallFactor = (fallDistance - minSpreadFallDistance) / spreadFallRange;
        fallFactor = MathHelper.clamp(fallFactor, 0.0, 1.0);

        double finalMultiplier = fallFactor * volumeFactor;

        return (int)(baseSpreadRadius * finalMultiplier);
    }

    @Unique
    private static double getVolumeFactor(ModConfig.TrampleTweaker.FarmlandTrampleSpread config, float entityVolume, double volumeCorrectionDivisor) {
        double volumeFactor = 1.0;

        if (ModConfig.get().trampleTweaker.farmlandTrampleSpread.volumeScaling.enableVolumeScaling) {
            float normalized = (float) (entityVolume / volumeCorrectionDivisor);

            normalized = (float) MathHelper.clamp(normalized, 0.0,  config.volumeScaling.volumeClampMax);

            normalized = switch (config.volumeScaling.volumeScaleMode) {
                case sqrt -> (float) Math.sqrt(normalized);
                case cbrt -> (float) Math.cbrt(normalized);
                case quadratic -> normalized * normalized;
                case cubic -> normalized * normalized * normalized;
                case log -> (float) Math.log(normalized + 1.0);
                default -> normalized;
            };

            volumeFactor =  MathHelper.clamp(normalized, config.volumeScaling.volumeScaleMin, config.volumeScaling.volumeScaleMax);
        }

        return volumeFactor;
    }

    @Override
    public void farmland_tweaker$setGlidingCollision() {
        this.isGlidingCollision = true;
    }
}

