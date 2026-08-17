package org.agmas.kiraaddon.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.agmas.kiraaddon.KiraAddon;

public class ModSounds {
    // 枯萎穿心脚步声
    public static final SoundEvent SHEER_HEART_STEP = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "sheer_heart_step"));
    
    // 枯萎穿心膨胀/炸弹音效
    public static final SoundEvent SHEER_HEART_BOMB = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "sheer_heart_bomb"));

    // 败者食尘触发音效，音频资源来自旧吉良吉影模组
    public static final SoundEvent KIRA_BITES_THE_DUST = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "kira_bites_the_dust"));

    // 东方仗助复活音效，音频资源来自工作区钻石.ogg
    public static final SoundEvent JOSUKE_REVIVE = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "josuke_revive"));

    public static void init() {
        Registry.register(BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "sheer_heart_step"), SHEER_HEART_STEP);
        Registry.register(BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "sheer_heart_bomb"), SHEER_HEART_BOMB);
        Registry.register(BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "kira_bites_the_dust"), KIRA_BITES_THE_DUST);
        Registry.register(BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "josuke_revive"), JOSUKE_REVIVE);
    }
}