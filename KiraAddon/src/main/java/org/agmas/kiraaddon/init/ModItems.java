package org.agmas.kiraaddon.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.agmas.kiraaddon.KiraAddon;
import org.agmas.kiraaddon.content.item.DetonateButtonItem;
import org.agmas.kiraaddon.content.item.SheerHeartAttackItem;

public class ModItems {
    public static final SheerHeartAttackItem SHEER_HEART_ATTACK = new SheerHeartAttackItem(new Item.Properties().stacksTo(1));
    public static final DetonateButtonItem DETONATE_BUTTON = new DetonateButtonItem(new Item.Properties().stacksTo(1));

    public static void init() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "kira_sheer_heart_attack"), SHEER_HEART_ATTACK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "detonate_button"), DETONATE_BUTTON);
    }
}