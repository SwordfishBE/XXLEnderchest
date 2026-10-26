package net.xxlenderchest;

import net.xxlenderchest.config.XXLConfig;
import net.xxlenderchest.config.XXLConfigManager;
import net.xxlenderchest.permission.PermissionHelper;
import net.xxlenderchest.platform.LoaderPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared initialization and configuration for both loaders. */
public final class XXLEnderChest {
    public static final String MOD_ID = "xxlenderchest";
    public static final String MOD_NAME = "XXL Enderchest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static LoaderPlatform platform;
    private static XXLConfigManager configManager;
    private static volatile XXLConfig config;

    private XXLEnderChest() {
    }

    public static void initialize(LoaderPlatform loaderPlatform) {
        platform = loaderPlatform;
        configManager = new XXLConfigManager();
        config = configManager.load();
        logPermissionMode();
        LOGGER.info("[{}] Mod initialized on {}. Version: {}", MOD_NAME, platform.loaderId(), getModVersion());
    }

    public static LoaderPlatform getPlatform() {
        if (platform == null) {
            throw new IllegalStateException("XXL Enderchest has not been initialized by its loader.");
        }
        return platform;
    }

    public static XXLConfig getConfig() {
        return config;
    }

    public static XXLConfig loadConfigForEditing() {
        return config.copy();
    }

    public static synchronized void reloadConfig() {
        config = configManager.load();
        logPermissionMode();
    }

    public static synchronized void applyEditedConfig(XXLConfig editedConfig) {
        XXLConfig snapshot = editedConfig.copy();
        snapshot.validate();
        if (!configManager.save(snapshot)) {
            LOGGER.error("[{}] Edited config was not applied because saving to disk failed.", MOD_NAME);
            return;
        }
        config = snapshot;
        logPermissionMode();
    }

    private static void logPermissionMode() {
        if (config.isUseLuckPerms() && !PermissionHelper.isLuckPermsAvailable()) {
            LOGGER.warn("[{}] useLuckPerms is true, but LuckPerms is not installed. Falling back to config rows.", MOD_NAME);
        } else {
            LOGGER.debug("[{}] Row mode: {}", MOD_NAME,
                    PermissionHelper.isUsingLuckPerms(config) ? "LuckPerms" : "Config");
        }
    }

    public static String getLogPrefix() {
        return "[" + MOD_NAME + "]";
    }

    public static String getModVersion() {
        return getPlatform().modVersion();
    }
}
