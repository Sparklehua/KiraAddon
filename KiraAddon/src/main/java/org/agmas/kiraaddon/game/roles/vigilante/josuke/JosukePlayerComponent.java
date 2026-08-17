package org.agmas.kiraaddon.game.roles.vigilante.josuke;

import io.wifi.starrailexpress.api.RoleComponent;
import io.wifi.starrailexpress.cca.SREGameWorldComponent;
import io.wifi.starrailexpress.game.GameUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.agmas.kiraaddon.cca.KiraComponents;
import org.agmas.kiraaddon.init.ModRoles;
import org.agmas.noellesroles.init.ModEffects;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * 东方仗助组件
 *
 * 用于管理玩家的状态
 * 该组件会自动在客户端和服务端之间同步
 *
 * 功能：
 * - 空手连击击杀（由 JosukeFistPunchHandler 处理）
 * - 护盾破碎冲击波（由 JosukeFistPunchHandler 处理）
 * - 无孤独死亡机制
 * - 无漂浮技能
 * - 无HUD显示
 */
public class JosukePlayerComponent
        implements RoleComponent, ServerTickingComponent, ClientTickingComponent {

    @Override
    public Player getPlayer() {
        return player;
    }

    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<JosukePlayerComponent> KEY = KiraComponents.JOSUKE_PLAYER_KEY;

    // 持有该组件的玩家
    private final Player player;
    private int reviveCooldown;

    /**
     * 构造函数
     */
    public JosukePlayerComponent(Player player) {
        this.player = player;
    }

    /**
     * 重置组件状态
     * 在游戏开始时或角色分配时调用
     */
    @Override
    public void init() {
        reviveCooldown = 0;
        this.sync();
    }

    public int getReviveCooldown() {
        return reviveCooldown;
    }

    public boolean isReviveReady() {
        return reviveCooldown <= 0;
    }

    public void startReviveCooldown() {
        reviveCooldown = 2 * 60 * 20;
        sync();
    }

    @Override
    public void clear() {
        player.removeEffect(MobEffects.DIG_SPEED);
        this.init();
    }

    /**
     * 同步到客户端
     */
    public void sync() {
        KEY.sync(this.player);
    }

    // ==================== Tick 处理 ====================

    @Override
    public void serverTick() {
        // 服务端tick处理
        var gameWorldComponent = SREGameWorldComponent.KEY.get(player.level());
        if (!gameWorldComponent.isRunning())
            return;
        if (!gameWorldComponent.isRole(player, ModRoles.JOSUKE))
            return;
        
        if (reviveCooldown > 0) {
            reviveCooldown--;
            sync();
        }

        if (!player.hasEffect(MobEffects.DIG_SPEED)) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, -1, 1, false, false, true));
        }

        // 空手攻击和护盾破碎逻辑由 JosukeFistPunchHandler 处理
    }

    @Override
    public void clientTick() {
        // 客户端tick处理
        var gameWorldComponent = SREGameWorldComponent.KEY.get(player.level());
        if (!gameWorldComponent.isRunning()) {
            return;
        }
        if (!gameWorldComponent.isRole(player, ModRoles.JOSUKE)) {
            return;
        }

        // 安全时间内，东方仗助不会感知到吉良吉影
        if (player.hasEffect(ModEffects.SAFE_TIME)) {
            return;
        }

        boolean kiraNearby = false;
        if (player.level() instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel) {
            double radiusSqr = 6.0D * 6.0D;
            for (Player other : clientLevel.players()) {
                if (other == player) {
                    continue;
                }
                if (!other.isAlive()) {
                    continue;
                }
                if (gameWorldComponent.isRole(other, ModRoles.KIRA) && other.distanceToSqr(player) <= radiusSqr) {
                    kiraNearby = true;
                    break;
                }
            }
        }

        if (kiraNearby) {
            player.displayClientMessage(
                Component.translatable("message.kiraaddon.josuke.kira_nearby").withStyle(ChatFormatting.RED),
                true
            );
        }
    }

    // ==================== NBT 序列化 ====================

    @Override
    public void writeToSyncNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("ReviveCooldown", reviveCooldown);
    }

    @Override
    public void readFromSyncNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        reviveCooldown = tag.getInt("ReviveCooldown");
    }
    
    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("ReviveCooldown", reviveCooldown);
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        // 东方仗助没有需要持久化的数据
    }
}