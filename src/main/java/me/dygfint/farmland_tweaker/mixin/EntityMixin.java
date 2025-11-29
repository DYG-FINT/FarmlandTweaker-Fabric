package me.dygfint.farmland_tweaker.mixin;

import me.dygfint.farmland_tweaker.access.EntityMixinAccess;
import me.dygfint.farmland_tweaker.access.TrampleTweakerMixinAccess;
import me.dygfint.farmland_tweaker.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements EntityMixinAccess {
    @Unique
    private Vec3d lastVelocity;
    @Unique
    private boolean lastOnGround;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void farmland_tweaker$takeVelocity(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        lastVelocity = self.getVelocity();
        lastOnGround = self.isOnGround();
    }

    @Inject(method = "fall", at = @At(value = "HEAD"), cancellable = true)
    private void farmland_tweaker$onGlidingLand(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;

        ModConfig.TrampleTweaker config = ModConfig.get().trampleTweaker;
        if (!config.enableTrampleTweaker) return;

        if (!lastOnGround && onGround && state.getBlock() instanceof TrampleTweakerMixinAccess farmlandBlock) {
            World world = self.world;

            if (self instanceof LivingEntity living && living.isFallFlying()) {
                state = farmlandBlock.farmland_tweaker$setGlidingCollision(state, true);
            }

            //? if >= 1.17 {
            state.getBlock().onLandedUpon(world, state, landedPosition, self, self.fallDistance);
             //?} else {
            /*state.getBlock().onLandedUpon(world, landedPosition, self, self.fallDistance);
            *///?}

            //?if >= 1.17 {
            if (!state.isIn(net.minecraft.tag.BlockTags.OCCLUDES_VIBRATION_SIGNALS)) self.emitGameEvent(net.minecraft.world.event.GameEvent.HIT_GROUND);
            //?}

            farmlandBlock.farmland_tweaker$setGlidingCollision(state, true);

            self.fallDistance = 0.0F;
            ci.cancel();
        }
    }

    @Override
    public Vec3d farmland_tweaker$getLastVelocity() {
        return lastVelocity;
    }
}
