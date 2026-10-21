package com.evandev.tiny_takeover_backport.client.model;

import com.evandev.tiny_takeover_backport.client.animation.BabyRabbitAnimation;
import com.evandev.tiny_takeover_backport.client.animation.PartAnimator;
import com.evandev.tiny_takeover_backport.config.ModConfig;
import com.evandev.tiny_takeover_backport.entity.RabbitAnimationStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.Rabbit;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BabyRabbitModel<T extends Rabbit> extends RabbitModel<T> {

    private final ModelPart root;
    private final ModelPart realHead;
    private final Map<String, ModelPart> animatedParts = new HashMap<>();

    public BabyRabbitModel(ModelPart root) {
        super(root);
        this.root = root;
        ModelPart body = root.getChild("body");
        this.realHead = body.getChild("head");
        ModelPart frontlegs = body.getChild("frontlegs");
        ModelPart backlegs = root.getChild("backlegs");

        this.animatedParts.put("body", body);
        this.animatedParts.put("head", this.realHead);
        this.animatedParts.put("left_ear", this.realHead.getChild("left_ear"));
        this.animatedParts.put("right_ear", this.realHead.getChild("right_ear"));
        this.animatedParts.put("tail", body.getChild("tail"));
        this.animatedParts.put("frontlegs", frontlegs);
        this.animatedParts.put("left_front_leg", frontlegs.getChild("left_front_leg"));
        this.animatedParts.put("right_front_leg", frontlegs.getChild("right_front_leg"));
        this.animatedParts.put("backlegs", backlegs);
        this.animatedParts.put("left_hind_leg", backlegs.getChild("left_hind_leg"));
        this.animatedParts.put("right_hind_leg", backlegs.getChild("right_hind_leg"));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("left_hind_foot", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_hind_foot", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_haunch", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_haunch", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("nose", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 1.6F));
        body.addOrReplaceChild(
                "body_r1",
                CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, -1.6F, -0.5236F, 0.0F, 0.0F)
        );
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -2.2F, 2.0F));
        tail.addOrReplaceChild(
                "tail_r1",
                CubeListBuilder.create().texOffs(0, 21).addBox(-1.4F, -2.0268F, -1.0177F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F)
        );
        PartDefinition head = body.addOrReplaceChild(
                "head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -3.0F, 5.0F, 4.0F, 4.0F), PartPose.offset(0.0F, -5.0F, -2.6F)
        );
        head.addOrReplaceChild(
                "right_ear", CubeListBuilder.create().texOffs(18, 0).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offset(-1.5F, -3.5F, -0.5F)
        );
        head.addOrReplaceChild(
                "left_ear", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offset(1.5F, -3.5F, -0.5F)
        );
        PartDefinition frontLegs = body.addOrReplaceChild("frontlegs", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, -2.6F));
        PartDefinition leftFrontLeg = frontLegs.addOrReplaceChild(
                "left_front_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 1.0F, -0.5F, 0.3927F, 0.0F, 0.0F)
        );
        leftFrontLeg.addOrReplaceChild(
                "left_front_leg_r1",
                CubeListBuilder.create().texOffs(18, 8).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F)
        );
        PartDefinition rightFrontLeg = frontLegs.addOrReplaceChild(
                "right_front_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 1.0F, -0.5F, 0.3927F, 0.0F, 0.0F)
        );
        rightFrontLeg.addOrReplaceChild(
                "right_front_leg_r1",
                CubeListBuilder.create().texOffs(14, 8).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F)
        );
        PartDefinition backLegs = root.addOrReplaceChild("backlegs", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 2.0F));
        PartDefinition leftBackLeg = backLegs.addOrReplaceChild(
                "left_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(1.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F)
        );
        leftBackLeg.addOrReplaceChild(
                "left_haunch",
                CubeListBuilder.create().texOffs(10, 17).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(1.0F, 0.0F, 0.5F, 0.0F, -0.7854F, 0.0F)
        );
        PartDefinition rightBackLeg = backLegs.addOrReplaceChild(
                "right_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F)
        );
        rightBackLeg.addOrReplaceChild(
                "right_haunch",
                CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.5F, 0.0F, -0.9F, 0.0F, 0.7854F, 0.0F)
        );
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        RabbitAnimationStates states = (RabbitAnimationStates) entity;
        if (!states.tiny_takeover_backport$getIdleHeadTiltAnimationState().isStarted()) {
            this.realHead.yRot = netHeadYaw * ((float) Math.PI / 180F);
            this.realHead.xRot = headPitch * ((float) Math.PI / 180F);
        }
        PartAnimator.animate(this.animatedParts, states.tiny_takeover_backport$getHopAnimationState(), BabyRabbitAnimation.HOP, ageInTicks);
        PartAnimator.animate(this.animatedParts, states.tiny_takeover_backport$getIdleHeadTiltAnimationState(), BabyRabbitAnimation.IDLE_HEAD_TILT, ageInTicks);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (ModConfig.get().rabbitBoundingBox) {
            this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
        } else {
            poseStack.pushPose();
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.translate(0.0F, 1.0F, 0.0F);
            this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }
}
