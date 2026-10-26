package net.xxlenderchest.platform;

import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface LoaderPlatform {
    Path configDirectory();

    String loaderId();

    String modVersion();

    String minecraftVersion();

    boolean isModLoaded(String modId);

    boolean hasPermission(ServerPlayer player, String permission);

    boolean isNewerVersion(String candidate, String current);
}
