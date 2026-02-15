package com.quamtumcoderin.companion;

import com.quamtumcoderin.companion.entity.CompanionEntity;
import com.quamtumcoderin.companion.event.ModEvents;
import com.quamtumcoderin.companion.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class CompanionMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ModEntities.register();

        FabricDefaultAttributeRegistry.register(ModEntities.COMPANION, CompanionEntity.createCompanionAttributes());

        ModEvents.register();

        System.out.println("CompanionMod Initialized!");
    }
}