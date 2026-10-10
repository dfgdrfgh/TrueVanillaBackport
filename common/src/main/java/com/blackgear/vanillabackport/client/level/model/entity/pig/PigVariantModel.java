package com.blackgear.vanillabackport.client.level.model.entity.pig;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.animal.Pig;

@Environment(EnvType.CLIENT)
public class PigVariantModel<T extends Pig> extends PigModel<T> {
    public PigVariantModel(ModelPart root) {
        super(root);
    }

    /** Spring to Life's normal pig mesh uses a 64x64 texture atlas. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = QuadrupedModel.createBodyMesh(6, CubeDeformation.NONE);
        mesh.getRoot().addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(16, 16)
                .addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F),
            PartPose.offset(0.0F, 12.0F, -6.0F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }
}
