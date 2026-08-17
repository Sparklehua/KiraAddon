package org.agmas.kiraaddon.cca;

import io.wifi.starrailexpress.api.RoleComponent;
import io.wifi.starrailexpress.cca.SREGameWorldComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KiraPlayerComponent implements ServerTickingComponent, ClientTickingComponent, RoleComponent {
    private final Player player;
    private SREGameWorldComponent gameWorldComponent = null;
    
    private Set<UUID> markedPlayers = new HashSet<>();
    private Map<UUID, Integer> markTimers = new HashMap<>();
    public static final int MAX_MARKS = 3;

    // 每次标记独立拥有 10 秒冷却；标记本身不因计时器自动过期。
    public static final int MARK_COOLDOWN_TICKS = 10 * 20;
    public static final int DETONATE_COOLDOWN_TICKS = 100;
    public static final int BITES_THE_DUST_ROLLBACK_TICKS = 30 * 20;
    public static final int JEB_TRIGGER_TICKS = 200;
    private int markCooldown = 0;
    
    private int killCount = 0;
    private int bitesTheDustCharges = 0;
    private int bitesTheDustUsedCount = 0;
    private boolean bitesTheDustSnapshot = false;
    private double snapshotX;
    private double snapshotY;
    private double snapshotZ;
    public static final int MAX_BITES_THE_DUST_CHARGES = 4;
    
    private static final int[] UNLOCK_KILL_THRESHOLDS = {3, 7, 12, 15};
    
    private double markedX = 0;
    private double markedY = 0;
    private double markedZ = 0;
    private boolean hasMarkedPosition = false;
    
    private int detonateCooldown = 0;

    public KiraPlayerComponent(Player player) {
        this.player = player;
    }
    
    public void sync() {
        KiraComponents.KIRA_PLAYER_KEY.sync(this.player);
    }
    
    @Override
    public Player getPlayer() {
        return this.player;
    }
    
    @Override
    public void init() {
        markedPlayers.clear();
        markTimers.clear();
        killCount = 0;
        bitesTheDustCharges = 0;
        bitesTheDustUsedCount = 0;
        detonateCooldown = 0;
        markCooldown = 0;
        hasMarkedPosition = false;
        bitesTheDustSnapshot = false;
        sync();
    }
    
    @Override
    public void clear() {
        init();
    }
    
    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return this.player == player;
    }

    private boolean checkIsGameRunning() {
        if (player == null || player.level() == null) {
            gameWorldComponent = null;
            return false;
        }
        gameWorldComponent = SREGameWorldComponent.KEY.get(player.level());
        return gameWorldComponent != null && gameWorldComponent.isRunning();
    }

    public boolean isMarked(UUID playerUuid) {
        return markedPlayers.contains(playerUuid);
    }

    public void addMark(UUID playerUuid) {
        if (playerUuid == null || markedPlayers.size() >= MAX_MARKS) {
            return;
        }
        markedPlayers.add(playerUuid);
        markTimers.put(playerUuid, 0); // 初始化计时器
        markCooldown = MARK_COOLDOWN_TICKS;
        
        // 给被标记的玩家添加静音效果（参照静语者实现）
        Player targetPlayer = this.player.level().getPlayerByUUID(playerUuid);
        if (targetPlayer != null && targetPlayer.isAlive() && targetPlayer instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // 添加语音静音效果
            serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                org.agmas.noellesroles.init.ModEffects.VOICE_SILENCE,
                Integer.MAX_VALUE, // 持续到标记被移除
                0,
                false,
                false,
                false
            ));
        }
        
        sync();
    }

    public void removeMark(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        
        // 移除静音效果
        Player targetPlayer = this.player.level().getPlayerByUUID(playerUuid);
        if (targetPlayer != null && targetPlayer.isAlive() && targetPlayer instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            if (serverPlayer.hasEffect(org.agmas.noellesroles.init.ModEffects.VOICE_SILENCE)) {
                serverPlayer.removeEffect(org.agmas.noellesroles.init.ModEffects.VOICE_SILENCE);
            }
        }
        
        markedPlayers.remove(playerUuid);
        markTimers.remove(playerUuid);
        removeJebModifier(playerUuid);
        sync();
    }

    public void clearKiraState() {
        markedPlayers.clear();
        markTimers.clear();
        sync();
    }

    public boolean canAddMark() {
        return markedPlayers.size() < MAX_MARKS && markCooldown <= 0;
    }
    
    public int getMarkCooldown() {
        return markCooldown;
    }

    public int getMarkCount() {
        return markedPlayers.size();
    }

    public Set<UUID> getMarkedPlayers() {
        return new HashSet<>(markedPlayers);
    }

    public boolean canDetonate() {
        return detonateCooldown <= 0;
    }

    public int getDetonateCooldown() {
        return detonateCooldown;
    }

    public void setDetonateCooldown(int ticks) {
        this.detonateCooldown = Math.max(0, ticks);
        sync();
    }

    public void incrementKillCount() {
        killCount++;
        updateBitesTheDustCharges();
        sync();
    }

    public int getKillCount() {
        return killCount;
    }

    private void updateBitesTheDustCharges() {
        for (int i = UNLOCK_KILL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (killCount >= UNLOCK_KILL_THRESHOLDS[i] && bitesTheDustCharges <= i) {
                bitesTheDustCharges = i + 1;
                break;
            }
        }
    }

    public boolean isBitesTheDustUnlocked() {
        return bitesTheDustCharges > 0;
    }

    public boolean isBitesTheDustReady() {
        return bitesTheDustCharges > bitesTheDustUsedCount && bitesTheDustSnapshot;
    }

    public boolean hasBitesTheDustSnapshot() {
        return bitesTheDustSnapshot;
    }

    public void saveBitesTheDustSnapshot(ServerPlayer player) {
        snapshotX = player.getX();
        snapshotY = player.getY();
        snapshotZ = player.getZ();
        bitesTheDustSnapshot = true;
        sync();
    }

    public void restoreBitesTheDustSnapshot(ServerPlayer player) {
        if (!bitesTheDustSnapshot) return;
        player.teleportTo(snapshotX, snapshotY, snapshotZ);
        player.setDeltaMovement(0, 0, 0);
        bitesTheDustSnapshot = false;
        sync();
    }

    public boolean isBitesTheDustUsed() {
        return bitesTheDustCharges <= 0 || bitesTheDustUsedCount >= bitesTheDustCharges;
    }

    public boolean useBitesTheDust() {
        if (!isBitesTheDustUnlocked() || isBitesTheDustUsed()) {
            return false;
        }
        bitesTheDustUsedCount++;
        sync();
        return true;
    }

    public void resetBitesTheDust() {
        bitesTheDustUsedCount = 0;
        sync();
    }

    public int getBitesTheDustCharges() {
        return bitesTheDustCharges;
    }

    public int getBitesTheDustUsedCount() {
        return bitesTheDustUsedCount;
    }

    public int getRemainingBitesTheDust() {
        return bitesTheDustCharges - bitesTheDustUsedCount;
    }

    public void markPosition(double x, double y, double z) {
        setMarkedPosition(x, y, z);
    }

    public net.minecraft.world.phys.Vec3 getMarkedPosition() {
        return new net.minecraft.world.phys.Vec3(markedX, markedY, markedZ);
    }

    public void setMarkedPosition(double x, double y, double z) {
        this.markedX = x;
        this.markedY = y;
        this.markedZ = z;
        this.hasMarkedPosition = true;
        sync();
    }

    public boolean hasMarkedPosition() {
        return hasMarkedPosition;
    }

    public double getMarkedX() {
        return markedX;
    }

    public double getMarkedY() {
        return markedY;
    }

    public double getMarkedZ() {
        return markedZ;
    }

    public void clearMarkedPosition() {
        this.hasMarkedPosition = false;
        sync();
    }

    @Override
    public void serverTick() {
        if (!checkIsGameRunning()) return;

        // 目标死亡或进入旁观时立即清除标记；爆炸、普通击杀等所有死亡方式均适用。
        java.util.Iterator<UUID> iterator = markedPlayers.iterator();
        while (iterator.hasNext()) {
            UUID targetUuid = iterator.next();
            Player targetPlayer = this.player.level().getPlayerByUUID(targetUuid);
            if (targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isSpectator()) {
                // 移除静音效果
                if (targetPlayer != null && targetPlayer instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    if (serverPlayer.hasEffect(org.agmas.noellesroles.init.ModEffects.VOICE_SILENCE)) {
                        serverPlayer.removeEffect(org.agmas.noellesroles.init.ModEffects.VOICE_SILENCE);
                    }
                }
                iterator.remove();
                markTimers.remove(targetUuid);
                removeJebModifier(targetUuid);
                sync();
            }
        }

        // 更新冷却时间
        if (detonateCooldown > 0) {
            detonateCooldown--;
        }
        
        // 更新标记冷却时间
        if (markCooldown > 0) {
            markCooldown--;
        }

        // 标记持续10秒后自动给予JEB修饰符；每个目标独立计时。
        boolean timersChanged = false;
        for (UUID targetUuid : new HashSet<>(markedPlayers)) {
            Player targetPlayer = this.player.level().getPlayerByUUID(targetUuid);
            if (targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isSpectator()) {
                continue;
            }

            int timer = markTimers.getOrDefault(targetUuid, 0);
            if (timer < JEB_TRIGGER_TICKS) {
                timer++;
                markTimers.put(targetUuid, timer);
                timersChanged = true;
            }

            if (timer == JEB_TRIGGER_TICKS) {
                addJebModifierToMarkedPlayer(targetUuid);
                // 防止后续tick重复触发通知和添加逻辑。
                markTimers.put(targetUuid, timer + 1);
                timersChanged = true;
            }
        }

        if (timersChanged) {
            sync();
        }
    }

    private void removeJebModifier(UUID targetUuid) {
        org.agmas.harpymodloader.component.WorldModifierComponent component =
                org.agmas.harpymodloader.component.WorldModifierComponent.KEY.get(this.player.level());
        if (component != null) {
            component.removeModifier(targetUuid, pro.fazeclan.river.stupid_express.constants.SEModifiers.JEB_);
        }
    }

    /**
     * 给被标记的玩家添加jeb修饰符
     */
    private void addJebModifierToMarkedPlayer(UUID targetUuid) {
        if (!markedPlayers.contains(targetUuid)) {
            return;
        }

        Player targetPlayer = this.player.level().getPlayerByUUID(targetUuid);
        if (targetPlayer == null || !targetPlayer.isAlive()) {
            return;
        }

        // 添加jeb修饰符
        org.agmas.harpymodloader.component.WorldModifierComponent worldModifierComponent =
            org.agmas.harpymodloader.component.WorldModifierComponent.KEY.get(this.player.level());
        if (worldModifierComponent == null) {
            return;
        }

        // 按主模组规范使用 WorldModifierComponent 管理修饰符，避免重复添加。
        if (!worldModifierComponent.isModifier(targetUuid,
                pro.fazeclan.river.stupid_express.constants.SEModifiers.JEB_)) {
            worldModifierComponent.addModifier(targetUuid,
                    pro.fazeclan.river.stupid_express.constants.SEModifiers.JEB_);
        }
        sync();

        // 通知吉良吉影玩家
        if (this.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.kira.jeb_added_auto", targetPlayer.getName())
                    .withStyle(net.minecraft.ChatFormatting.GREEN),
                true
            );
        }

        // 通知被标记的玩家
        if (targetPlayer instanceof net.minecraft.server.level.ServerPlayer targetServerPlayer) {
            targetServerPlayer.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.kira.jeb_received")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
        }
    }

    @Override
    public void clientTick() {
        // 客户端不需要处理
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("KillCount", killCount);
        tag.putInt("BitesTheDustCharges", bitesTheDustCharges);
        tag.putInt("BitesTheDustUsedCount", bitesTheDustUsedCount);
        tag.putBoolean("BitesTheDustSnapshot", bitesTheDustSnapshot);
        tag.putDouble("SnapshotX", snapshotX);
        tag.putDouble("SnapshotY", snapshotY);
        tag.putDouble("SnapshotZ", snapshotZ);
        tag.putInt("DetonateCooldown", detonateCooldown);
        tag.putInt("MarkCooldown", markCooldown);
        tag.putDouble("MarkedX", markedX);
        tag.putDouble("MarkedY", markedY);
        tag.putDouble("MarkedZ", markedZ);
        tag.putBoolean("HasMarkedPosition", hasMarkedPosition);
        
        // 保存标记的玩家
        CompoundTag marksTag = new CompoundTag();
        for (UUID uuid : markedPlayers) {
            marksTag.putString(uuid.toString(), String.valueOf(markTimers.getOrDefault(uuid, 0)));
        }
        tag.put("MarkedPlayers", marksTag);
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        killCount = tag.getInt("KillCount");
        bitesTheDustCharges = tag.getInt("BitesTheDustCharges");
        bitesTheDustUsedCount = tag.getInt("BitesTheDustUsedCount");
        bitesTheDustSnapshot = tag.getBoolean("BitesTheDustSnapshot");
        snapshotX = tag.getDouble("SnapshotX");
        snapshotY = tag.getDouble("SnapshotY");
        snapshotZ = tag.getDouble("SnapshotZ");
        detonateCooldown = tag.getInt("DetonateCooldown");
        markCooldown = tag.getInt("MarkCooldown");
        markedX = tag.getDouble("MarkedX");
        markedY = tag.getDouble("MarkedY");
        markedZ = tag.getDouble("MarkedZ");
        hasMarkedPosition = tag.getBoolean("HasMarkedPosition");
        
        // 读取标记的玩家
        CompoundTag marksTag = tag.getCompound("MarkedPlayers");
        markedPlayers.clear();
        markTimers.clear();
        for (String key : marksTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                markedPlayers.add(uuid);
                markTimers.put(uuid, marksTag.getInt(key));
            } catch (IllegalArgumentException e) {
                // 忽略无效的UUID
            }
        }
    }
    
    @Override
    public void writeToSyncNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("KillCount", killCount);
        tag.putInt("BitesTheDustCharges", bitesTheDustCharges);
        tag.putInt("BitesTheDustUsedCount", bitesTheDustUsedCount);
        tag.putBoolean("BitesTheDustSnapshot", bitesTheDustSnapshot);
        tag.putDouble("SnapshotX", snapshotX);
        tag.putDouble("SnapshotY", snapshotY);
        tag.putDouble("SnapshotZ", snapshotZ);
        tag.putInt("DetonateCooldown", detonateCooldown);
        tag.putInt("MarkCooldown", markCooldown);
        
        // 保存标记的玩家
        CompoundTag marksTag = new CompoundTag();
        for (UUID uuid : markedPlayers) {
            marksTag.putString(uuid.toString(), String.valueOf(markTimers.getOrDefault(uuid, 0)));
        }
        tag.put("MarkedPlayers", marksTag);
    }
    
    @Override
    public void readFromSyncNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        killCount = tag.getInt("KillCount");
        bitesTheDustCharges = tag.getInt("BitesTheDustCharges");
        bitesTheDustUsedCount = tag.getInt("BitesTheDustUsedCount");
        bitesTheDustSnapshot = tag.getBoolean("BitesTheDustSnapshot");
        snapshotX = tag.getDouble("SnapshotX");
        snapshotY = tag.getDouble("SnapshotY");
        snapshotZ = tag.getDouble("SnapshotZ");
        detonateCooldown = tag.getInt("DetonateCooldown");
        markCooldown = tag.getInt("MarkCooldown");
        
        // 读取标记的玩家
        CompoundTag marksTag = tag.getCompound("MarkedPlayers");
        markedPlayers.clear();
        markTimers.clear();
        for (String key : marksTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                markedPlayers.add(uuid);
                markTimers.put(uuid, marksTag.getInt(key));
            } catch (IllegalArgumentException e) {
                // 忽略无效的UUID
            }
        }
    }
}