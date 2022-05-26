package net.lizistired.animationoverhaul.mixin.config;

import java.io.File;

import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({PlayerSkinProvider.class})
public interface MixinPlayerSkinProvider {
    @Accessor("skinCacheDir")
    File getSkinCacheDirectory();
}