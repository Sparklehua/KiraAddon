package org.agmas.kiraaddon.events;

import io.wifi.starrailexpress.cca.AreasWorldComponent;
import io.wifi.starrailexpress.cca.SREGameTimeComponent;
import io.wifi.starrailexpress.cca.SREGameWorldComponent;
import io.wifi.starrailexpress.cca.SREPlayerShopComponent;
import io.wifi.starrailexpress.event.AllowPlayerDeathWithKiller;
import io.wifi.starrailexpress.event.OnGameEnd;
import io.wifi.starrailexpress.event.OnGameTrueStarted;
import io.wifi.starrailexpress.event.OnPlayerKilledPlayer;
import io.wifi.starrailexpress.game.GameConstants;
import io.wifi.starrailexpress.game.GameUtils;
import io.wifi.starrailexpress.game.ServerTaskInfoClasses;
import org.agmas.noellesroles.Noellesroles;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.agmas.kiraaddon.cca.KiraComponents;
import org.agmas.kiraaddon.cca.KiraPlayerComponent;
import org.agmas.kiraaddon.game.roles.vigilante.josuke.JosukeFistPunchHandler;
import org.agmas.kiraaddon.init.ModRoles;
import org.agmas.kiraaddon.init.ModSounds;
import org.agmas.noellesroles.init.ModEffects;

