package net.xxlenderchest.neoforge;

import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.platform.LoaderPlatform;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.nio.file.Path;

public final class NeoForgePlatform implements LoaderPlatform {
    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String loaderId() {
        return "neoforge";
    }

    @Override
    public String modVersion() {
        return ModList.get().getModContainerById(XXLEnderChest.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString()).orElse("unknown");
    }

    @Override
    public String minecraftVersion() {
        return SharedConstants.getCurrentVersion().name();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String permission) {
        return isModLoaded("luckperms") && LuckPermsPermissions.check(player, permission);
    }

    @Override
    public boolean isNewerVersion(String candidate, String current) {
        return new ComparableVersion(candidate).compareTo(new ComparableVersion(current)) > 0;
    }
}
