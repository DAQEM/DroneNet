package com.daqem.irobot.client.gui.robot.components;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.uilib.gui.component.AbstractComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class EntityComponent extends AbstractComponent {

    private final Supplier<Integer> entityIdSupplier;

    public EntityComponent(int x, int y, int width, int height, Supplier<Integer> entityIdSupplier) {
        super(x, y, width, height);
        this.entityIdSupplier = entityIdSupplier;
    }

    public void renderEntity(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity entity = level.getEntity(this.entityIdSupplier.get());
        if (entity instanceof LivingEntity livingEntity) {
            int scale = livingEntity instanceof MiniRobotEntity ? 40 : 30;
            float yOffset = livingEntity instanceof MiniRobotEntity ? 0.25F : 0.0625F;
            float f = (getTotalX() + getTotalX() + getWidth()) / 2.0F;
            float g = (getTotalY() + getTotalY() + getHeight()) / 2.0F;
            guiGraphics.enableScissor(getTotalX(), getTotalY(), getTotalX() + getWidth(), getTotalY() + getHeight());
            float h = (float) Math.atan((f - mouseX) / 40.0F);
            float i = (float) Math.atan((g - mouseY) / 40.0F);
            Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf quaternionf2 = new Quaternionf().rotateX(i * 20.0F * (float) (Math.PI / 180.0));
            quaternionf.mul(quaternionf2);
            float j = livingEntity.yBodyRot;
            float k = livingEntity.getYRot();
            float l = livingEntity.getXRot();
            float m = livingEntity.yHeadRotO;
            float n = livingEntity.yHeadRot;
            livingEntity.yBodyRot = 180.0F + h * 20.0F;
            livingEntity.setYRot(180.0F + h * 40.0F);
            livingEntity.setXRot(-i * 20.0F);
            livingEntity.yHeadRot = livingEntity.getYRot();
            livingEntity.yHeadRotO = livingEntity.getYRot();
            float o = livingEntity.getScale();
            Vector3f vector3f = new Vector3f(0.0F, livingEntity.getBbHeight() / 2.0F + yOffset * o, 0.0F);
            float p = scale / o;
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(livingEntity);
            EntityRenderState entityRenderState = entityRenderer.createRenderState(livingEntity, 1.0F);
            entityRenderState.hitboxesRenderState = null;
            guiGraphics.submitEntityRenderState(entityRenderState, p, vector3f, quaternionf, quaternionf2, getTotalX(), getTotalY(), getTotalX() + getWidth(), getTotalY() + getHeight());
            livingEntity.yBodyRot = j;
            livingEntity.setYRot(k);
            livingEntity.setXRot(l);
            livingEntity.yHeadRotO = m;
            livingEntity.yHeadRot = n;
            guiGraphics.disableScissor();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth, int parentHeight) {
        renderEntity(guiGraphics, mouseX, mouseY);
    }
}