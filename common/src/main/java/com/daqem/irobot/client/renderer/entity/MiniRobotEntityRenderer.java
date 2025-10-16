package com.daqem.irobot.client.renderer.entity;

import com.daqem.irobot.client.model.entity.MiniRobotModel;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;
import software.bernie.geckolib.renderer.layer.ItemInHandGeoLayer;

import java.util.List;

public class MiniRobotEntityRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<MiniRobotEntity, R> {

    public MiniRobotEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new MiniRobotModel());

        addRenderLayer(new ItemInHandGeoLayer<>(this) {
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, ItemDisplayContext displayContext, R renderState, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.scale(0.75f, 0.75f, 0.75f);
                super.renderStackForBone(poseStack, bone, stack, displayContext, renderState, bufferSource, packedLight, packedOverlay);
                poseStack.popPose();
            }
        });
        addRenderLayer(new ItemArmorGeoLayer<>(this, context) {
            private final List<RenderData> BONES = List.of(RenderData.head("helmet"), RenderData.body("chestplate"),
                    RenderData.leftArm("chestplateLeft"), RenderData.rightArm("chestplateRight"),
                    RenderData.leftLeg("leggingsLeft"), RenderData.rightLeg("leggingsRight"),
                    RenderData.leftFoot("bootsLeft"), RenderData.rightFoot("bootsRight"));

            @Override
            protected List<RenderData> getRelevantBones(R renderState, BakedGeoModel model) {
                return BONES;
            }
        });
    }
}
