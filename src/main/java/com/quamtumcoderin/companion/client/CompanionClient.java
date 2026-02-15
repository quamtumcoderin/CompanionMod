package com.quamtumcoderin.companion.client;

import com.quamtumcoderin.companion.client.renderer.CompanionRenderer;
import com.quamtumcoderin.companion.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class CompanionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(
                ModEntities.COMPANION,
                (dispatcher, context) -> new CompanionRenderer(dispatcher)
        );
    }
}
