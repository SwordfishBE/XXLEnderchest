package net.xxlenderchest.permission;

import net.minecraft.server.level.ServerPlayer;
import net.xxlenderchest.config.XXLConfig;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Resolves XXL Enderchest row access with optional LuckPerms integration.
 */
public final class PermissionHelper {

    public static final String PERMISSION_ROW_4 = "xxlenderchest.rows.4";
    public static final String PERMISSION_ROW_5 = "xxlenderchest.rows.5";
    public static final String PERMISSION_ROW_6 = "xxlenderchest.rows.6";

    private static final int VANILLA_ROWS = 3;
    private static final boolean LUCKPERMS_AVAILABLE = isLuckPermsPresent();

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

        if (hasPermission(player.getUUID(), PERMISSION_ROW_6)) {
            return 6;
        }
        if (hasPermission(player.getUUID(), PERMISSION_ROW_5)) {
            return 5;
        }
        if (hasPermission(player.getUUID(), PERMISSION_ROW_4)) {
            return 4;
        }

        return VANILLA_ROWS;
    }

    private static boolean isLuckPermsPresent() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean hasPermission(UUID uuid, String node) {
        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object luckPerms = providerClass.getMethod("get").invoke(null);

            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Method getUser = userManager.getClass().getMethod("getUser", UUID.class);
            Object user = getUser.invoke(userManager, uuid);

            if (user == null) {
                Method loadUser = userManager.getClass().getMethod("loadUser", UUID.class);
                @SuppressWarnings("unchecked")
                CompletableFuture<Object> future = (CompletableFuture<Object>) loadUser.invoke(userManager, uuid);
                user = future.join();
            }

            if (user == null) {
                return false;
            }

            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object permissionData = cachedData.getClass().getMethod("getPermissionData").invoke(cachedData);
            Object tristate = permissionData.getClass().getMethod("checkPermission", String.class).invoke(permissionData, node);

            return "TRUE".equalsIgnoreCase(tristate.toString());
        } catch (Throwable ignored) {
            return false;
        }
    }
}
