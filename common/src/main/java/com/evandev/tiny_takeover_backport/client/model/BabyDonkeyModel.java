package com.evandev.tiny_takeover_backport.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ChestedHorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import org.jetbrains.annotations.NotNull;

public class BabyDonkeyModel<T extends AbstractChestedHorse> extends ChestedHorseModel<T> {

    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindBabyLeg;
    private final ModelPart leftHindBabyLeg;
    private final ModelPart rightFrontBabyLeg;
    private final ModelPart leftFrontBabyLeg;
    private final ModelPart leftChest;
    private final ModelPart rightChest;
    private final ModelPart tail;
    private final ModelPart actualHeadParts;
    private float partialTick;

    public BabyDonkeyModel(ModelPart root) {
        super(root);
        this.rightHindLeg = this.body.getChild("right_hind_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.rightHindBabyLeg = root.getChild("right_hind_baby_leg");
        this.leftHindBabyLeg = root.getChild("left_hind_baby_leg");
        this.rightFrontBabyLeg = root.getChild("right_front_baby_leg");
        this.leftFrontBabyLeg = root.getChild("left_front_baby_leg");
        this.leftChest = this.body.getChild("left_chest");
        this.rightChest = this.body.getChild("right_chest");
        this.tail = this.body.getChild("tail");
        this.actualHeadParts = this.body.getChild("head_parts");
    }

    public static LayerDefinition createBodyLayer() {
        return createBabyLayer();
    }

    public static LayerDefinition createBabyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition dummyHeadParts = partdefinition.addOrReplaceChild("head_parts", CubeListBuilder.create(), PartPose.ZERO);
        dummyHeadParts.addOrReplaceChild("left_saddle_mouth", CubeListBuilder.create(), PartPose.ZERO);
        dummyHeadParts.addOrReplaceChild("right_saddle_mouth", CubeListBuilder.create(), PartPose.ZERO);
        dummyHeadParts.addOrReplaceChild("left_saddle_line", CubeListBuilder.create(), PartPose.ZERO);
        dummyHeadParts.addOrReplaceChild("right_saddle_line", CubeListBuilder.create(), PartPose.ZERO);
        dummyHeadParts.addOrReplaceChild("head_saddle", CubeListBuilder.create(), PartPose.ZERO);
        dummyHeadParts.addOrReplaceChild("mouth_saddle_wrap", CubeListBuilder.create(), PartPose.ZERO);

        partdefinition.addOrReplaceChild("right_hind_baby_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_hind_baby_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_front_baby_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_front_baby_leg", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 13).addBox(-5.0F, -3.0F, -7.0F, 8.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, 14.0F, 0.0F)
        );

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 6.5F));
        tail.addOrReplaceChild(
                "tail_r1",
                CubeListBuilder.create().texOffs(24, 33).addBox(-2.5F, -1.0F, -0.5F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7418F, 0.0F, 0.0F)
        );

        body.addOrReplaceChild(
                "left_hind_leg",
                CubeListBuilder.create().texOffs(12, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.25F, 3.5F, 5.25F)
        );
        body.addOrReplaceChild(
                "right_hind_leg",
                CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.4F, 3.5F, 5.4F)
        );
        body.addOrReplaceChild(
                "left_front_leg",
                CubeListBuilder.create().texOffs(12, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.4F, 3.5F, -5.3F)
        );
        body.addOrReplaceChild(
                "right_front_leg",
                CubeListBuilder.create().texOffs(0, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.4F, 3.5F, -5.4F)
        );

        body.addOrReplaceChild("saddle", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("right_chest", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));
        body.addOrReplaceChild("left_chest", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("head_parts", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -5.0F));
        neck.addOrReplaceChild(
                "neck_r1",
                CubeListBuilder.create().texOffs(30, 9).addBox(-3.0F, -6.0F, -3.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F)
        );
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -3.0F));
        head.addOrReplaceChild(
                "head_r1",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.6F, -8.4F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.3927F, 0.0F, 0.0F)
        );
        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -3.5F, -1.0F, 0.48F, 0.0F, 0.48F)
        );
        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create().texOffs(22, 0).mirror().addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-2.0F, -3.5F, -1.0F, 0.48F, 0.0F, -0.48F)
        );

        neck.addOrReplaceChild("head_saddle", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("left_saddle_mouth", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("right_saddle_mouth", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("left_saddle_line", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("right_saddle_line", CubeListBuilder.create(), PartPose.ZERO);
        neck.addOrReplaceChild("mouth_saddle_wrap", CubeListBuilder.create(), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float clampedYRot = Mth.clamp(netHeadYaw, -20.0F, 20.0F);
        float headRotXRad = -30.0F * (float) (Math.PI / 180.0);

        float eating = entity.getEatAnim(this.partialTick);
        float standing = entity.getStandAnim(this.partialTick);
        float iStanding = 1.0F - standing;
        float mouthAnim = entity.getMouthAnim(this.partialTick);
        boolean animateTail = entity.tailCounter != 0;

        float baseHeadAngle = (1.0F - Math.max(standing, eating)) * ((float) (Math.PI / 6) + headRotXRad + mouthAnim * Mth.sin(ageInTicks) * 0.05F);
        this.actualHeadParts.xRot = standing * ((float) (Math.PI / 12) + headRotXRad) + eating * ((float) (Math.PI / 2) + Mth.sin(ageInTicks) * 0.05F) + baseHeadAngle;
        this.actualHeadParts.yRot = standing * clampedYRot * (float) (Math.PI / 180.0) + (1.0F - Math.max(standing, eating)) * clampedYRot * (float) (Math.PI / 180.0);

        this.actualHeadParts.y = Mth.lerp(eating, -3.0F, -1.2F);
        this.actualHeadParts.z = Mth.lerp(standing, -5.0F, -3.6F);

        this.body.y = 14.0F;
        this.body.xRot = standing * (float) (-Math.PI / 4);

        float waterMultiplier = entity.isInWater() ? 0.2F : 1.0F;
        float legAnim1 = Mth.cos(waterMultiplier * limbSwing * 0.6662F + (float) Math.PI);
        float legXRotAnim = legAnim1 * 0.8F * limbSwingAmount;

        float standAngle = ((float) (Math.PI / 3)) * standing;
        float bobValue = Mth.cos(ageInTicks * 0.6F + (float) Math.PI);
        float rlegRot = bobValue * standing + legXRotAnim * iStanding;
        float llegRot = -bobValue * standing - legXRotAnim * iStanding;

        this.leftHindLeg.xRot = standAngle - legAnim1 * 0.5F * limbSwingAmount * iStanding;
        this.rightHindLeg.xRot = standAngle + legAnim1 * 0.5F * limbSwingAmount * iStanding;
        this.leftFrontLeg.xRot = rlegRot;
        this.rightFrontLeg.xRot = llegRot;

        this.leftHindLeg.y = Mth.lerp(standing, 3.5F, -0.3F);
        this.rightHindLeg.y = Mth.lerp(standing, 3.5F, -0.3F);

        this.leftFrontLeg.y = 3.5F - standing;
        this.leftFrontLeg.z = -5.3F + 0.5F * standing;
        this.rightFrontLeg.y = 3.5F - standing;
        this.rightFrontLeg.z = -5.4F + 0.5F * standing;

        this.tail.xRot = (float) (-Math.PI / 4) + (float) (Math.PI / 6) + limbSwingAmount * 0.75F;
        this.tail.y = -1.5F + limbSwingAmount;
        this.tail.z = 6.5F + limbSwingAmount * 2.0F;
        if (animateTail) {
            this.tail.yRot = Mth.cos(ageInTicks * 0.7F);
        } else {
            this.tail.yRot = 0.0F;
        }

        if (entity.hasChest()) {
            this.leftChest.visible = true;
            this.rightChest.visible = true;
        } else {
            this.leftChest.visible = false;
            this.rightChest.visible = false;
        }
    }

    @Override
    public void prepareMobModel(@NotNull T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.partialTick = partialTick;

        this.rightHindLeg.visible = true;
        this.leftHindLeg.visible = true;
        this.rightFrontLeg.visible = true;
        this.leftFrontLeg.visible = true;
        this.rightHindBabyLeg.visible = false;
        this.leftHindBabyLeg.visible = false;
        this.rightFrontBabyLeg.visible = false;
        this.leftFrontBabyLeg.visible = false;
    }

    @Override
    public @NotNull Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected @NotNull Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
