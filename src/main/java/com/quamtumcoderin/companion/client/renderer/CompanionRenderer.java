package com.quamtumcoderin.companion.client.renderer;

import com.quamtumcoderin.companion.entity.CompanionEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class CompanionRenderer extends BipedEntityRenderer<CompanionEntity, BipedEntityModel<CompanionEntity>> {

    private static final Identifier TEXTURE = new Identifier("companionmod", "textures/entity/companion_texture.png");

    public CompanionRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher, new PlayerEntityModel<>(0.0F, false), 0.5F);
    }

    @Override
    public Identifier getTexture(CompanionEntity entity) {
        return TEXTURE;
    }
}
