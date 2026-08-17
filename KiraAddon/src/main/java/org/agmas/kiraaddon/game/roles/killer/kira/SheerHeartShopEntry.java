package org.agmas.kiraaddon.game.roles.killer.kira;

import io.wifi.starrailexpress.util.ShopEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.agmas.kiraaddon.content.entity.SheerHeartEntity;
import org.agmas.kiraaddon.init.ModItems;
import org.jetbrains.annotations.NotNull;

public class SheerHeartShopEntry extends ShopEntry {
    public SheerHeartShopEntry(ItemStack stack, int price, Type type) {
        super(stack, price, type);
    }

    @Override
    public boolean canBuy(@NotNull Player player) {
        if (!super.canBuy(player)) {
            return false;
        }
        
        // 检查是否有活跃的枯萎穿心实体
        if (hasActiveSheerHeart(player)) {
            return false;
        }
        
        // 如果背包里存在枯萎穿心物品，无法再次购买
        if (hasSheerHeartInInventory(player)) {
            return false;
        }
        
        return true;
    }

    private boolean hasActiveSheerHeart(Player player) {
        return player.level().getEntitiesOfClass(SheerHeartEntity.class, 
                player.getBoundingBox().inflate(200), 
                entity -> entity.getOwnerUUID() != null && entity.getOwnerUUID().equals(player.getUUID())).size() > 0;
    }
    
    private boolean hasSheerHeartInInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ModItems.SHEER_HEART_ATTACK)) {
                return true;
            }
        }
        return false;
    }
}