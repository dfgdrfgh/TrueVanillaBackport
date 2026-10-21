package com.evandev.tiny_takeover_backport.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class PartAnimator {
    private static final Vector3f CACHE = new Vector3f();

    public static void animate(Map<String, ModelPart> parts, AnimationState state, AnimationDefinition definition, float ageInTicks) {
        state.updateTime(ageInTicks, 1.0F);
        state.ifStarted(s -> apply(parts, definition, s.getAccumulatedTime()));
    }

    public static void apply(Map<String, ModelPart> parts, AnimationDefinition definition, long accumulatedTime) {
        apply(parts, definition, accumulatedTime, 1.0F);
    }

    public static void applyWalk(Map<String, ModelPart> parts, AnimationDefinition definition, float position, float speed, float maxSpeed, float scale) {
        apply(parts, definition, (long)(position * 50.0F * maxSpeed), Math.min(speed * scale, 1.0F));
    }

    private static void apply(Map<String, ModelPart> parts, AnimationDefinition definition, long accumulatedTime, float weight) {
        float seconds = accumulatedTime / 1000.0F;
        float elapsed = definition.looping() ? seconds % definition.lengthInSeconds() : seconds;

        for (Map.Entry<String, List<AnimationChannel>> entry : definition.boneAnimations().entrySet()) {
            ModelPart part = parts.get(entry.getKey());
            if (part == null) continue;
            for (AnimationChannel channel : entry.getValue()) {
                Keyframe[] keyframes = channel.keyframes();
                int current = Math.max(0, Mth.binarySearch(0, keyframes.length, i -> elapsed <= keyframes[i].timestamp()) - 1);
                int next = Math.min(keyframes.length - 1, current + 1);
                Keyframe from = keyframes[current];
                Keyframe to = keyframes[next];
                float alpha = next != current ? Mth.clamp((elapsed - from.timestamp()) / (to.timestamp() - from.timestamp()), 0.0F, 1.0F) : 0.0F;
                to.interpolation().apply(CACHE, alpha, keyframes, current, next, weight);
                channel.target().apply(part, CACHE);
            }
        }
    }
}
