package org.agmas.kiraaddon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.kiraaddon.events.KiraEvents;
import org.agmas.kiraaddon.game.roles.killer.kira.KiraShopHandler;
import org.agmas.kiraaddon.init.ModEntities;
import org.agmas.kiraaddon.init.ModItems;
import org.agmas.kiraaddon.init.ModRoles;
import org.agmas.kiraaddon.init.ModSounds;
import org.agmas.kiraaddon.network.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraAddon implements ModInitializer {
    public static final String MOD_ID = "kiraaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Kira Yoshikage Addon...");

        // 注册物品
        ModItems.init();
        
        // 注册实体
        ModEntities.init();

        // 注册音效
        ModSounds.init();

        // 注册角色
        ModRoles.init();

        // 注册网络处理器
        PacketHandler.register();

        // 注册事件监听器
        KiraEvents.registerEvents();

        // 维持吉良吉影和东方杖助的成对生成规则：12人以上开放吉良吉影，
        // 东方杖助由角色绑定系统随吉良吉影自动加入。
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) {
                return;
            }
            int players = server.getPlayerList().getPlayerCount();
            int maximum = players >= 12 ? 1 : 0;
            Harpymodloader.setRoleMaximum(ModRoles.KIRA_ID, maximum);
        });

        // 使用 SERVER_STARTED 事件（在服务器完全启动后执行）
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            KiraShopHandler.init();
            LOGGER.info("Kira Yoshikage shop initialized!");
        });

        LOGGER.info("Kira Yoshikage Addon loaded successfully!");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}