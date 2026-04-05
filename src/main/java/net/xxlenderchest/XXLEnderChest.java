package net.xxlenderchest;

import net.xxlenderchest.command.XXLCommand;
import net.xxlenderchest.config.XXLConfig;
import net.xxlenderchest.config.XXLConfigManager;
import net.xxlenderchest.permission.PermissionHelper;
import net.xxlenderchest.util.ModrinthUpdateChecker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for the <strong>XXL Enderchest</strong> Fabric mod.
 *
 * <p>This mod expands the ender chest inventory from the vanilla 3 rows (27 slots)
 * to up to 6 rows (54 slots), configurable via {@code config/xxlenderchest.json}.</p>
 *
 * <h2>Commands</h2>
 * <ul>
 *   <li>{@code /xxlenderchest info}   – Shows the current mod status and permission mode.</li>
 *   <li>{@code /xxlenderchest reload} – Reloads the config file from disk (OP only).</li>
 * </ul>
 *
 * <h2>Config options</h2>
 * <pre>
 * {
 *   "enabled": true,       // false = vanilla ender chest behavior (3 rows)
 *   "useLuckPerms": false, // true = use permission nodes for 4/5/6 rows
 *   "rows": 6              // fallback row count when LuckPerms is not used
 * }
 * </pre>
 *
 * <h2>Technical design</h2>
 * <p>Two mixins handle the expansion:</p>
 * <ol>
 *   <li>{@code PlayerEnderChestContainerMixin} – Always allocates 54 slots in the
 *       player's ender chest container so items in higher rows are never discarded
 *       when the row count is reduced.</li>
 *   <li>{@code EnderChestBlockMixin} – Intercepts the ender chest block interaction to
 *       open a {@code ChestMenu} with the correct number of rows.</li>
 * </ol>
 */
public class XXLEnderChest implements ModInitializer {

    /** The mod ID, must match {@code fabric.mod.json}. */
    public static final String MOD_ID = "xxlenderchest";
    public static final String MOD_NAME = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getName())
            .orElse("XXL Enderchest");

    /** Shared logger instance for all classes in this mod. */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // -------------------------------------------------------------------------
    // Config management (static so mixins can access it without an instance ref)
    // -------------------------------------------------------------------------

    private static XXLConfigManager configManager;
    private static XXLConfig config;

    /** Returns the currently active configuration. Never {@code null} after mod init. */
    public static XXLConfig getConfig() {
        return config;
    }

    /**
     * Returns a detached copy of the current config for client-side GUI editing.
     */
    public static XXLConfig loadConfigForEditing() {
        XXLConfig editableConfig = new XXLConfig();
        editableConfig.setEnabled(config.isEnabled());
        editableConfig.setUseLuckPerms(config.isUseLuckPerms());
        editableConfig.setRows(config.getRows());
        return editableConfig;
    }

    /**
     * Reloads the config from disk and updates the static reference.
     * Called by {@code /xxlenderchest reload}.
     */
    public static void reloadConfig() {
        config = configManager.load();
        logPermissionMode();
    }

    /**
     * Validates and persists an edited config, then updates the active runtime copy.
     */
    public static void applyEditedConfig(XXLConfig editedConfig) {
        editedConfig.validate();
        configManager.save(editedConfig);
        config = editedConfig;
        logPermissionMode();
    }

    // -------------------------------------------------------------------------
    // ModInitializer
    // -------------------------------------------------------------------------

    @Override
    public void onInitialize() {
        // Load config on startup
        configManager = new XXLConfigManager();
        config = configManager.load();
        logPermissionMode();

        // Register the /xxlenderchest command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                XXLCommand.register(dispatcher)
        );

        ServerLifecycleEvents.SERVER_STARTED.register(server -> ModrinthUpdateChecker.checkOnceAsync());

        LOGGER.info("[{}] Mod initialized. Version: {}", MOD_NAME, getModVersion());
        LOGGER.debug("[{}] Runtime config after initialization: rows={}, enabled={}",
                MOD_NAME, config.getRows(), config.isEnabled());
    }

    private static void logPermissionMode() {
        if (config.isUseLuckPerms() && !PermissionHelper.isLuckPermsAvailable()) {
            LOGGER.warn("[{}] useLuckPerms is true, but LuckPerms is not installed. Falling back to config rows.", MOD_NAME);
            return;
        }

        if (PermissionHelper.isUsingLuckPerms(config)) {
            LOGGER.debug("[{}] LuckPerms row permissions enabled.", MOD_NAME);
            return;
        }

        LOGGER.debug("[{}] Config row mode enabled.", MOD_NAME);
    }

    public static String getLogPrefix() {
        return "[" + MOD_NAME + "]";
    }

    public static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}
