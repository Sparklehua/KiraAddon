package org.agmas.kiraaddon.client;

import io.wifi.starrailexpress.api.SRERole;
import io.wifi.starrailexpress.cca.SREGameWorldComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import io.wifi.utils.client.betterrender.FakeGuiGraphics;
import org.agmas.noellesroles.client.event.RoleHudRenderCallback;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.agmas.kiraaddon.cca.KiraComponents;
import org.agmas.kiraaddon.cca.KiraPlayerComponent;
import org.agmas.kiraaddon.init.ModRoles;

public class KiraHudRenderer {
    private static final int[] KILL_THRESHOLDS = {3, 7, 12, 15};
    private static final int MAX_CHARGES = 4;

    public static void init() {
        RoleHudRenderCallback.EVENT.register(ModRoles.KIRA_ID,
                (context, tracker) -> renderHud(context, tracker));
    }

    private static void renderHud(FakeGuiGraphics context, DeltaTracker tracker) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        
        if (player == null) return;

        SREGameWorldComponent gameWorld = SREGameWorldComponent.KEY.get(player.level());
        if (gameWorld == null || !gameWorld.isRunning()) return;

        SRERole role = gameWorld.getRole(player);
        if (role == null || !role.identifier().equals(ModRoles.KIRA_ID)) return;

        KiraPlayerComponent kiraComponent = KiraComponents.KIRA_PLAYER_KEY.get(player);
        
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        
        // 败者食尘信息保持旧版右下角布局。
        int baseX = width - 180;
        int baseY = height - 100;

        int lineHeight = 12;

        // 绘制击杀进度
        int killCount = kiraComponent.getKillCount();
        int charges = kiraComponent.getBitesTheDustCharges();
        int used = kiraComponent.getBitesTheDustUsedCount();
        int remaining = charges - used;

        // 击杀计数文字
        Component killText = Component.translatable("hud.kira.kill_count", killCount)
                .withStyle(ChatFormatting.GOLD);
        context.drawString(client.font, killText, baseX, baseY, 0xFFFFFF);

        // 进度条背景
        int progressBarWidth = 150;
        int progressBarHeight = 8;
        context.fill(baseX, baseY + 14, baseX + progressBarWidth, baseY + 14 + progressBarHeight, 0x44000000);

        // 计算进度
        int nextThresholdIndex = 0;
        for (int i = 0; i < KILL_THRESHOLDS.length; i++) {
            if (killCount >= KILL_THRESHOLDS[i]) {
                nextThresholdIndex = i + 1;
            }
        }

        int currentProgress;
        if (nextThresholdIndex >= KILL_THRESHOLDS.length) {
            currentProgress = progressBarWidth;
        } else if (nextThresholdIndex == 0) {
            currentProgress = (killCount * progressBarWidth) / KILL_THRESHOLDS[0];
        } else {
            int prevThreshold = KILL_THRESHOLDS[nextThresholdIndex - 1];
            int nextThreshold = KILL_THRESHOLDS[nextThresholdIndex];
            int segmentProgress = killCount - prevThreshold;
            int segmentTotal = nextThreshold - prevThreshold;
            int completedWidth = (prevThreshold * progressBarWidth) / KILL_THRESHOLDS[KILL_THRESHOLDS.length - 1];
            int segmentWidth = (segmentProgress * progressBarWidth) / (KILL_THRESHOLDS[KILL_THRESHOLDS.length - 1] - prevThreshold);
            currentProgress = completedWidth + segmentWidth;
        }

        // 进度条颜色根据阶段变化
        int progressColor;
        if (killCount >= 15) {
            progressColor = 0xFFD700; // 金色 - 完全解锁
        } else if (killCount >= 12) {
            progressColor = 0x00FF00; // 绿色 - 3次
        } else if (killCount >= 7) {
            progressColor = 0x00FFFF; // 青色 - 2次
        } else if (killCount >= 3) {
            progressColor = 0xFF00FF; // 紫色 - 1次
        } else {
            progressColor = 0xFF6600; // 橙色 - 未解锁
        }

        context.fill(baseX, baseY + 14, baseX + Math.min(currentProgress, progressBarWidth), 
                baseY + 14 + progressBarHeight, progressColor);

        // 绘制阈值标记
        int markY = baseY + 14 + progressBarHeight;
        for (int i = 0; i < KILL_THRESHOLDS.length; i++) {
            int markX = baseX + (KILL_THRESHOLDS[i] * progressBarWidth) / KILL_THRESHOLDS[KILL_THRESHOLDS.length - 1];
            context.fill(markX - 1, markY, markX + 1, markY + 4, 0xFFFFFF);
        }

        // 败者食尘显示实际已获得次数与剩余次数，不能使用阶段阈值代替计数。
        int currentCharges = charges;
        String bitesDisplay = String.format("败者食尘（剩余 %d 次）", Math.max(0, remaining));
        String usageText = String.format("已使用次数：%d", used);
        
        // 绘制败者食尘状态
        Component bitesText = Component.literal(bitesDisplay)
                .withStyle(currentCharges > 0 ? ChatFormatting.DARK_RED : ChatFormatting.GRAY);
        context.drawString(client.font, bitesText, baseX, baseY + 28, 0xFFFFFF);
        
        // 绘制使用次数
        Component usageComponent = Component.literal(usageText)
                .withStyle(currentCharges > 0 ? ChatFormatting.GOLD : ChatFormatting.GRAY);
        context.drawString(client.font, usageComponent, baseX, baseY + 40, 0xFFFFFF);

        // 绘制标记位置状态
        if (kiraComponent.hasMarkedPosition()) {
            Component posText = Component.translatable("hud.kira.position_marked")
                    .withStyle(ChatFormatting.GREEN);
            context.drawString(client.font, posText, baseX, baseY + 52, 0xFFFFFF);
        }

        // 绘制引爆冷却时间
        int cooldown = kiraComponent.getDetonateCooldown();
        if (cooldown > 0) {
            int seconds = (cooldown + 19) / 20;
            Component cooldownText = Component.translatable("hud.kira.detonate_cooldown", seconds)
                    .withStyle(ChatFormatting.RED);
            context.drawString(client.font, cooldownText, baseX, baseY + 64, 0xFFFFFF);
        }

        // 绘制当前标记数量
        int markCount = kiraComponent.getMarkCount();
        Component markText = Component.translatable("hud.kira.mark_count", markCount, KiraPlayerComponent.MAX_MARKS)
                .withStyle(ChatFormatting.BLUE);
        context.drawString(client.font, markText, baseX, baseY + 76, 0xFFFFFF);

        // 标记冷却仅在冷却期间显示，位置固定在玩家物品栏上方。
        int markCooldown = kiraComponent.getMarkCooldown();
        if (markCooldown > 0) {
            int seconds = (markCooldown + 19) / 20;
            Component cooldownText = Component.translatable("hud.kira.mark_cooldown", seconds)
                    .withStyle(ChatFormatting.RED);
            int cooldownX = (width - client.font.width(cooldownText)) / 2;
            context.drawString(client.font, cooldownText, cooldownX, height - 48, 0xFFFFFF);
        }
    }
}