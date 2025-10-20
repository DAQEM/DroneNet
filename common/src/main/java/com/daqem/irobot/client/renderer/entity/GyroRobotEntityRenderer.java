package com.daqem.irobot.client.renderer.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.client.model.entity.GyroRobotModel;
import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.GyroRobotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;
import software.bernie.geckolib.renderer.layer.ItemInHandGeoLayer;

import java.util.List;

public class GyroRobotEntityRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<GyroRobotEntity, R> {

    public GyroRobotEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new GyroRobotModel());

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

        addRenderLayer(new GeoRenderLayer<>(this) {
            private static final ResourceLocation EYES_TEXTURE = IRobot.getId("textures/entity/gyro_robot_eyes.png");

            @Override
            public void render(R renderState, PoseStack poseStack, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, int packedOverlay, int renderColor) {
                RenderType overlayRenderType = RenderType.entityCutoutNoCull(EYES_TEXTURE);
                VertexConsumer overlayBuffer = bufferSource.getBuffer(overlayRenderType);
                getRenderer().reRender(renderState, poseStack, bakedModel, bufferSource, overlayRenderType, overlayBuffer, packedLight, packedOverlay, 0xFFFFFFFF);
            }
        });

        addRenderLayer(new GeoRenderLayer<>(this) {
            private static final ResourceLocation OVERLAY_TEXTURE = IRobot.getId("textures/entity/gyro_robot_overlay.png");

            @Override
            public void render(R renderState, PoseStack poseStack, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, int packedOverlay, int renderColor) {
                int overlayColor = renderState.getGeckolibData(IRobotEntity.OVERLAY_COLOR_TICKET);
                if (overlayColor == -1) {
                    overlayColor = 0xFFFFFF;
                }
                RenderType overlayRenderType = RenderType.entityCutoutNoCull(OVERLAY_TEXTURE);
                VertexConsumer overlayBuffer = bufferSource.getBuffer(overlayRenderType);
                getRenderer().reRender(renderState, poseStack, bakedModel, bufferSource, overlayRenderType, overlayBuffer, packedLight, packedOverlay, overlayColor | 0xFF000000);
            }
        });
    }

    @Override
    public void addRenderData(GyroRobotEntity animatable, Void relatedObject, R renderState) {
        super.addRenderData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(IRobotEntity.OVERLAY_COLOR_TICKET, animatable.getOverlayColor());
    }

    @Override
    public int getRenderColor(GyroRobotEntity animatable, Void relatedObject, float partialTick) {
        return animatable.getColor() | 0xFF000000;
    }
}