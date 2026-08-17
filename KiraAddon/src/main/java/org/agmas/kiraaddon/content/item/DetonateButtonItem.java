package org.agmas.kiraaddon.content.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DetonateButtonItem extends Item {
    public DetonateButtonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            // 客户端发送引爆请求
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new org.agmas.kiraaddon.network.KiraC2SPacket(new java.util.UUID(0, 0), org.agmas.kiraaddon.network.KiraC2SPacket.ACTION_DETONATE));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.success(stack);
    }
}