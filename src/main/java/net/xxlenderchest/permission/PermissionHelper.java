package net.xxlenderchest.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.xxlenderchest.config.XXLConfig;

/**
 * Resolves XXL Enderchest row access with optional LuckPerms integration.
 */
public final class PermissionHelper {

    public static final String PERMISSION_ROW_4 = "xxlenderchest.rows.4";
    public static final String PERMISSION_ROW_5 = "xxlenderchest.rows.5";
    public static final String PERMISSION_ROW_6 = "xxlenderchest.rows.6";
    public static final String PERMISSION_COMMAND_ENDERCHEST = "xxlenderchest.command.enderchest";

    private static final int VANILLA_ROWS = 3;
    private static final boolean LUCKPERMS_AVAILABLE = FabricLoader.getInstance().isModLoaded("luckperms");

    private PermissionHelper() {
    }

    public static boolean isLuckPermsAvailable() {
        return LUCKPERMS_AVAILABLE;
    }

    public static boolean isUsingLuckPerms(XXLConfig config) {
        return config.isEnabled() && config.isUseLuckPerms() && LUCKPERMS_AVAILABLE;
    }

    public static int resolveRows(ServerPlayer player, XXLConfig config) {
        if (!config.isEnabled()) {
            return VANILLA_ROWS;
        }

        if (!isUsingLuckPerms(config)) {
            return config.getRows();
        }

        if (Permissions.check(player, PERMISSION_ROW_6, false)) {
            return 6;
        }
        if (Permissions.check(player, PERMISSION_ROW_5, false)) {
            return 5;
        }
        if (Permissions.check(player, PERMISSION_ROW_4, false)) {
            return 4;
        }

        return VANILLA_ROWS;
    }

    public static boolean canUseEnderChestCommand(ServerPlayer player, XXLConfig config) {
        if (!config.isCommandEnabled()) {
            return false;
        }

        if (!config.isUseLuckPerms() || !LUCKPERMS_AVAILABLE) {
            return true;
        }

        return Permissions.check(player, PERMISSION_COMMAND_ENDERCHEST, false);
    }
}
