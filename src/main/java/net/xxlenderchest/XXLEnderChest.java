package net.xxlenderchest;

import net.xxlenderchest.command.XXLCommand;
import net.xxlenderchest.config.XXLConfig;
import net.xxlenderchest.config.XXLConfigManager;
import net.xxlenderchest.permission.PermissionHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
     * Reloads the config from disk and updates the static reference.
     * Called by {@code /xxlenderchest reload}.
     */
    public static void reloadConfig() {
        config = configManager.load();
        logPermissionMode();
    }

    // -------------------------------------------------------------------------
    // ModInitializer
    // -------------------------------------------------------------------------

    @Override
    public void onInitialize() {
        LOGGER.info("[XXL Enderchest] Initializing...");

        // Load config on startup
        configManager = new XXLConfigManager();
        config = configManager.load();
        logPermissionMode();

        // Register the /xxlenderchest command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                XXLCommand.register(dispatcher)
        );

        LOGGER.info("[XXL Enderchest] Ready! Ender chest rows: {} (enabled: {})",
                config.getRows(), config.isEnabled());
    }

    private static void logPermissionMode() {
        if (config.isUseLuckPerms() && !PermissionHelper.isLuckPermsAvailable()) {
            LOGGER.warn("[XXL Enderchest] useLuckPerms is true, but LuckPerms is not installed. Falling back to config rows.");
            return;
        }

        if (PermissionHelper.isUsingLuckPerms(config)) {
            LOGGER.info("[XXL Enderchest] LuckPerms row permissions enabled.");
            return;
        }

        LOGGER.info("[XXL Enderchest] Config row mode enabled.");
    }
}
