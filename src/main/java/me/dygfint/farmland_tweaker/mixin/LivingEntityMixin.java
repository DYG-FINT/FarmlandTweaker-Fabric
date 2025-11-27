package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.access.TrampleTweakerMixinAccess;
import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private boolean wasOnGround = false;
    @Unique
    private Vec3d glideVelocity;

    @Inject(method = "travel", at = @At(value = "INVOKE",
            //? if >= 1.21.5 {
            target = "Lnet/minecraft/entity/LivingEntity;travelGliding(Lnet/minecraft/util/math/Vec3d;)V"
            //?} else {
            /*target = "Lnet/minecraft/entity/LivingEntity;travelGliding()V"
            *///?}
    ))
    private void farmland_tweaker$captureVelocity(Vec3d movementInput, CallbackInfo ci) {
        ModConfig.TrampleTweaker config = ModConfig.get().trampleTweaker;
        if (!(config.enableTrampleTweaker && config.allowGlidingCollisionTrample)) return;

        LivingEntity self = (LivingEntity)(Object)this;
        glideVelocity = self.getVelocity();
    }

    @Inject(method = "travel", at = @At(value = "INVOKE",
            //? if >= 1.21.5 {
            target = "Lnet/minecraft/entity/LivingEntity;travelGliding(Lnet/minecraft/util/math/Vec3d;)V",
            //?} else {
            /*target = "Lnet/minecraft/entity/LivingEntity;travelGliding()V",
            *///?}
            shift = At.Shift.AFTER
    ))
    private void farmland_tweaker$onGlidingLand(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        World world = self.getWorld();
        if (world.isClient()) return;

        boolean onGroundNow = self.isOnGround();

        if (!wasOnGround && onGroundNow && self.isGliding()) {
            double vx = glideVelocity.x;
            double vy = glideVelocity.y;
            double vz = glideVelocity.z;

            double speedSq = vx * vx + vy * vy + vz * vz;
            double fallDistance = speedSq / 0.16;

            if (fallDistance > 0.0) {
                BlockPos pos = self.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.getBlock() instanceof FarmlandBlock) {
                    ((ServerWorld)world).getServer().execute(() -> {
                        BlockState newState = world.getBlockState(pos);
                        Block block = newState.getBlock();
                        if (block instanceof TrampleTweakerMixinAccess farmlandBlock) {
                            farmlandBlock.farmland_tweaker$setGlidingCollision(true);

                            //? if >= 1.21.5 {
                            block.onLandedUpon(world, newState, pos, self, fallDistance);
                            //?} else {
                            /*block.onLandedUpon(world, newState, pos, self, (float) fallDistance);
                             *///?}

                            farmlandBlock.farmland_tweaker$setGlidingCollision(false);
                        }
                    });
                }
            }
        }

        wasOnGround = onGroundNow;
    }
}
