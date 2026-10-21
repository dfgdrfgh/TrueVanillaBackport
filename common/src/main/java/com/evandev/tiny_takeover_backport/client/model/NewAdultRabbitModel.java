package com.evandev.tiny_takeover_backport.client.model;

import com.evandev.tiny_takeover_backport.client.animation.PartAnimator;
import com.evandev.tiny_takeover_backport.client.animation.RabbitAnimation;
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

public class NewAdultRabbitModel<T extends Rabbit> extends RabbitModel<T> {

    private final ModelPart root;
    private final ModelPart realHead;
    private final Map<String, ModelPart> animatedParts = new HashMap<>();

    public NewAdultRabbitModel(ModelPart root) {
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

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -9.0F, 8.0F, 6.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 23.0F, 4.0F, -0.3927F, 0.0F, 0.0F)
        );
        body.addOrReplaceChild(
                "tail", CubeListBuilder.create().texOffs(20, 16).addBox(-2.0F, -3.0084F, -1.0125F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, -4.9916F, 0.0125F)
        );
        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.5F, -3.0F, -4.0F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -5.2929F, -8.1213F, 0.3927F, 0.0F, 0.0F)
        );
        head.addOrReplaceChild(
                "left_ear", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -4.2929F, -0.1213F, 2.0F, 5.0F, 1.0F), PartPose.offset(1.5F, -3.7071F, -0.8787F)
        );
        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, -4.2929F, -0.1213F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(-1.5F, -3.7071F, -0.8787F)
        );
        PartDefinition frontLegs = body.addOrReplaceChild("frontlegs", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5349F, -6.3108F));
        frontLegs.addOrReplaceChild(
                "right_front_leg",
                CubeListBuilder.create().texOffs(36, 18).addBox(-0.9F, -1.0F, -0.9F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 1.9239F, 0.3827F, 0.3927F, 0.0F, 0.0F)
        );
        frontLegs.addOrReplaceChild(
                "left_front_leg",
                CubeListBuilder.create().texOffs(44, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(2.0F, 1.9239F, 0.4827F, 0.3927F, 0.0F, 0.0F)
        );
        PartDefinition backLegs = root.addOrReplaceChild("backlegs", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 4.0F));
        PartDefinition rightBackLeg = backLegs.addOrReplaceChild("right_hind_leg", CubeListBuilder.create(), PartPose.offset(-3.0F, 0.5F, 0.0F));
        rightBackLeg.addOrReplaceChild(
                "right_haunch",
                CubeListBuilder.create().texOffs(20, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 0.3927F, 0.0F)
        );
        PartDefinition leftBackLeg = backLegs.addOrReplaceChild("left_hind_leg", CubeListBuilder.create(), PartPose.offset(3.0F, 0.5F, 0.0F));
        leftBackLeg.addOrReplaceChild(
                "left_haunch",
                CubeListBuilder.create().texOffs(36, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, -0.3927F, 0.0F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        RabbitAnimationStates states = (RabbitAnimationStates) entity;
        if (!states.tiny_takeover_backport$getIdleHeadTiltAnimationState().isStarted()) {
            this.realHead.yRot = netHeadYaw * ((float) Math.PI / 180F);
            this.realHead.xRot = headPitch * ((float) Math.PI / 180F);
        }
        PartAnimator.animate(this.animatedParts, states.tiny_takeover_backport$getHopAnimationState(), RabbitAnimation.HOP, ageInTicks);
        PartAnimator.animate(this.animatedParts, states.tiny_takeover_backport$getIdleHeadTiltAnimationState(), RabbitAnimation.IDLE_HEAD_TILT, ageInTicks);
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
