package com.quamtumcoderin.companion.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.quamtumcoderin.companion.entity.CompanionEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class CompanionRenderer extends BipedEntityRenderer<CompanionEntity, BipedEntityModel<CompanionEntity>> {

    private static final Identifier TEXTURE = new Identifier("companionmod", "textures/entity/companion_texture.png");

    public CompanionRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher, new PlayerEntityModel<>(0.0F, false), 0.5F);
    }

    @Override
    public Identifier getTexture(CompanionEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(CompanionEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        if (this.dispatcher.getSquaredDistanceToCamera(entity) < 64 * 64) {
            renderHealthAndHunger(entity, matrices);
        }
    }

    private void renderHealthAndHunger(CompanionEntity entity, MatrixStack matrices) {
        matrices.push();

        double height = entity.getHealth() + 0.5F;
        matrices.translate(0, height, 0);

        matrices.multiply(this.dispatcher.getRotation());

        float scale = 0.025F;
        matrices.scale(-scale, -scale, scale);

        float healthPerc = entity.getHealth() / entity.getMaxHealth();
        float hungerPerc = (float) entity.getHunger() / 20.0F;

        int barWidth = 40;
        int healthWidth = (int) (barWidth * healthPerc);
        int hungerWidth = (int) (barWidth * hungerPerc);

        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();

        drawRect(matrix, buffer, -10, 20, -6, 0xFF000000);
        drawRect(matrix, buffer, -10, -20 + healthWidth, -6, 0xFFFF0000);
        drawRect(matrix, buffer, -4, 20, 0, 0xFF000000);
        drawRect(matrix, buffer, -4, -20 + hungerWidth, 0, 0xFFFFAA00);

        RenderSystem.disableDepthTest();
        RenderSystem.enableTexture();

        matrices.pop();
    }

    private void drawRect(Matrix4f matrix, BufferBuilder buffer, int y1, int x2, int y2, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        buffer.begin(7, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, -20, y2, 0.0F).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, 0.0F).color(r, g, b, a).next();
        buffer.vertex(matrix, -20, y1, 0.0F).color(r, g, b, a).next();
        buffer.end();
    }
}