public class KiraEvents {
    public static void registerEvents() {
        // 游戏结束时清除连击记录
        OnGameEnd.EVENT.register((serverLevel, gameWorldComponent) -> {
            JosukeFistPunchHandler.PUNCH_RECORDS.clear();
        });
        
        // 游戏真正开始时清除连击记录
        OnGameTrueStarted.EVENT.register((serverLevel) -> {
            JosukeFistPunchHandler.PUNCH_RECORDS.clear();
        });

        // 败者食尘回溯后，若场上只剩吉良，明确结算为杀手胜利。
        // 这样不会因死亡回调与结束判定的时序差异而被错误判为乘客胜利或继续游戏。
        io.wifi.starrailexpress.event.AllowGameEnd.EVENT.register((serverLevel, winStatus, isLooseEndsMode) -> {
            if (isLooseEndsMode || winStatus == GameUtils.WinStatus.TIME) {
                return GameUtils.WinStatus.NOT_MODIFY;
            }

            SREGameWorldComponent gameWorld = SREGameWorldComponent.KEY.get(serverLevel);
            if (gameWorld == null || !gameWorld.isRunning()) {
                return GameUtils.WinStatus.NOT_MODIFY;
            }

            boolean kiraAlive = false;
            boolean otherAlive = false;
            for (ServerPlayer player : serverLevel.players()) {
                if (!GameUtils.isPlayerAliveAndSurvival(player)) {
                    continue;
                }
                if (gameWorld.isRole(player, ModRoles.KIRA)) {
                    kiraAlive = true;
                } else {
                    otherAlive = true;
                }
            }

            if (kiraAlive && !otherAlive) {
                return GameUtils.WinStatus.KILLERS;
            }
            return GameUtils.WinStatus.NOT_MODIFY;
        });
        
        // 右键玩家标记事件
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return net.minecraft.world.InteractionResult.PASS;
            if (level.isClientSide()) return net.minecraft.world.InteractionResult.PASS;
            
            if (!(entity instanceof Player targetPlayer)) return net.minecraft.world.InteractionResult.PASS;
            
            SREGameWorldComponent gameWorld = SREGameWorldComponent.KEY.get(level);
            if (gameWorld == null || !gameWorld.isRunning() || !gameWorld.isRole(player, ModRoles.KIRA)) return net.minecraft.world.InteractionResult.PASS;
            
            // 检查是否在安全时间内
            if (player.hasEffect(ModEffects.SAFE_TIME)) {
                player.displayClientMessage(
                    Component.translatable("message.kira.safe_time_cannot_mark").withStyle(ChatFormatting.RED), 
                    true
                );
                return net.minecraft.world.InteractionResult.PASS;
            }
            
            KiraPlayerComponent kiraComp = KiraComponents.getKiraComponent(player);
            SREPlayerShopComponent shopComp = SREPlayerShopComponent.KEY.get(player);
            if (kiraComp == null || shopComp == null || targetPlayer == player) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            
            // 检查是否已标记
            if (kiraComp.isMarked(targetPlayer.getUUID())) {
                player.displayClientMessage(
                    Component.translatable("message.kira.already_marked"), 
                    true
                );
                return net.minecraft.world.InteractionResult.PASS;
            }
            
            // 标记冷却优先于数量上限，避免冷却期间显示错误提示
            if (kiraComp.getMarkCooldown() > 0) {
                int seconds = (kiraComp.getMarkCooldown() + 19) / 20;
                player.displayClientMessage(
                    Component.translatable("message.kira.mark_cooldown", seconds)
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return net.minecraft.world.InteractionResult.PASS;
            }

            // 检查标记数量上限
            if (!kiraComp.canAddMark()) {
                player.displayClientMessage(
                    Component.translatable("message.kira.max_marks_reached", kiraComp.getMarkCount(), KiraPlayerComponent.MAX_MARKS)
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return net.minecraft.world.InteractionResult.PASS;
            }
            
            // 检查金币
            if (shopComp == null || kiraComp == null || shopComp.balance < 10) {
                player.displayClientMessage(
                    Component.translatable("message.noellesroles.insufficient_funds"), 
                    true
                );
                return net.minecraft.world.InteractionResult.PASS;
            }
            
            // 扣除金币并添加标记
            shopComp.addToBalance(-10);
            kiraComp.addMark(targetPlayer.getUUID());
            
            // 当前 4.3.0 主模组已移除 SilencedPlayerComponent；标记状态由 KiraAddon 自己维护。
            // 不再硬引用旧组件，避免右键标记时触发 NoClassDefFoundError。
            
            player.displayClientMessage(
                Component.translatable("message.kira.marked_success", targetPlayer.getName(), kiraComp.getMarkCount(), KiraPlayerComponent.MAX_MARKS)
                    .withStyle(ChatFormatting.GREEN),
                true
            );
            
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
        
        OnPlayerKilledPlayer.EVENT.register((victim, killer, reason) -> {
            if (killer == null || killer.level().isClientSide()) return;
            SREGameWorldComponent gameWorld = SREGameWorldComponent.KEY.get(killer.level());
            if (gameWorld != null && gameWorld.isRunning() && gameWorld.isRole(killer, ModRoles.KIRA)) {
                KiraPlayerComponent kiraComp = KiraComponents.getKiraComponent(killer);
                if (kiraComp != null && !isKiraExplosionReason(reason)) {
                    kiraComp.incrementKillCount();
                }
            }
        });

        AllowPlayerDeathWithKiller.EVENT.register((victim, killer, deathReason) -> {
            if (victim == null || victim.level().isClientSide())
                return true;

            SREGameWorldComponent gameWorld = SREGameWorldComponent.KEY.get(victim.level());
            if (gameWorld == null || !gameWorld.isRunning() || !gameWorld.isRole(victim, ModRoles.KIRA))
                return true;

            KiraPlayerComponent kiraComp = KiraComponents.getKiraComponent(victim);
            if (kiraComp == null || !kiraComp.isBitesTheDustUnlocked() || kiraComp.isBitesTheDustUsed())
                return true;

            // 败者食尘只能由吉良吉影被直接击杀时触发，排除以下间接死亡机制：
            // - 巫毒绑定死亡（killer 为 null）
            // - 恋人殉情死亡（BROKEN_HEART）
            // - 冷箫/巫毒等强制处决（GOD_COMMAND / voodoo）
            if (killer == null
                || deathReason.equals(GameConstants.DeathReasons.BROKEN_HEART)
                || deathReason.equals(GameConstants.DeathReasons.GOD_COMMAND)
                || deathReason.equals(Noellesroles.id("voodoo"))) {
                return true;
            }

            if (victim instanceof ServerPlayer sp) {
                ServerLevel level = sp.serverLevel();

                for (ServerPlayer p : level.players()) {
                    p.connection.send(new ClientboundSoundPacket(
                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.KIRA_BITES_THE_DUST),
                        net.minecraft.sounds.SoundSource.MASTER,
                        p.getX(), p.getY(), p.getZ(), 5.0f, 1.0f, p.getRandom().nextLong()));
                }

                if (!kiraComp.useBitesTheDust()) {
                    return true;
                }
                int remaining = kiraComp.getRemainingBitesTheDust();

                // 在 StarRailExpress 的死亡许可阶段直接拦截死亡，避免进入旁观者状态。
                sp.setHealth(sp.getMaxHealth());
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 255, false, false));
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 255, false, false));

                for (Player p : level.players()) {
                    p.displayClientMessage(
                        Component.translatable("message.kira.bites_the_dust_activated", remaining)
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                        true
                    );
                }

                GameUtils.serverAsynTaskLists.add(new ServerTaskInfoClasses.SchedulerTask(20, () -> {
                    sp.setHealth(sp.getMaxHealth());
                    sp.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);

                    sp.removeAllEffects();

                    if (sp.isRemoved()) {
                        return;
                    }

                    if (kiraComp.hasMarkedPosition()) {
                        net.minecraft.world.phys.Vec3 pos = kiraComp.getMarkedPosition();
                        sp.teleportTo(pos.x, pos.y, pos.z);
                    } else {
                        net.minecraft.world.phys.Vec3 spawnPos = GameUtils.getSpawnPos(
                            AreasWorldComponent.KEY.get(level),
                            GameUtils.roomToPlayer.getOrDefault(sp.getUUID(), 1)
                        );
                        if (spawnPos != null) {
                            sp.teleportTo(spawnPos.x, spawnPos.y + 1, spawnPos.z);
                        }
                    }

                    AreasWorldComponent areas = AreasWorldComponent.KEY.get(level);
                    if (areas != null) {
                        for (ServerPlayer player : level.players()) {
                            if (player != sp && GameUtils.isPlayerAliveAndSurvival(player)) {
                                net.minecraft.world.phys.Vec3 spawnPos = GameUtils.getSpawnPos(
                                    areas,
                                    GameUtils.roomToPlayer.getOrDefault(player.getUUID(), 1)
                                );
                                if (spawnPos != null) {
                                    player.teleportTo(spawnPos.x, spawnPos.y + 1, spawnPos.z);
                                }
                            }
                        }
                    }

                    if (sp.isRemoved()) {
                        return;
                    }
                    SREGameTimeComponent gameTimeComponent = SREGameTimeComponent.KEY.get(level);
                    if (gameTimeComponent != null) {
                        gameTimeComponent.addTime(-KiraPlayerComponent.BITES_THE_DUST_ROLLBACK_TICKS);
                    }
                }));

            }

            return false;
        });
    }

    private static boolean isKiraExplosionReason(OnPlayerKilledPlayer.DeathReason reason) {
        return reason == OnPlayerKilledPlayer.DeathReason.GRENADE;
    }
}