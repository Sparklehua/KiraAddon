package org.agmas.kiraaddon.init;

import io.wifi.starrailexpress.api.NormalRole;
import io.wifi.starrailexpress.api.SRERole;
import io.wifi.starrailexpress.api.TMMRoles;
import io.wifi.starrailexpress.util.ShopEntry;
import org.agmas.kiraaddon.game.roles.killer.kira.KiraShopHandler;
import org.agmas.kiraaddon.game.roles.killer.kira.KiraBitesTheDustHandler;
import org.agmas.kiraaddon.game.roles.vigilante.josuke.JosukeSkillHandler;
import org.agmas.kiraaddon.game.roles.vigilante.josuke.JosukeFistPunchHandler;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.modded_murder.RoleAssignmentManager;
import org.agmas.kiraaddon.KiraAddon;
import org.agmas.kiraaddon.cca.KiraComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ModRoles {
    private static final Logger LOGGER = LoggerFactory.getLogger("KiraAddon-Roles");
    // KiraAddon 独立命名空间，禁止与 noellesroles 的同名角色发生注册覆盖。
    public static final ResourceLocation KIRA_ID = KiraAddon.id("kira");
    public static final ResourceLocation JOSUKE_ID = KiraAddon.id("josuke");
    // 仅用于客户端列表过滤；该角色由主模组注册，不能使用 KiraAddon 命名空间。
    public static final ResourceLocation LOST_KILLER_ID = ResourceLocation.fromNamespaceAndPath("noellesroles", "lost_killer");

    public static final SRERole KIRA;
    public static final SRERole JOSUKE;
    public static final net.minecraft.resources.ResourceLocation JOSUKE_ITEM = KiraAddon.id("josuke_revive");
    
    static {
        KIRA = TMMRoles.registerRole(new NormalRole(
                KIRA_ID,
                new java.awt.Color(255, 215, 0).getRGB(),
                false,
                true,
                SRERole.MoodType.FAKE,
                Integer.MAX_VALUE,
                true
        ) {
            @Override
            public List<ShopEntry> getShopEntries() {
                return KiraShopHandler.getEntries();
            }
        }.setComponentKey(KiraComponents.KIRA_PLAYER_KEY))
                .setCanSeeCoin(true)
                .setOccupiedRoleCount(2)
                .setCanBeRandomedByOtherRoles(false);

        JOSUKE = TMMRoles.registerRole(new NormalRole(
                JOSUKE_ID,
                new java.awt.Color(112, 64, 180).getRGB(),
                true,
                false,
                SRERole.MoodType.REAL,
                100,
                false
        ).setVigilanteTeam(true)
         .setCanBeRandomedByOtherRoles(false)
         .setComponentKey(KiraComponents.JOSUKE_PLAYER_KEY));
    }

    public static void init() {
        JosukeFistPunchHandler.register();
        JosukeSkillHandler.register();
        KiraBitesTheDustHandler.register();
        LOGGER.info("Registered KiraAddon roles: {} ({}) and {} ({})", KIRA_ID, KIRA.getClass().getSimpleName(), JOSUKE_ID, JOSUKE.getClass().getSimpleName());
        RoleAssignmentManager.addOccupationRole(KIRA, JOSUKE);
        Harpymodloader.setRoleMaximum(KIRA_ID, 1);
        Harpymodloader.setRoleMaximum(JOSUKE_ID, 0);
        LOGGER.info("KiraAddon role maximum configured for {} and {}", KIRA_ID, JOSUKE_ID);
        KiraAddon.LOGGER.info("Registered KiraAddon roles and occupation binding");
    }
}