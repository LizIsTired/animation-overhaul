package net.lizistired.animationoverhaul.access;

public interface BlockEntityAccess {
    float getAnimationVariable(String animationVariable);
    void setAnimationVariable(String animationVariable, float newValue);
}
