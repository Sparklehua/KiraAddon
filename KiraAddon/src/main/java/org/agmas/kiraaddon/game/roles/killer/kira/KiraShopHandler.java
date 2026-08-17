package org.agmas.kiraaddon.game.roles.killer.kira;

import io.wifi.starrailexpress.game.ShopContent;
import io.wifi.starrailexpress.index.TMMItems;
import io.wifi.starrailexpress.util.ShopEntry;
import org.agmas.kiraaddon.KiraAddon;
import org.agmas.kiraaddon.init.ModItems;
import org.agmas.kiraaddon.init.ModRoles;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraShopHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("KiraAddon-Shop");
    public static ArrayList<ShopEntry> getEntries() {
        ArrayList<ShopEntry> kiraShop = new ArrayList<>();

        // 假枪 - 10金币
        kiraShop.add(new ShopEntry(
            org.agmas.noellesroles.init.ModItems.FAKE_REVOLVER.getDefaultInstance(),
            10,
            ShopEntry.Type.WEAPON));

        // 关灯 - 100金币
        kiraShop.add(new ShopEntry(TMMItems.BLACKOUT.getDefaultInstance(), 100, ShopEntry.Type.TOOL));
        kiraShop.add(new ShopEntry(TMMItems.LOCKPICK.getDefaultInstance(), 80, ShopEntry.Type.TOOL));
        kiraShop.add(new ShopEntry(TMMItems.KNIFE.getDefaultInstance(), 105, ShopEntry.Type.WEAPON));
        kiraShop.add(new SheerHeartShopEntry(
            ModItems.SHEER_HEART_ATTACK.getDefaultInstance(), 125, ShopEntry.Type.WEAPON));
        return kiraShop;
    }

    public static void init() {
        LOGGER.info("Initializing KiraAddon shop entries for {}", ModRoles.KIRA_ID);
        var kiraShop = getEntries();
        
        // 移除旧的注册，确保我们的注册是最新的
        ShopContent.customEntries.put(ModRoles.KIRA_ID, kiraShop);
        
        KiraAddon.LOGGER.info("Kira shop overridden with custom entries!");
        KiraAddon.LOGGER.info("Shop entries count: {}", kiraShop.size());
        KiraAddon.LOGGER.info("Custom entries contains KIRA_ID: {}", ShopContent.customEntries.containsKey(ModRoles.KIRA_ID));
    }
}