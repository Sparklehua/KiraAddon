package org.agmas.kiraaddon.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.agmas.kiraaddon.init.ModRoles;
import org.agmas.kiraaddon.input.KeyBindings;
import org.agmas.kiraaddon.network.JosukeSkillC2SPacket;
import org.agmas.kiraaddon.network.RecallSheerHeartPacket;
import io.wifi.starrailexpress.cca.SREGameWorldComponent;

import java.util.UUID;

public class KeyInputHandler {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindings.RECALL_SHEER_HEART.consumeClick()) {
                if (client.player != null) {
                    if (client.player.isCrouching()) {
                        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new RecallSheerHeartPacket());
                    } else {
                        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                                new JosukeSkillC2SPacket(new UUID(0, 0)));
                    }
                }
            }
        });
    }
}