package me.dygfint.farmland_tweaker.access;

import net.minecraft.block.BlockState;

public interface TrampleTweakerMixinAccess {
    boolean farmland_tweaker$isGlidingCollision(BlockState state);
    BlockState farmland_tweaker$setGlidingCollision(BlockState state, boolean value);
}
