package net.xxlenderchest.platform;

import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.Set;

public record TestPlatform(Path configDirectory, String loaderId, Set<String> mods, Set<String> permissions)
        implements LoaderPlatform {
    @Override
    public String modVersion() {
        return "1.1.8";
    }

    @Override
    public String minecraftVersion() {
        return "26.3";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return mods.contains(modId);
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String permission) {
        return permissions.contains(permission);
    }

    @Override
    public boolean isNewerVersion(String candidate, String current) {
        throw new UnsupportedOperationException("Version comparison is loader-specific.");
    }
}
