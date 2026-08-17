package org.agmas.kiraaddon.content.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.agmas.kiraaddon.KiraAddon;
import org.agmas.kiraaddon.content.entity.SheerHeartEntity;

public class SheerHeartEntityRenderer extends MobRenderer<SheerHeartEntity, SheerHeartEntityModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KiraAddon.MOD_ID, "textures/entity/sheer_heart.png");

    public SheerHeartEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SheerHeartEntityModel(context.bakeLayer(SheerHeartEntityModel.LAYER_LOCATION)), 0F);
    }

    @Override
    protected void scale(SheerHeartEntity entity, PoseStack poseStack, float partialTickTime) {
        float swellProgress = entity.getSwellProgress();
        // 起爆时稍微变大
        float swellScale = 1.0F + swellProgress * 0.2F;
        
        // 将模型缩小到原来的二分之一
        float baseScale = 0.5F;
        float totalScale = baseScale * swellScale;
        
        // 模型高度约为0.9格
        float modelHeight = 0.9F;
        
        // 中心缩放：向上平移到中心，缩放，再向下平移
        // 但需要确保底部保持在地面之上
        float halfHeight = modelHeight / 2.0F;
        
        // 先向上平移到模型中心
        poseStack.translate(0.0F, halfHeight, 0.0F);
        // 进行缩放
        poseStack.scale(totalScale, totalScale, totalScale);
        // 向下平移回原始位置，但稍微向上偏移一点避免遁地
        poseStack.translate(0.0F, -halfHeight + (1.0F - totalScale) * 0.1F, 0.0F);
        
        // 整体向下移动0.47格（0.45 + 0.02）
        poseStack.translate(0.0F, -0.47F, 0.0F);
        
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(SheerHeartEntity entity) {
        return TEXTURE;
    }
}