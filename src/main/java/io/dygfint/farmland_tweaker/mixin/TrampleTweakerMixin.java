package io.dygfint.farmland_tweaker.mixin;

import io.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class TrampleTweakerMixin extends Block {
    @Unique private static final ModConfig CONFIG = ModConfig.get();

    @Unique private static final float MIN_FALL_DISTANCE = (float) CONFIG.trampleTweaker.minTrampleFallHeight;
    @Unique private static final float CHANCE_RANGE = (float) CONFIG.trampleTweaker.trampleFallRange;
    @Unique private static final boolean REQUIRE_LIVING_ENTITY_TO_TRAMPLE = CONFIG.trampleTweaker.requireLivingEntityToTrample;
    @Unique private static final boolean ALLOW_PLAYER_TRAMPLE = CONFIG.trampleTweaker.allowPlayerTrample;
    @Unique private static final boolean ALLOW_MOB_TRAMPLE = CONFIG.trampleTweaker.allowMobTrample;
    @Unique private static final float TRAMPLE_VOLUME_THRESHOLD = (float) CONFIG.trampleTweaker.trampleVolumeThreshold;

    public TrampleTweakerMixin(Settings settings) {
        super(settings);
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
