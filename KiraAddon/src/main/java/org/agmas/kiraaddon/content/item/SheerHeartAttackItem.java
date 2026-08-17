package org.agmas.kiraaddon.content.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.agmas.kiraaddon.KiraAddon;
import org.agmas.kiraaddon.content.entity.SheerHeartEntity;
import org.agmas.kiraaddon.init.ModEntities;

public class SheerHeartAttackItem extends Item {
    private static final int COOLDOWN_TICKS = 600;

    public SheerHeartAttackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            float remaining = player.getCooldowns().getCooldownPercent(this, 0);
            player.displayClientMessage(
                Component.translatable("message.sheer_heart.cooldown", (int) (remaining / 20)), 
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        // 检查是否已经有活跃的枯萎穿心实体
        if (hasActiveSheerHeart(player, level)) {
            player.displayClientMessage(Component.translatable("message.sheer_heart.already_active"), true);
            return InteractionResultHolder.fail(stack);
        }
        
        // 检查实体类型是否为null
        if (ModEntities.SHEER_HEART == null) {
            KiraAddon.LOGGER.error("ModEntities.SHEER_HEART is NULL! Entity type not initialized!");
            return InteractionResultHolder.fail(stack);
        }

        // 在玩家位置生成，高度为地面+0.85
        double spawnX = player.getX();
        double spawnY = player.getY() + 0.85; // 地面高度+0.85
        double spawnZ = player.getZ();

        // 消耗物品
        stack.shrink(1);

        try {
            // 创建并生成枯萎穿心实体
            SheerHeartEntity entity = new SheerHeartEntity(ModEntities.SHEER_HEART, level);
            entity.setPos(spawnX, spawnY, spawnZ);
            entity.setOwnerUUID(player.getUUID());
            
            boolean added = level.addFreshEntity(entity);
            
            if (added) {
                KiraAddon.LOGGER.info("Successfully spawned SheerHeart entity at ({}, {}, {})", spawnX, spawnY, spawnZ);
                
                // 播放音效（使用与以前版本相同的音效）
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_EYE_DEATH, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

                // 添加冷却
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            } else {
                KiraAddon.LOGGER.error("Failed to spawn SheerHeart entity! Level may not allow entity spawning.");
                stack.grow(1);
                return InteractionResultHolder.fail(stack);
            }
        } catch (Exception e) {
            KiraAddon.LOGGER.error("Error spawning SheerHeart entity: {}", e.getMessage());
            e.printStackTrace();
            stack.grow(1);
            return InteractionResultHolder.fail(stack);
        }

        return InteractionResultHolder.success(stack);
    }

    private boolean hasActiveSheerHeart(Player player, Level level) {
        return level.getEntitiesOfClass(SheerHeartEntity.class, 
                player.getBoundingBox().inflate(200), 
                entity -> entity.getOwnerUUID() != null && entity.getOwnerUUID().equals(player.getUUID())).size() > 0;
    }
}