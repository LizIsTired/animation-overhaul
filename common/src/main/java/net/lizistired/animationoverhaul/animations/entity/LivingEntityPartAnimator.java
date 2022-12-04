package net.lizistired.animationoverhaul.animations.entity;

import net.lizistired.animationoverhaul.access.ModelAccess;
import net.lizistired.animationoverhaul.animations.AnimatorDispatcher;
import net.lizistired.animationoverhaul.util.animation.BakedPose;
import net.lizistired.animationoverhaul.util.animation.Locator;
import net.lizistired.animationoverhaul.util.animation.LocatorRig;
import net.lizistired.animationoverhaul.util.data.EntityAnimationData;
import net.lizistired.animationoverhaul.util.data.TimelineGroupData;
import net.lizistired.animationoverhaul.util.time.Easing;
import java.util.List;
import java.util.Random;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

public class LivingEntityPartAnimator<T extends LivingEntity, M extends EntityModel<T>> {

    protected T livingEntity;
    protected M entityModel;
    protected final LocatorRig locatorRig;

    protected EntityAnimationData entityAnimationData;
    protected final Random random = new Random();

    public LivingEntityPartAnimator(){
        this.locatorRig = new LocatorRig();
        buildRig(this.locatorRig);
    }

    public void setEntity(T livingEntity){
        this.livingEntity = livingEntity;
    }

    public void setEntityModel(M entityModel){
        this.entityModel = entityModel;
    }

    protected void buildRig(LocatorRig locatorRig){

    }

    public void tick(LivingEntity livingEntity, EntityAnimationData entityAnimationData){

    }

    protected void poseLocatorRig(){

    }

    protected void finalizeModelParts(ModelPart rootModelPart){
    }

    public void tickMethods(LivingEntity livingEntity){
        BakedPose bakedPose = AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUuid());
        EntityAnimationData entityAnimationData = AnimatorDispatcher.INSTANCE.getEntityAnimationData(livingEntity.getUuid());
        this.entityAnimationData = entityAnimationData;
        this.livingEntity = (T)livingEntity;

        this.tick(livingEntity, entityAnimationData);

        if(!bakedPose.hasPose){
            bakedPose.setPose(this.locatorRig.bakePose());
            bakedPose.hasPose = true;
        }
        bakedPose.pushToOld();

        this.locatorRig.resetRig();
        this.poseLocatorRig();
        this.locatorRig.applyOffsets();

