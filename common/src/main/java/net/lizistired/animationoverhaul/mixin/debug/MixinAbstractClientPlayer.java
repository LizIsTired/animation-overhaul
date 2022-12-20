package net.lizistired.animationoverhaul.mixin.debug;

import gg.moonflower.pollen.api.platform.Platform;
import net.lizistired.animationoverhaul.AnimationOverhaulMain;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayer {
//    private static final Identifier debugCapeLocation = new Identifier("textures/testcape.png");
//
//    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
//    private void useDebugCapeTexture(CallbackInfoReturnable<Identifier> cir) {
//        if (!Platform.isProduction()) {
//            cir.setReturnValue(debugCapeLocation);
//        }
//    }
}
