package com.evandev.tiny_takeover_backport.client;

import com.evandev.tiny_takeover_backport.client.model.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.Minecraft;

public class ModRenderHelper {
    public static final ThreadLocal<Boolean> SUPPRESS_AGE_SCALE = ThreadLocal.withInitial(() -> false);
    public static EntityModel<?> babyWoolModel;

    private static HumanoidModel<?> babyZombieInnerModel;
    private static HumanoidModel<?> babyZombieOuterModel;
    private static HumanoidModel<?> babyPiglinInnerModel;
    private static HumanoidModel<?> babyPiglinOuterModel;
    private static HumanoidModel<?> smallStandInnerModel;
    private static HumanoidModel<?> smallStandOuterModel;
    private static EntityModelSet lastModelSet;

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity, A extends HumanoidModel<T>> A getBabyArmorModel(A originalModel, EntityModel<?> parentModel, EquipmentSlot slot) {
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        if (modelSet != lastModelSet) {
            lastModelSet = modelSet;
            babyZombieInnerModel = null;
            babyZombieOuterModel = null;
            babyPiglinInnerModel = null;
            babyPiglinOuterModel = null;
            smallStandInnerModel = null;
            smallStandOuterModel = null;
        }

        if (parentModel instanceof ArmorStandModel && parentModel.young) {
            if (smallStandInnerModel == null) {
                smallStandInnerModel = new SmallArmorStandArmorModel(modelSet.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR));
                smallStandOuterModel = new SmallArmorStandArmorModel(modelSet.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR));
            }
            return (A) (slot == EquipmentSlot.LEGS ? smallStandInnerModel : smallStandOuterModel);
        } else if (parentModel instanceof BabyZombieModel || parentModel instanceof BabyDrownedModel || parentModel instanceof BabyZombieVillagerModel) {
            boolean isInner = (slot == EquipmentSlot.LEGS);
            if (babyZombieInnerModel == null) {
                babyZombieInnerModel = new BabyZombieModel<>(modelSet.bakeLayer(ModModelLayers.ZOMBIE_BABY_INNER_ARMOR));
                babyZombieOuterModel = new BabyZombieModel<>(modelSet.bakeLayer(ModModelLayers.ZOMBIE_BABY_OUTER_ARMOR));
            }
            return (A) (isInner ? babyZombieInnerModel : babyZombieOuterModel);
        } else if (parentModel instanceof BabyPiglinModel || parentModel instanceof BabyZombifiedPiglinModel) {
            boolean isInner = (slot == EquipmentSlot.LEGS);
            if (babyPiglinInnerModel == null) {
                babyPiglinInnerModel = new BabyPiglinModel(modelSet.bakeLayer(ModModelLayers.PIGLIN_BABY_INNER_ARMOR));
                babyPiglinOuterModel = new BabyPiglinModel(modelSet.bakeLayer(ModModelLayers.PIGLIN_BABY_OUTER_ARMOR));
            }
            return (A) (isInner ? babyPiglinInnerModel : babyPiglinOuterModel);
        }
        return originalModel;
    }

    public static void adjustBabyArmorVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
        if (model instanceof ModBabyArmorModel babyArmor) {
            ModelPart waist = babyArmor.getWaist();
            ModelPart bodyBase = babyArmor.getBodyBase();
            ModelPart rightLegBase = babyArmor.getRightLegBase();
            ModelPart leftLegBase = babyArmor.getLeftLegBase();
            ModelPart rightFoot = babyArmor.getRightFoot();
            ModelPart leftFoot = babyArmor.getLeftFoot();

            if (waist != null) waist.visible = (slot == EquipmentSlot.LEGS);
            if (bodyBase != null) bodyBase.visible = (slot == EquipmentSlot.CHEST);
            if (rightLegBase != null) rightLegBase.visible = (slot == EquipmentSlot.LEGS);
            if (leftLegBase != null) leftLegBase.visible = (slot == EquipmentSlot.LEGS);
            if (rightFoot != null) rightFoot.visible = (slot == EquipmentSlot.FEET);
            if (leftFoot != null) leftFoot.visible = (slot == EquipmentSlot.FEET);
        }
    }
}
