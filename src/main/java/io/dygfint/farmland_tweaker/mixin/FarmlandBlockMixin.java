package io.dygfint.farmland_tweaker.mixin;

import io.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin extends Block {
    @Unique private static final ModConfig CONFIG = ModConfig.get();
    @Unique private static final int RANGE_XZ = CONFIG.irrigationTweaker.rangeXZ;
    @Unique private static final int RANGE_Y_MIN = CONFIG.irrigationTweaker.rangeYmin;
    @Unique private static final int RANGE_Y_MAX = CONFIG.irrigationTweaker.rangeYmax;
    @Unique
    private static final Set<Identifier> EXTRA_BLOCK_IDS = CONFIG.irrigationTweaker.extraHydrationBlocks
            .stream()
            .map(Identifier::tryParse)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    @Unique private static final float MIN_FALL_DISTANCE = (float) CONFIG.trampleTweaker.minTrampleFallHeight;
    @Unique private static final float CHANCE_RANGE = (float) CONFIG.trampleTweaker.trampleFallRange;
    @Unique private static final boolean REQUIRE_LIVING_ENTITY_TO_TRAMPLE = CONFIG.trampleTweaker.requireLivingEntityToTrample;
    @Unique private static final boolean ALLOW_PLAYER_TRAMPLE = CONFIG.trampleTweaker.allowPlayerTrample;
    @Unique private static final boolean ALLOW_MOB_TRAMPLE = CONFIG.trampleTweaker.allowMobTrample;
    @Unique private static final float TRAMPLE_VOLUME_THRESHOLD = (float) CONFIG.trampleTweaker.trampleVolumeThreshold;

    public FarmlandBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "isWaterNearby", at = @At("HEAD"), cancellable = true)
    private static void farmland_tweaker$modifyIsWaterNearby(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CONFIG.irrigationTweaker.enableIrrigationTweaker) return;

        BlockPos.Mutable m = new BlockPos.Mutable();

        for (int dy = RANGE_Y_MIN; dy <= RANGE_Y_MAX; dy++) {
            for (int dx = -RANGE_XZ; dx <= RANGE_XZ; dx++) {
                for (int dz = -RANGE_XZ; dz <= RANGE_XZ; dz++) {
                    m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState state = world.getBlockState(m);
                    Identifier id = Registries.BLOCK.getId(state.getBlock());

                    if (world.getFluidState(m).isIn(FluidTags.WATER) || EXTRA_BLOCK_IDS.contains(id)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }

        cir.setReturnValue(false);
    }

    @Inject(method = "onLandedUpon", at = @At("HEAD"), cancellable = true)
    private void farmland_tweaker$modifyLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
        if (!CONFIG.trampleTweaker.enableTrampleTweaker) return;

        if (world instanceof ServerWorld) {
            float chanceValue = (float) ((fallDistance - MIN_FALL_DISTANCE) / CHANCE_RANGE);
            float entityVolume = entity.getWidth() * entity.getWidth() * entity.getHeight();

            boolean passedTrampleChance = world.random.nextFloat() < chanceValue;
            boolean isLivingEntityRequirementMet = REQUIRE_LIVING_ENTITY_TO_TRAMPLE ? entity instanceof LivingEntity : true;
            boolean isTrampleAllowed = entity instanceof PlayerEntity ? ALLOW_PLAYER_TRAMPLE : ALLOW_MOB_TRAMPLE;
            boolean isLargeEnoughToTrample  = entityVolume > TRAMPLE_VOLUME_THRESHOLD;

            if (passedTrampleChance && isLivingEntityRequirementMet && isTrampleAllowed && isLargeEnoughToTrample) {
                FarmlandBlock.setToDirt(entity, state, world, pos);
            }
        }

        super.onLandedUpon(world, state, pos, entity, fallDistance);
        ci.cancel();
    }
}