        bakedPose.setPose(locatorRig.bakePose());
        AnimatorDispatcher.INSTANCE.saveBakedPose(livingEntity.getUuid(), bakedPose);
    }

    public void animate(T livingEntity, M entityModel, MatrixStack poseStack, EntityAnimationData entityAnimationData, float partialTicks){
        setEntity(livingEntity);
        setEntityModel(entityModel);

        BakedPose bakedPose = AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUuid());

        ModelPart rootModelPart = getRoot(entityModel);
        bakedPose.bakeToModelParts(rootModelPart, partialTicks);
        finalizeModelParts(rootModelPart);

    }

    protected ModelPart getRoot(M entityModel){
        return ((ModelAccess)entityModel).getRootModelPart();
    }

    /**
     * Shortcut method for calling a timeline group
     * <p>
     * For use within the scope of {@link #poseLocatorRig()}
     * @param identifier    Mod identifier for the mod adding the timeline group
     * @param animationKey  Animation key used to identify the timeline group
     * @return              Timeline group object used for posing locator rigs
     */
    protected TimelineGroupData.TimelineGroup getTimelineGroup(String identifier, String animationKey){
        return TimelineGroupData.INSTANCE.get(identifier, this.livingEntity.getType(), animationKey);
    }

    /**
     * Shortcut method for retrieving a data value at the current tick
     * <p>
     * For use within the scope of {@link #poseLocatorRig()}
     * @param dataKey       Data key used to obtain the object from the entity animation data
     * @return              Object specified in data key object type
     */
    protected <D> D getDataValue(EntityAnimationData.DataKey<D> dataKey){
        return this.entityAnimationData.getValue(dataKey);
    }

    /**
     * Shortcut method for retrieving an interpolated float value at the current frame
     * <p>
     * For use within the scope of {@link #poseLocatorRig()}
     * @param dataKey       Float data key to obtain the value with
     * @return              Interpolated float for values inbetween ticks
     */

    /**
     * Shortcut method for retrieving an interpolated float value at the current frame and then applying it to an easing
     * <p>
     * For use within the scope of {@link #poseLocatorRig()}
     *
     * @param dataKey       Float data key to obtain the value with
     * @param easing        Easing object to apply the interpolated float onto
     * @return              Interpolated float for values inbetween ticks
     */
    protected float getDataValueEased(EntityAnimationData.DataKey<Float> dataKey, Easing easing){
        return this.entityAnimationData.getEased(dataKey, easing, 1);
    }

    protected float getDataValueEasedQuad(EntityAnimationData.DataKey<Float> dataKey){
        return getDataValueEased(dataKey, Easing.CubicBezier.bezierInOutQuad());
    }

    protected float getDataValueEasedCondition(EntityAnimationData.DataKey<Float> dataKey, Easing easing1, Easing easing2, boolean condition){
        return getDataValueEased(dataKey, condition ? easing1 : easing2);
    }

    /**
     * Shortcut method for obtaining whether the current entity is left-handed or not
     * <p>
     * For use within the scope of {@link #poseLocatorRig()}
     */
    protected boolean isLeftHanded(){
        return this.livingEntity.getMainArm() == Arm.LEFT;
    }

    protected float getLookLeftRightTimer(EntityAnimationData.DataKey<Float> dataKey){
        return MathHelper.clamp((getDataValue(dataKey) / MathHelper.HALF_PI) + 0.5F, 0, 1);
    }

    protected float getLookUpDownTimer(EntityAnimationData.DataKey<Float> dataKey){
        return MathHelper.clamp((getDataValue(dataKey) / MathHelper.PI) + 0.5F, 0, 1);
    }

    protected static final EntityAnimationData.DataKey<Float> HEAD_X_ROT = new EntityAnimationData.DataKey<>("head_x_rot", 0F);
    protected static final EntityAnimationData.DataKey<Float> HEAD_Y_ROT = new EntityAnimationData.DataKey<>("head_y_rot", 0F);
    protected static final EntityAnimationData.DataKey<Float> LEAN_X_ROT = new EntityAnimationData.DataKey<>("lean_x_rot", 0F);
    protected static final EntityAnimationData.DataKey<Float> LEAN_Y_ROT = new EntityAnimationData.DataKey<>("lean_y_rot", 0F);
    protected static final EntityAnimationData.DataKey<Float> DELTA_Y = new EntityAnimationData.DataKey<>("delta_y", 0F);
    protected static final EntityAnimationData.DataKey<Float> DELTA_Y_OLD = new EntityAnimationData.DataKey<>("delta_y_old", 0F);
    protected static final EntityAnimationData.DataKey<Float> ANIMATION_SPEED = new EntityAnimationData.DataKey<>("animation_speed", 0F);
    protected static final EntityAnimationData.DataKey<Float> ANIMATION_SPEED_Y = new EntityAnimationData.DataKey<>("animation_speed_y", 0F);
    protected static final EntityAnimationData.DataKey<Float> ANIMATION_SPEED_XYZ = new EntityAnimationData.DataKey<>("animation_speed_xyz", 0F);
    protected static final EntityAnimationData.DataKey<Float> ANIMATION_POSITION = new EntityAnimationData.DataKey<>("animation_position", 0F);
    protected static final EntityAnimationData.DataKey<Float> ANIMATION_POSITION_Y = new EntityAnimationData.DataKey<>("animation_position_y", 0F);
    protected static final EntityAnimationData.DataKey<Float> ANIMATION_POSITION_XYZ = new EntityAnimationData.DataKey<>("animation_position_xyz", 0F);
    protected static final EntityAnimationData.DataKey<Float> HURT_TIMER = new EntityAnimationData.DataKey<>("hurt_timer", 0F);
    protected static final EntityAnimationData.DataKey<Integer> HURT_INDEX = new EntityAnimationData.DataKey<>("hurt_index", 0);
    protected static final EntityAnimationData.DataKey<Float> DEATH_TIMER = new EntityAnimationData.DataKey<>("death_timer", 0F);
    protected static final EntityAnimationData.DataKey<Integer> DEATH_INDEX = new EntityAnimationData.DataKey<>("death_index", 0);
    protected static final EntityAnimationData.DataKey<Float> SLEEP_TIMER = new EntityAnimationData.DataKey<>("sleep_timer", 0F);
    protected static final EntityAnimationData.DataKey<Float> FLYING_SPEED = new EntityAnimationData.DataKey<>("flying_speed", 0F);
    protected static final EntityAnimationData.DataKey<Float> FLYING_POSITION = new EntityAnimationData.DataKey<>("flying_position", 0F);
    public static final EntityAnimationData.DataKey<Float> BODY_Y_ROT = new EntityAnimationData.DataKey<>("body_y_rot", 0F);
    protected static final EntityAnimationData.DataKey<Float> TURNING_LEFT_WEIGHT = new EntityAnimationData.DataKey<>("turning_left_weight", 0F);
    protected static final EntityAnimationData.DataKey<Float> TURNING_RIGHT_WEIGHT = new EntityAnimationData.DataKey<>("turning_right_weight", 0F);

    protected void tickBodyRotationTimersNormal(LivingEntity livingEntity, EntityAnimationData entityAnimationData){
        entityAnimationData.setValue(BODY_Y_ROT, (float) MathHelper.lerpAngleDegrees(0.75f, entityAnimationData.getValue(BODY_Y_ROT), livingEntity.bodyYaw));
    }

    protected void tickGeneralMovementTimers(LivingEntity livingEntity, EntityAnimationData entityAnimationData){
        float deltaY = (float) (livingEntity.getY() - livingEntity.prevY);
        float deltaYOld = entityAnimationData.getValue(DELTA_Y);
        entityAnimationData.setValue(DELTA_Y, deltaY);
        entityAnimationData.setValue(DELTA_Y_OLD, deltaYOld);
        tickAnimationSpeedTimers(livingEntity, entityAnimationData);
    }

    private void tickAnimationSpeedTimers(LivingEntity livingEntity, EntityAnimationData entityAnimationData){
        boolean useVerticalVector = livingEntity instanceof Flutterer;

        float previousAnimationSpeed = entityAnimationData.getValue(ANIMATION_SPEED);
        float previousFlyingSpeed =  entityAnimationData.getValue(FLYING_SPEED);
        float previousAnimationSpeedY =  entityAnimationData.getValue(ANIMATION_SPEED_Y);
        float previousAnimationSpeedXYZ =  entityAnimationData.getValue(ANIMATION_SPEED_XYZ);
        float previousAnimationPosition =  entityAnimationData.getValue(ANIMATION_POSITION);
        float previousFlyingPosition =  entityAnimationData.getValue(FLYING_POSITION);
        float previousAnimationPositionY =  entityAnimationData.getValue(ANIMATION_POSITION_Y);
        float previousAnimationPositionXYZ =  entityAnimationData.getValue(ANIMATION_POSITION_XYZ);

        double deltaX = livingEntity.getX() - livingEntity.prevX;
        double deltaY = livingEntity.getY() - livingEntity.prevY;
        double deltaZ = livingEntity.getZ() - livingEntity.prevZ;
        float movementSquared = (float)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 4.0F;
        float movementSquaredY = (float)Math.sqrt(deltaY * deltaY) * 4.0F;
        float movementSquaredXYZ = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 4.0F;
        float movementSquaredFlying = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 1.5F;
        if (movementSquared > 1.0F) {
            movementSquared = 1.0F;
        }
        if (movementSquaredY > 1.0F) {
            movementSquaredY = 1.0F;
        }
        if (movementSquaredXYZ > 1.0F) {
            movementSquaredXYZ = 1.0F;
        }
        if (movementSquaredFlying > 1.0F) {
            movementSquaredFlying = 1.0F;
        }

        float finalAnimationSpeed = previousAnimationSpeed + ((movementSquared - previousAnimationSpeed) * 0.4F);
        float finalAnimationSpeedY = previousAnimationSpeedY + ((movementSquaredY - previousAnimationSpeedY) * 0.4F);
        float finalAnimationSpeedXYZ = previousAnimationSpeedXYZ + ((movementSquaredXYZ - previousAnimationSpeedXYZ) * 0.4F);
        float finalAnimationSpeedFlying = previousFlyingSpeed + ((movementSquaredFlying - previousFlyingSpeed) * 0.4F);
        entityAnimationData.setValue(ANIMATION_SPEED, finalAnimationSpeed);
        entityAnimationData.setValue(ANIMATION_SPEED_Y, finalAnimationSpeedY);
        entityAnimationData.setValue(ANIMATION_SPEED_XYZ, finalAnimationSpeedXYZ);
        entityAnimationData.setValue(FLYING_SPEED, finalAnimationSpeedFlying);
        entityAnimationData.setValue(ANIMATION_POSITION, previousAnimationPosition + finalAnimationSpeed);
        entityAnimationData.setValue(ANIMATION_POSITION_Y, previousAnimationPositionY + finalAnimationSpeedY);
        entityAnimationData.setValue(ANIMATION_POSITION_XYZ, previousAnimationPositionXYZ + finalAnimationSpeedXYZ);
        entityAnimationData.setValue(FLYING_POSITION, previousFlyingPosition + finalAnimationSpeedFlying);
    }

    protected void tickBodyYRotTimers(LivingEntity livingEntity, EntityAnimationData entityAnimationData){
        float previousBodyYRot = entityAnimationData.getValue(BODY_Y_ROT);
        float targetBodyYRot = livingEntity.bodyYaw;
        boolean isMoving = entityAnimationData.getValue(ANIMATION_SPEED_XYZ) > 0.05F
                || livingEntity.isInSwimmingPose()
                || livingEntity.getRoll() > 0;
        float increment = (isMoving ? 20 : 8);

        float difference = MathHelper.abs(targetBodyYRot - previousBodyYRot);
        float newBodyYRot;
        boolean turningLeft = false;
        boolean turningRight = false;

        if(difference < increment){
            newBodyYRot = targetBodyYRot;
        } else {
            if(targetBodyYRot < previousBodyYRot){
                turningLeft = !isMoving;
            } else {
                turningRight = !isMoving;
            }
            float additional360 = 0;
            newBodyYRot = targetBodyYRot < previousBodyYRot ? previousBodyYRot - increment - additional360 : previousBodyYRot + increment + additional360;
        }
        entityAnimationData.incrementInTicksFromCondition(TURNING_LEFT_WEIGHT, turningLeft, 8, 8);
        entityAnimationData.incrementInTicksFromCondition(TURNING_RIGHT_WEIGHT, turningRight, 8, 8);
        entityAnimationData.setValue(BODY_Y_ROT, newBodyYRot);
    }

    protected void tickHeadTimers(LivingEntity livingEntity, EntityAnimationData entityAnimationData){
        float h = livingEntity.bodyYaw;
        float j = livingEntity.headYaw;
        float k = j - h;
        float o;
        if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof LivingEntity livingEntity2) {
            h = livingEntity2.bodyYaw;
            k = j - h;
            o = MathHelper.wrapDegrees(k);
            if (o < -85.0F) {
                o = -85.0F;
            }

            if (o >= 85.0F) {
                o = 85.0F;
            }

            h = j - o;
            if (o * o > 2500.0F) {
                h += o * 0.2F;
            }

            k = j - h;
        }

        entityAnimationData.setValue(HEAD_X_ROT, (float) MathHelper.lerp(0.75F, entityAnimationData.getValue(HEAD_X_ROT), Math.toRadians(livingEntity.getPitch())));
        entityAnimationData.setValue(HEAD_Y_ROT, (float) MathHelper.lerp(0.75F, entityAnimationData.getValue(HEAD_Y_ROT), Math.toRadians(k)));
    }

    protected void tickLeanTimers(EntityAnimationData entityAnimationData){
        entityAnimationData.setValue(LEAN_X_ROT, MathHelper.lerp(0.25F, entityAnimationData.getValue(LEAN_X_ROT), entityAnimationData.getValue(HEAD_X_ROT)));
        entityAnimationData.setValue(LEAN_Y_ROT, MathHelper.lerp(0.25F, entityAnimationData.getValue(LEAN_Y_ROT), entityAnimationData.getValue(HEAD_Y_ROT)));
    }

    protected void tickHurtTimers(LivingEntity livingEntity, EntityAnimationData entityAnimationData, int numberOfTimers){
        entityAnimationData.incrementInTicksOrResetRandomFromCondition(HURT_TIMER, HURT_INDEX, numberOfTimers, livingEntity.hurtTime == 10, 10, this.random);
    }

    protected void tickDeathTimer(LivingEntity livingEntity, EntityAnimationData entityAnimationData){
        entityAnimationData.incrementInTicksOrResetFromCondition(DEATH_TIMER, livingEntity.deathTime == 0, 19);
    }

    protected void tickSleepTimer(LivingEntity livingEntity, EntityAnimationData entityAnimationData){
        entityAnimationData.incrementInFramesOrResetFromCondition(SLEEP_TIMER, livingEntity.getPose() != EntityPose.SLEEPING, 24);
    }

    protected void addPoseLayerHurt(List<TimelineGroupData.TimelineGroup> timelineGroupList, List<Locator> locatorList){
        this.locatorRig.animateMultipleLocatorsAdditive(locatorList, timelineGroupList.get(this.entityAnimationData.getValue(HURT_INDEX)), getDataValue(HURT_TIMER), 1, false);
    }

    // Add after everything but before damage animations
    protected void addPoseLayerDeath(TimelineGroupData.TimelineGroup timelineGroup, List<Locator> locatorList){
        this.locatorRig.weightedClearTransforms(locatorList, Math.min(this.entityAnimationData.getValue(DEATH_TIMER) * 4, 1));
        this.locatorRig.animateMultipleLocatorsAdditive(locatorList, timelineGroup, getDataValue(DEATH_TIMER), 1, false);
    }
}
