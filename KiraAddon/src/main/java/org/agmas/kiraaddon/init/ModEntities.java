package org.agmas.kiraaddon.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.agmas.kiraaddon.KiraAddon;
import org.agmas.kiraaddon.content.entity.SheerHeartEntity;

public class ModEntities {
    public static final EntityType<SheerHeartEntity> SHEER_HEART = FabricEntityTypeBuilder.<SheerHeartEntity>create(MobCategory.MONSTER, 
            SheerHeartEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
            .build();

    public static void init() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "sheer_heart"), SHEER_HEART);
        
        // 使用 Fabric API 注册实体属性
        FabricDefaultAttributeRegistry.register(SHEER_HEART, SheerHeartEntity.createAttributes());
    }
}