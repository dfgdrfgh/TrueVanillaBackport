package com.evandev.tiny_takeover_backport.client.model;

import com.evandev.tiny_takeover_backport.client.animation.BabyAxolotlAnimation;
import com.evandev.tiny_takeover_backport.client.animation.PartAnimator;
import com.evandev.tiny_takeover_backport.entity.AxolotlAnimationStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class BabyAxolotlModel extends AxolotlModel {
    private final ModelPart root;
    private final Map<String, ModelPart> animatedParts = new HashMap<>();

    public BabyAxolotlModel(ModelPart root) {
        super(root.getChild("root"));
        this.root = root;
        ModelPart animatedRoot = root.getChild("root");
        ModelPart body = animatedRoot.getChild("body");
        ModelPart head = body.getChild("head");
        this.animatedParts.put("root", animatedRoot);
        this.animatedParts.put("body", body);
        for (String name : new String[]{"head", "tail", "left_front_leg", "right_front_leg", "left_hind_leg", "right_hind_leg"}) {
            this.animatedParts.put(name, body.getChild(name));
        }
        for (String name : new String[]{"left_gills", "right_gills", "top_gills"}) {
            this.animatedParts.put(name, head.getChild(name));
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -0.75f, -2.75f, 4.0f, 2.0f, 6.0f, new CubeDeformation(0.0f)).texOffs(0, 12).addBox(0.0f, -1.75f, -2.75f, 0.0f, 3.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -1.25f, 1.75f));
        body.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(20, 16).addBox(-3.0f, 0.0f, -0.5f, 3.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset(-2.0f, 0.25f, -1.25f));
        PartDefinition right_leg = body.addOrReplaceChild("right_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0f, 0.25f, 1.75f, 0.0f, 1.5708f, 1.5708f));
        right_leg.addOrReplaceChild("right_leg_r1", CubeListBuilder.create().texOffs(20, 14).addBox(0.0f, 0.0f, -0.5f, 3.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -1.5708f, 0.0f, 1.5708f));
        body.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(20, 13).addBox(0.0f, 0.0f, -0.5f, 3.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset(2.0f, 0.25f, -1.25f));
        body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(20, 14).addBox(0.0f, 0.0f, -0.5f, 3.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset(2.0f, 0.25f, 1.75f));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(10, 9).addBox(0.0f, -1.5f, -1.0f, 0.0f, 3.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -0.25f, 3.25f));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0f, -2.0f, -4.0f, 6.0f, 3.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 0.25f, -2.75f));
        head.addOrReplaceChild("left_gills", CubeListBuilder.create().texOffs(20, 8).addBox(0.0f, -3.5f, 0.0f, 3.0f, 5.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset(3.0f, -0.5f, -2.0f));
        head.addOrReplaceChild("right_gills", CubeListBuilder.create().texOffs(20, 3).addBox(-3.0f, -3.5f, 0.0f, 3.0f, 5.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset(-3.0f, -0.5f, -2.0f));
        head.addOrReplaceChild("top_gills", CubeListBuilder.create().texOffs(20, 0).addBox(-3.0f, -3.0f, 0.0f, 6.0f, 3.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -2.0f, -2.0f));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(@NotNull Axolotl entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        AnimationState[] states = ((AxolotlAnimationStates) entity).tiny_takeover_backport$getAxolotlAnimations();
        if (states[AxolotlAnimationStates.WALK].isStarted()) {
            PartAnimator.applyWalk(this.animatedParts, BabyAxolotlAnimation.AXOLOTL_WALK_FLOOR, limbSwing, limbSwingAmount, 15.0F, 30.0F);
        }
        PartAnimator.animate(this.animatedParts, states[AxolotlAnimationStates.SWIM], BabyAxolotlAnimation.BABY_AXOLOTL_SWIM, ageInTicks);
        // Vanilla 26.1 applies this channel with the ground-walk state as well.
        PartAnimator.animate(this.animatedParts, states[AxolotlAnimationStates.WALK], BabyAxolotlAnimation.WALK_FLOOR_UNDERWATER, ageInTicks);
        PartAnimator.animate(this.animatedParts, states[AxolotlAnimationStates.IDLE_GROUND], BabyAxolotlAnimation.BABY_AXOLOTL_IDLE_FLOOR, ageInTicks);
        PartAnimator.animate(this.animatedParts, states[AxolotlAnimationStates.IDLE_WATER], BabyAxolotlAnimation.IDLE_UNDERWATER, ageInTicks);
        PartAnimator.animate(this.animatedParts, states[AxolotlAnimationStates.IDLE_WATER_GROUND], BabyAxolotlAnimation.IDLE_FLOOR_UNDERWATER, ageInTicks);
        PartAnimator.animate(this.animatedParts, states[AxolotlAnimationStates.PLAY_DEAD], BabyAxolotlAnimation.BABY_AXOLOTL_PLAY_DEAD, ageInTicks);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
