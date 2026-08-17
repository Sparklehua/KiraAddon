package org.agmas.kiraaddon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.agmas.kiraaddon.client.JosukeHudRenderer;
import org.agmas.kiraaddon.client.KiraHudRenderer;
import org.agmas.kiraaddon.client.KeyInputHandler;
import org.agmas.kiraaddon.content.entity.SheerHeartEntity;
import org.agmas.kiraaddon.content.entity.render.SheerHeartEntityModel;
import org.agmas.kiraaddon.content.entity.render.SheerHeartEntityRenderer;
import org.agmas.kiraaddon.init.ModEntities;
import org.agmas.kiraaddon.init.ModRoles;
import org.agmas.kiraaddon.input.KeyBindings;
import org.agmas.kiraaddon.network.JosukeSkillC2SPacket;
import org.agmas.kiraaddon.network.KiraC2SPacket;
import org.agmas.noellesroles.client.GKeyRoleSkill;
import io.wifi.starrailexpress.cca.SREGameWorldComponent;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class KiraAddonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(SheerHeartEntityModel.LAYER_LOCATION, SheerHeartEntityModel::createBodyLayer);
        
        EntityRendererRegistry.register(ModEntities.SHEER_HEART, SheerHeartEntityRenderer::new);
        
        KeyBindings.init();
        KeyInputHandler.init();
        
        KiraHudRenderer.init();
        JosukeHudRenderer.init();

        registerGKeySkills();
    }

    private static void registerGKeySkills() {
        GKeyRoleSkill.register(ModRoles.KIRA, true, (client, gameWorld) -> {
            ClientPlayNetworking.send(new KiraC2SPacket(new UUID(0, 0), KiraC2SPacket.ACTION_ANCHOR_MARK));
            return true;
        });

        GKeyRoleSkill.register(ModRoles.JOSUKE, true, (client, gameWorld) -> {
            UUID targetUuid = findLookedAtPlayer(client);
            ClientPlayNetworking.send(new JosukeSkillC2SPacket(targetUuid != null ? targetUuid : new UUID(0, 0)));
            return true;
        });
    }

    private static UUID findLookedAtPlayer(net.minecraft.client.Minecraft client) {
        if (client.player == null) {
            return null;
        }
        double range = 6.0;
        Vec3 eyePos = client.player.getEyePosition();
        Vec3 look = client.player.getLookAngle();
        Vec3 targetPos = eyePos.add(look.x * range, look.y * range, look.z * range);
        AABB boundingBox = client.player.getBoundingBox()
            .expandTowards(look.scale(range))
            .inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
            client.player.level(), client.player, eyePos, targetPos, boundingBox,
            entity -> entity instanceof Player && entity != client.player && entity.isAlive());
        if (hit != null && hit.getEntity() instanceof Player target) {
            return target.getUUID();
        }
        return null;
    }
}