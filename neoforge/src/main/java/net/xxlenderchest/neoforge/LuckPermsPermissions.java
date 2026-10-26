package net.xxlenderchest.neoforge;

import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.level.ServerPlayer;
import net.xxlenderchest.XXLEnderChest;

/** Loaded only when LuckPerms is installed. */
final class LuckPermsPermissions {
    private LuckPermsPermissions() {
    }

    static boolean check(ServerPlayer player, String permission) {
        try {
            var luckPerms = LuckPermsProvider.get();
            var user = luckPerms.getUserManager().getUser(player.getUUID());
            if (user == null) {
                return false;
            }
            var contextManager = luckPerms.getContextManager();
            var queryOptions = contextManager.getQueryOptions(user).orElse(contextManager.getStaticQueryOptions());
            return user.getCachedData().getPermissionData(queryOptions).checkPermission(permission).asBoolean();
        } catch (IllegalStateException e) {
            XXLEnderChest.LOGGER.debug("{} LuckPerms is not ready; denying {}.", XXLEnderChest.getLogPrefix(), permission, e);
            return false;
        }
    }
}
