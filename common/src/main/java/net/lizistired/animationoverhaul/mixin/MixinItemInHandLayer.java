package net.lizistired.animationoverhaul.mixin;

import net.lizistired.animationoverhaul.animations.AnimatorDispatcher;
import net.lizistired.animationoverhaul.util.animation.BakedPose;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemFeatureRenderer.class)
public abstract class MixinItemInHandLayer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    public MixinItemInHandLayer(FeatureRendererContext<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/ModelWithArms;setArmAngle(Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;)V"))
    protected void transformItemInHandLayer(LivingEntity livingEntity, ItemStack itemStack, ModelTransformation.Mode transformType, Arm humanoidArm, MatrixStack poseStack, VertexConsumerProvider multiBufferSource, int i, CallbackInfo ci){
        if(shouldTransformItemInHand(livingEntity, itemStack)){
            // +
            poseStack.translate((humanoidArm == Arm.LEFT ? 1 : -1) /16F, 0, 0);
            String locatorIdentifier = humanoidArm == Arm.LEFT ? "leftHand" : "rightHand";
            AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUuid()).getLocator(locatorIdentifier, MinecraftClient.getInstance().getTickDelta()).translateAndRotatePoseStack(poseStack);
            // +
        }
    }
    private boolean shouldTransformItemInHand(LivingEntity livingEntity, ItemStack itemStack){
        if(itemStack.isEmpty()){
            return false;
        }
        BakedPose bakedPose = AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUuid());
        if(bakedPose != null){
            return bakedPose.containsLocator("leftHand") && bakedPose.containsLocator("rightHand");
        }
        return false;
    }
}
