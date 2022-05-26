package net.lizistired.animationoverhaul.util.config;

import java.nio.file.Path;

import net.lizistired.animationoverhaul.mixin.config.MixinPlayerSkinProvider;
import net.minecraft.client.MinecraftClient;

public class GamePaths {
    private GamePaths() {
    }

    public static Path getGameDirectory() {
        return MinecraftClient.getInstance().runDirectory.toPath();
    }
    public static Path getConfigDirectory() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("config");
    }

    public static Path getAssetsDirectory() {
        return ((MixinPlayerSkinProvider)MinecraftClient.getInstance().getSkinProvider()).getSkinCacheDirectory().getParentFile().toPath();
    }
}
