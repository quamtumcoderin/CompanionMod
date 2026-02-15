package com.quamtumcoderin.companion.event;

import com.quamtumcoderin.companion.entity.CompanionEntity;
import com.quamtumcoderin.companion.registry.ModEntities;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class ModEvents {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;

            if (!player.getScoreboardTags().contains("companion.spawned")) {
                spawnCompanionForPlayer(player);
                player.addScoreboardTag("companion.spawned");
            }
        });
    }

    private static void spawnCompanionForPlayer(ServerPlayerEntity player) {
        World world = player.world;
        CompanionEntity companion = ModEntities.COMPANION.create(world);

        if (companion != null) {
            companion.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0, 0);
            companion.setOwner(player);
            companion.setTamed(true);
            companion.setCustomName(player.getName());
            world.spawnEntity(companion);
        }
    }
}
