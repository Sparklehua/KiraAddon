package org.agmas.kiraaddon.mixin.client;

import io.wifi.starrailexpress.content.entity.PlayerBodyEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 防止主模组尸体实体在移除后仍被客户端渲染器访问。
 */
@Mixin(PlayerBodyEntity.class)
public abstract class PlayerBodyEntityMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void kiraaddon$skipRemovedBody(double x, double y, double z,
            CallbackInfoReturnable<Boolean> cir) {
        PlayerBodyEntity body = (PlayerBodyEntity) (Object) this;
        if (body.isRemoved() || body.level() instanceof ClientLevel && body.getServer() == null) {
            cir.setReturnValue(false);
        }
    }
}
