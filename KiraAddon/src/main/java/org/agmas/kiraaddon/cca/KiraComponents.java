package org.agmas.kiraaddon.cca;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.kiraaddon.KiraAddon;
import org.agmas.kiraaddon.game.roles.vigilante.josuke.JosukePlayerComponent;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class KiraComponents implements EntityComponentInitializer {
    public static final ComponentKey<KiraPlayerComponent> KIRA_PLAYER_KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "kira_player"), KiraPlayerComponent.class);
    
    public static final ComponentKey<JosukePlayerComponent> JOSUKE_PLAYER_KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "josuke_player"), JosukePlayerComponent.class);

    public static KiraPlayerComponent getKiraComponent(Player player) {
        return player == null ? null : KIRA_PLAYER_KEY.get(player);
    }

    public static JosukePlayerComponent getJosukeComponent(Player player) {
        return player == null ? null : JOSUKE_PLAYER_KEY.get(player);
    }

    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(Player.class, KIRA_PLAYER_KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(KiraPlayerComponent::new);
        
        registry.beginRegistration(Player.class, JOSUKE_PLAYER_KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(JosukePlayerComponent::new);
    }
}