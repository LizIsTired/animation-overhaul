package net.lizistired.animationoverhaul.mixin.debug;

import net.lizistired.animationoverhaul.AnimationOverhaulMain;
import net.lizistired.animationoverhaul.animations.AnimatorDispatcher;
import net.lizistired.animationoverhaul.util.data.EntityAnimationData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.hit.HitResult;

@Mixin(DebugHud.class)
public abstract class MixinDebugScreenOverlay extends DrawableHelper {

    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private TextRenderer textRenderer;

    @Shadow @Final private static int TEXT_COLOR;

    @Shadow protected abstract int interpolateColor(int i, int j, float f);

    @Shadow private HitResult blockHit;

    @Shadow private HitResult fluidHit;

    @Inject(method = "renderRightText", at = @At("HEAD"), cancellable = true)
    private void drawTimerDebugInfo(MatrixStack poseStack, CallbackInfo ci){

        if(this.client.options.getFov().getValue() == 72){
            poseStack.translate(this.client.getWindow().getScaledWidth() / 4F, 0, 0);
            poseStack.scale(0.75F, 0.75F, 0.75F);

            drawTimerDebug(poseStack);

            poseStack.scale(1/0.75F, 1/0.75F, 1/0.75F);
            poseStack.translate(-this.client.getWindow().getScaledWidth() / 4F, 0, 0);
            ci.cancel();
        }
    }

    private void drawTimerDebug(MatrixStack poseStack){
        boolean shouldRenderDebugTimers = true;
        Entity entity = AnimationOverhaulMain.debugEntity;

        entity = client.player;

        for(Entity entity1 : client.world.getEntities()){
            if(entity1.getType() == EntityType.CREEPER){
                entity = entity1;
            }
        }

        if(entity != null){

            EntityAnimationData entityAnimationData = AnimatorDispatcher.INSTANCE.getEntityAnimationData(entity);
            TreeMap<String, EntityAnimationData.Data<?>> debugData = entityAnimationData.getDebugData();

            DecimalFormat format = new DecimalFormat("0.00");
            if(debugData.size() > 0){

                for(int i = 0; i < debugData.size(); i++){
                    String key = debugData.keySet().stream().toList().get(i);
                    EntityAnimationData.Data<?> data = debugData.get(key);

                    boolean isFloat = data.get() instanceof Float;
                    boolean isWithinRange = false;
                    if(isFloat){
                        float value = (float) data.get();
                        isWithinRange = value <= 1 && value >= 0;
                    }

                    String string;
                    int j = 9;
                    int m = 2 + j * i;
                    m += j + 4;
                    if(isWithinRange && !this.client.options.debugTpsEnabled && isFloat){
                        string = key;
                    } else if(isFloat) {
                        string = key + " " + format.format(data.get());
                    } else {
                        string = key + " " + data.get().toString();
                    }
                    int k = this.textRenderer.getWidth(isWithinRange ? string + " 0.00" : string);
                    int l = this.client.getWindow().getScaledWidth() - 2 - k;
                    Objects.requireNonNull(this.textRenderer);
                    fill(poseStack, l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
                    this.textRenderer.draw(poseStack, string, (float)l, (float)m, TEXT_COLOR);

                    if(isWithinRange){
                        float value = (float) data.get();
                        k = this.textRenderer.getWidth("0.00");
                        k *= value;
                        l = this.client.getWindow().getScaledWidth() - 2 - k;
                        int k2 = (int) (k / value);
                        int l2 = this.client.getWindow().getScaledWidth() - 2 - k;
                        fill(poseStack, l - 1, m, l + k, m + j - 2, -2);
                        fill(poseStack, l2 - 1, m, l2 + k2, m + j - 2, TEXT_COLOR);
                    }
                }


                String string = "Selected entity: " + entity.getName().getString() + " (" + entity.getType().getUntranslatedName() + ")";
                int j = 9;
                int m = 2;
                int k = this.textRenderer.getWidth(string);
                int l = this.client.getWindow().getScaledWidth() - 2 - k;
                Objects.requireNonNull(this.textRenderer);
                fill(poseStack, l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
                this.textRenderer.draw(poseStack, string, (float)l, (float)m, TEXT_COLOR);

            } else {
                String string = "Animation timers not initiated!";
                Objects.requireNonNull(this.textRenderer);
                int j = 9;
                int k = this.textRenderer.getWidth(string);
                int l = this.client.getWindow().getScaledWidth() - 2 - k;
                int m = 2;
                fill(poseStack, l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
                this.textRenderer.draw(poseStack, string, (float)l, (float)m, TEXT_COLOR);
            }
        }
    }
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void testCompassGadget(MatrixStack poseStack, CallbackInfo ci){
        if(this.client.options.getFov().getValue() == 73){
            ci.cancel();
        }
    }
}
