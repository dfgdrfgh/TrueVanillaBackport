package com.evandev.tiny_takeover_backport.entity;

import net.minecraft.world.entity.AnimationState;

public interface AxolotlAnimationStates {
    int SWIM = 0;
    int WALK = 1;
    int WALK_WATER = 2;
    int IDLE_WATER = 3;
    int IDLE_WATER_GROUND = 4;
    int IDLE_GROUND = 5;
    int PLAY_DEAD = 6;

    AnimationState[] tiny_takeover_backport$getAxolotlAnimations();
}
