package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.access.TrampleTweakerMixinAccess;
import me.dygfint.farmland_tweaker.config.ModConfig;
import me.dygfint.farmland_tweaker.config.ModConfig.TrampleTweaker.FarmlandTrampleSpread.VolumeScaling.VolumeScaleMode;
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
    @Unique private static final ModConfig CONFIG = ModConfig.get();

    @Unique private static final float MIN_TRAMPLE_FALL_DISTANCE = (float) CONFIG.trampleTweaker.minTrampleFallHeight;
    @Unique private static final float TRAMPLE_CHANCE_RANGE = (float) CONFIG.trampleTweaker.trampleFallRange;
    @Unique private static final boolean REQUIRE_LIVING_ENTITY_TO_TRAMPLE = CONFIG.trampleTweaker.requireLivingEntityToTrample;
    @Unique private static final boolean ALLOW_PLAYER_TRAMPLE = CONFIG.trampleTweaker.allowPlayerTrample;
    @Unique private static final boolean ALLOW_MOB_TRAMPLE = CONFIG.trampleTweaker.allowMobTrample;
    @Unique private static final float TRAMPLE_VOLUME_THRESHOLD = (float) CONFIG.trampleTweaker.trampleVolumeThreshold;
    @Unique private static final boolean ALLOW_TRAMPLING_FARMLAND_UNDER_CROPS = CONFIG.trampleTweaker.allowTramplingFarmlandUnderCrops;

    @Unique private static final int BASE_SPREAD_RADIUS = CONFIG.trampleTweaker.farmlandTrampleSpread.defaultSpreadRadius.baseSpreadRadius;
    @Unique private static final double MIN_SPREAD_FALL_DISTANCE = CONFIG.trampleTweaker.farmlandTrampleSpread.defaultSpreadRadius.minSpreadFallDistance;
    @Unique private static final double SPREAD_FALL_RANGE = CONFIG.trampleTweaker.farmlandTrampleSpread.defaultSpreadRadius.spreadFallRange;
    @Unique private static final double VOLUME_CORRECTION_DIVISOR = CONFIG.trampleTweaker.farmlandTrampleSpread.defaultSpreadRadius.volumeCorrectionDivisor;

    @Unique private static final int GLIDE_BASE_SPREAD_RADIUS = CONFIG.trampleTweaker.farmlandTrampleSpread.glideSpreadRadius.glideBaseSpreadRadius;
    @Unique private static final double GLIDE_MIN_SPREAD_FALL_DISTANCE = CONFIG.trampleTweaker.farmlandTrampleSpread.glideSpreadRadius.glideMinSpreadFallDistance;
    @Unique private static final double GLIDE_SPREAD_FALL_RANGE = CONFIG.trampleTweaker.farmlandTrampleSpread.glideSpreadRadius.glideSpreadFallRange;
    @Unique private static final double GLIDE_VOLUME_CORRECTION_DIVISOR = CONFIG.trampleTweaker.farmlandTrampleSpread.glideSpreadRadius.glideVolumeCorrectionDivisor;

    @Unique private static final VolumeScaleMode VOLUME_SCALE_MODE = CONFIG.trampleTweaker.farmlandTrampleSpread.volumeScaling.volumeScaleMode;
    @Unique private static final double VOLUME_CLAMP_MAX = CONFIG.trampleTweaker.farmlandTrampleSpread.volumeScaling.volumeClampMax;
    @Unique private static final double VOLUME_SCALE_MIN = CONFIG.trampleTweaker.farmlandTrampleSpread.volumeScaling.volumeScaleMin;
    @Unique private static final double VOLUME_SCALE_MAX = CONFIG.trampleTweaker.farmlandTrampleSpread.volumeScaling.volumeScaleMax;

    @Unique private static final int SPREAD_RANGE_MIN_Y = CONFIG.trampleTweaker.farmlandTrampleSpread.spreadRangeMinY;
    @Unique private static final int SPREAD_RANGE_MAX_Y = CONFIG.trampleTweaker.farmlandTrampleSpread.spreadRangeMaxY;



    @Unique
    private boolean isGlidingCollision = false;

    public TrampleTweakerMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onLandedUpon", at = @At("HEAD"), cancellable = true)
    private void farmland_tweaker$modifyLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
        if (!(CONFIG.trampleTweaker.enableTrampleTweaker || isGlidingCollision)) return;

        if (!world.isClient()) {
            float chanceValue = (float) ((fallDistance - MIN_TRAMPLE_FALL_DISTANCE) / TRAMPLE_CHANCE_RANGE);
            float entityVolume = entity.getWidth() * entity.getWidth() * entity.getHeight();
            boolean hasCrop = world.getBlockState(pos.up()).isIn(BlockTags.MAINTAINS_FARMLAND);

            if (canTrampleFarmland(world, entity, chanceValue, entityVolume, hasCrop, isGlidingCollision)) {
                FarmlandBlock.setToDirt(entity, state, world, pos);

                if (CONFIG.trampleTweaker.farmlandTrampleSpread.enableSpread) {
                    int radius = getRadius(fallDistance, entityVolume, isGlidingCollision);

                    BlockPos.Mutable m = new BlockPos.Mutable();

                    for (int dy = SPREAD_RANGE_MIN_Y; dy <= SPREAD_RANGE_MAX_Y; dy++) {
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
        }

        super.onLandedUpon(world, state, pos, entity, fallDistance);
        ci.cancel();
    }

    @Unique
    private static boolean canTrampleFarmland(World world, Entity entity,float chanceValue, float entityVolume, boolean hasCrop, boolean isGlidingCollision) {
        boolean passedTrampleChance = world.random.nextFloat() < chanceValue;
        boolean isLivingEntityRequirementMet = REQUIRE_LIVING_ENTITY_TO_TRAMPLE ? entity instanceof LivingEntity : true;
        boolean isTrampleAllowed = entity instanceof PlayerEntity ? ALLOW_PLAYER_TRAMPLE : ALLOW_MOB_TRAMPLE;
        boolean isLargeEnoughToTrample  = entityVolume > TRAMPLE_VOLUME_THRESHOLD;
        boolean canTrampleCropFarmland = hasCrop ? ALLOW_TRAMPLING_FARMLAND_UNDER_CROPS : true;

        return isGlidingCollision || passedTrampleChance && isLivingEntityRequirementMet && isTrampleAllowed && isLargeEnoughToTrample && canTrampleCropFarmland;
    }

    @Unique
    private static int getRadius(double fallDistance, float entityVolume, boolean isGlidingCollision) {
        int baseSpreadRadius = isGlidingCollision ? GLIDE_BASE_SPREAD_RADIUS : BASE_SPREAD_RADIUS;
        double minSpreadFallDistance = isGlidingCollision ? GLIDE_MIN_SPREAD_FALL_DISTANCE : MIN_SPREAD_FALL_DISTANCE;
        double spreadFallRange = isGlidingCollision ? GLIDE_SPREAD_FALL_RANGE : SPREAD_FALL_RANGE;
        double volumeCorrectionDivisor = isGlidingCollision ? GLIDE_VOLUME_CORRECTION_DIVISOR : VOLUME_CORRECTION_DIVISOR;

        double volumeFactor = getVolumeFactor(entityVolume, volumeCorrectionDivisor);

        double fallFactor = (fallDistance - minSpreadFallDistance) / spreadFallRange;
        fallFactor = MathHelper.clamp(fallFactor, 0.0, 1.0);

        double finalMultiplier = MathHelper.clamp(fallFactor * volumeFactor, 0.0, 1.0);

        return (int)(baseSpreadRadius * finalMultiplier);
    }

    @Unique
    private static double getVolumeFactor(float entityVolume, double volumeCorrectionDivisor) {
        double volumeFactor = 1.0;

        if (CONFIG.trampleTweaker.farmlandTrampleSpread.volumeScaling.enableVolumeScaling) {
            float normalized = (float) (entityVolume / volumeCorrectionDivisor);

            normalized = switch (VOLUME_SCALE_MODE) {
                case sqrt -> (float) Math.sqrt(normalized);
                case cbrt -> (float) Math.cbrt(normalized);
                case quadratic -> normalized * normalized;
                case cubic -> normalized * normalized * normalized;
                case log -> (float) Math.log(normalized + 1.0);
                default -> normalized;
            };

            volumeFactor = VOLUME_SCALE_MIN + MathHelper.clamp(normalized, 0.0, VOLUME_CLAMP_MAX) * (VOLUME_SCALE_MAX - VOLUME_SCALE_MIN);
        }

        return volumeFactor;
    }

    @Override
    public void farmland_tweaker$setGlidingCollision() {
        this.isGlidingCollision = true;
    }
}

