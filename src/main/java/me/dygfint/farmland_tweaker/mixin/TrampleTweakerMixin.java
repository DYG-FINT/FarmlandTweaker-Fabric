package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.access.EntityMixinAccess;
import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class TrampleTweakerMixin extends Block {
    @Shadow
    private static boolean hasCrop(BlockView world, BlockPos pos) {
        throw new AssertionError();
    }

    public TrampleTweakerMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onLandedUpon", at = @At("HEAD"), cancellable = true)
    //? if >= 1.21.5 {
    private void farmland_tweaker$modifyLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
    //?} else {
    /*private void farmland_tweaker$modifyLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) { *///?}
        if (world.isClient()) return;

        ModConfig.TrampleTweaker config = ModConfig.get().trampleTweaker;
        if (!config.enableTrampleTweaker) return;

        if (entity instanceof EntityMixinAccess accessEntity) {
            //? if >= 1.21.2 {
            boolean isGlidingCollision = entity instanceof LivingEntity living && living.isGliding();
            //?} else {
            /*boolean isGlidingCollision = entity instanceof LivingEntity living && living.isFallFlying(); *///?}

            double minTrampleBPS = isGlidingCollision ? config.glideTweaker.minGlideTrampleBPS : config.defaultTweaker.minTrampleBPS;
            double trampleBPSRange = isGlidingCollision ? config.glideTweaker.glideTrampleBPSRange : config.defaultTweaker.trampleBPSRange;

            Vec3d v = accessEntity.farmland_tweaker$getLastVelocity();
            double speed = v.length();
            double bps = speed * 20;

            float chanceValue = (float) ((bps - minTrampleBPS) / trampleBPSRange);
            float entityVolume = entity.getWidth() * entity.getWidth() * entity.getHeight();

            if (canTrampleFarmland(world, pos, entity, config, chanceValue, entityVolume, isGlidingCollision)) {
                //? if >=1.19.4 {
                FarmlandBlock.setToDirt(entity, state, world, pos);
                //?} else {
                /*FarmlandBlock.setToDirt(state, world, pos); *///?}
                trampleSpread(world, pos, entity, config, bps, entityVolume, isGlidingCollision);
            }
            super.onLandedUpon(world, state, pos, entity, fallDistance);
            ci.cancel();
        }
    }

    @Unique
    private static boolean canTrampleFarmland(World world, BlockPos pos, Entity entity, ModConfig.TrampleTweaker config,float chanceValue, float entityVolume, boolean isGlidingCollision) {
        boolean passedTrampleChance = world.random.nextFloat() < chanceValue;
        boolean isLivingEntityRequirementMet = config.requireLivingEntityToTrample ? entity instanceof LivingEntity : true;
        boolean isTrampleAllowed = entity instanceof PlayerEntity ? config.allowPlayerTrample : config.allowMobTrample;
        boolean isLargeEnoughToTrample  = entityVolume >= (isGlidingCollision ? (float) config.glideTweaker.glideTrampleVolumeThreshold : (float) config.defaultTweaker.trampleVolumeThreshold);
        boolean canTrampleCropFarmland = !hasCrop(world, pos) || (isGlidingCollision ? config.glideTweaker.allowGlideTramplingFarmlandUnderCrops : config.defaultTweaker.allowTramplingFarmlandUnderCrops);

        return passedTrampleChance && isLivingEntityRequirementMet && isTrampleAllowed && isLargeEnoughToTrample && canTrampleCropFarmland;
    }

    @Unique
    private static void trampleSpread(World world, BlockPos pos, Entity entity, ModConfig.TrampleTweaker config, double bps, float entityVolume, boolean isGlidingCollision) {
        if (!config.farmlandTrampleSpread.enableSpread) return;

        int radius = getRadius(config.farmlandTrampleSpread, bps, entityVolume, isGlidingCollision);

        BlockPos.Mutable m = new BlockPos.Mutable();

        for (int dy = config.farmlandTrampleSpread.spreadRangeMinY; dy <= config.farmlandTrampleSpread.spreadRangeMaxY; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= (radius + 0.5) * (radius + 0.5)) {
                        m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                        BlockState targetState = world.getBlockState(m);
                        if (targetState.getBlock() instanceof FarmlandBlock) {
                            //? if >=1.19.4 {
                            FarmlandBlock.setToDirt(entity, targetState, world, m);
                            //?} else {
                            /*FarmlandBlock.setToDirt(targetState, world, m); *///?}
                        }
                    }
                }
            }
        }
    }

    @Unique
    private static int getRadius(ModConfig.TrampleTweaker.FarmlandTrampleSpread config, double bps, float entityVolume, boolean isGlidingCollision) {
        int baseSpreadRadius = isGlidingCollision ? config.glideSpreadRadius.glideBaseSpreadRadius : config.defaultSpreadRadius.baseSpreadRadius;
        double minSpreadBPS = isGlidingCollision ? config.glideSpreadRadius.glideMinSpreadBPS : config.defaultSpreadRadius.minSpreadBPS;
        double spreadBPSRange = isGlidingCollision ? config.glideSpreadRadius.glideSpreadBPSRange : config.defaultSpreadRadius.spreadBPSRange;
        double volumeCorrectionDivisor = isGlidingCollision ? config.glideSpreadRadius.glideVolumeCorrectionDivisor : config.defaultSpreadRadius.volumeCorrectionDivisor;

        double volumeFactor = getVolumeFactor(config, entityVolume, volumeCorrectionDivisor);

        double fallFactor = (bps - minSpreadBPS) / spreadBPSRange;
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
}

