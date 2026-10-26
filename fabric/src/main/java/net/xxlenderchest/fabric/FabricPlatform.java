package net.xxlenderchest.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.server.level.ServerPlayer;
import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.platform.LoaderPlatform;

import java.nio.file.Path;

public final class FabricPlatform implements LoaderPlatform {
    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public String loaderId() {
        return "fabric";
    }

    @Override
    public String modVersion() {
        return versionOf(XXLEnderChest.MOD_ID);
    }

    @Override
    public String minecraftVersion() {
        return versionOf("minecraft");
    }

    private static String versionOf(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String permission) {
        return Permissions.check(player, permission, false);
    }

    @Override
    public boolean isNewerVersion(String candidate, String current) {
        try {
            return Version.parse(candidate).compareTo(Version.parse(current)) > 0;
        } catch (VersionParsingException e) {
            XXLEnderChest.LOGGER.debug("{} Invalid version comparison: {} / {}", XXLEnderChest.getLogPrefix(), candidate, current, e);
            return false;
        }
    }
}
