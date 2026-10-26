package net.xxlenderchest.permission;

import net.xxlenderchest.XXLEnderChest;
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

    private PermissionHelper() {
    }

    public static boolean isLuckPermsAvailable() {
        return XXLEnderChest.getPlatform().isModLoaded("luckperms");
    }

    public static boolean isUsingLuckPerms(XXLConfig config) {
        return config.isEnabled() && config.isUseLuckPerms() && isLuckPermsAvailable();
    }

    public static int resolveRows(ServerPlayer player, XXLConfig config) {
        if (!config.isEnabled()) {
            return VANILLA_ROWS;
        }

        if (!isUsingLuckPerms(config)) {
            return config.getRows();
        }

        if (XXLEnderChest.getPlatform().hasPermission(player, PERMISSION_ROW_6)) {
            return 6;
        }
        if (XXLEnderChest.getPlatform().hasPermission(player, PERMISSION_ROW_5)) {
            return 5;
        }
        if (XXLEnderChest.getPlatform().hasPermission(player, PERMISSION_ROW_4)) {
            return 4;
        }

        return VANILLA_ROWS;
    }

    public static boolean canUseEnderChestCommand(ServerPlayer player, XXLConfig config) {
        if (!config.isCommandEnabled()) {
            return false;
        }

        if (!config.isUseLuckPerms() || !isLuckPermsAvailable()) {
            return true;
        }

        return XXLEnderChest.getPlatform().hasPermission(player, PERMISSION_COMMAND_ENDERCHEST);
    }
}
