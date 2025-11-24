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
    @Unique private static final ModConfig CONFIG = ModConfig.get();

    @Unique private static final float MIN_TRAMPLE_FALL_DISTANCE = (float) CONFIG.trampleTweaker.minTrampleFallHeight;
    @Unique private static final float TRAMPLE_CHANCE_RANGE = (float) CONFIG.trampleTweaker.trampleFallRange;
    @Unique private static final boolean REQUIRE_LIVING_ENTITY_TO_TRAMPLE = CONFIG.trampleTweaker.requireLivingEntityToTrample;
    @Unique private static final boolean ALLOW_PLAYER_TRAMPLE = CONFIG.trampleTweaker.allowPlayerTrample;
    @Unique private static final boolean ALLOW_MOB_TRAMPLE = CONFIG.trampleTweaker.allowMobTrample;
    @Unique private static final float TRAMPLE_VOLUME_THRESHOLD = (float) CONFIG.trampleTweaker.trampleVolumeThreshold;
    @Unique private static final boolean ALLOW_TRAMPLING_FARMLAND_UNDER_CROPS = CONFIG.trampleTweaker.allowTramplingFarmlandUnderCrops;
    @Unique private static final double MIN_SPREAD_FALL_DISTANCE = CONFIG.trampleTweaker.farmlandTrampleSpread.minSpreadFallDistance;
    @Unique private static final double SPREAD_FALL_RANGE = CONFIG.trampleTweaker.farmlandTrampleSpread.spreadFallRange;
    @Unique private static final int MAX_SPREAD_RADIUS = CONFIG.trampleTweaker.farmlandTrampleSpread.maxSpreadRadius;
    @Unique private static final int SPREAD_RANGE_MIN_Y = CONFIG.trampleTweaker.farmlandTrampleSpread.spreadRangeMinY;
    @Unique private static final int SPREAD_RANGE_MAX_Y = CONFIG.trampleTweaker.farmlandTrampleSpread.spreadRangeMaxY;
    @Unique private static final int GLIDE_INCREASE_RADIUS = CONFIG.trampleTweaker.farmlandTrampleSpread.glideIncreaseSpreadRadius;

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

            boolean passedTrampleChance = world.random.nextFloat() < chanceValue;
            boolean isLivingEntityRequirementMet = REQUIRE_LIVING_ENTITY_TO_TRAMPLE ? entity instanceof LivingEntity : true;
            boolean isTrampleAllowed = entity instanceof PlayerEntity ? ALLOW_PLAYER_TRAMPLE : ALLOW_MOB_TRAMPLE;
            boolean isLargeEnoughToTrample  = entityVolume > TRAMPLE_VOLUME_THRESHOLD;
            boolean canTrampleCropFarmland = hasCrop ? ALLOW_TRAMPLING_FARMLAND_UNDER_CROPS : true;

            if (isGlidingCollision || passedTrampleChance && isLivingEntityRequirementMet && isTrampleAllowed && isLargeEnoughToTrample && canTrampleCropFarmland) {
                FarmlandBlock.setToDirt(entity, state, world, pos);

                if (CONFIG.trampleTweaker.farmlandTrampleSpread.enableSpread) {
                    double Multiplier = (float) ((fallDistance - MIN_TRAMPLE_FALL_DISTANCE) / SPREAD_FALL_RANGE);
                    Multiplier = MathHelper.clamp(Multiplier, 0.0, 1.0);

                    int maxSpreadRadius = isGlidingCollision ? MAX_SPREAD_RADIUS + GLIDE_INCREASE_RADIUS : MAX_SPREAD_RADIUS;
                    int radius = (int)(maxSpreadRadius * Multiplier);

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

                    isGlidingCollision = false;
                }
            }
        }

        super.onLandedUpon(world, state, pos, entity, fallDistance);
        ci.cancel();
    }

    @Override
    public void farmland_tweaker$setGlidingCollision() {
        this.isGlidingCollision = true;
    }
}

