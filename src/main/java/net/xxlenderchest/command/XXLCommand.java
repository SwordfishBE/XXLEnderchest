package net.xxlenderchest.command;

import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.config.XXLConfig;
import net.xxlenderchest.permission.PermissionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Registers and handles the /xxlenderchest command.
 *
 * Sub-commands:
 *   /xxlenderchest info   - Shows the current mod state (OP Gamemaster level only).
 *   /xxlenderchest reload - Reloads the config from disk (OP Gamemaster level only).
 */
public class XXLCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("xxlenderchest")
                .then(Commands.literal("info")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(XXLCommand::executeInfo)
                )
                .then(Commands.literal("reload")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(XXLCommand::executeReload)
                )
        );
    }

    private static int executeInfo(CommandContext<CommandSourceStack> ctx) {
        XXLConfig config = XXLEnderChest.getConfig();
        boolean luckPermsLoaded = PermissionHelper.isLuckPermsAvailable();
        boolean usingLuckPerms = PermissionHelper.isUsingLuckPerms(config);

        String statusColor = config.isEnabled() ? "§a" : "§c";
        String status = config.isEnabled() ? "ENABLED" : "DISABLED";
        String luckPermsStatus = luckPermsLoaded ? "§aLOADED" : "§cMISSING";
        String permissionMode = usingLuckPerms ? "§bLuckPerms" : "§eConfig";

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§6[XXL Enderchest]§r Mod status: " + statusColor + status
        ), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§6[XXL Enderchest]§r Permission mode: " + permissionMode
                        + "§r | LuckPerms: " + luckPermsStatus
        ), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§6[XXL Enderchest]§r Fallback config rows: §e" + config.getRows()
                        + "§r (" + (config.getRows() * 9) + " slots)"
        ), false);

        if (usingLuckPerms) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6[XXL Enderchest]§r Nodes: §7"
                            + PermissionHelper.PERMISSION_ROW_4 + "§r, §7"
                            + PermissionHelper.PERMISSION_ROW_5 + "§r, §7"
                            + PermissionHelper.PERMISSION_ROW_6
            ), false);
        }

        return 1;
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        XXLEnderChest.reloadConfig();
        XXLConfig config = XXLEnderChest.getConfig();

        String statusColor = config.isEnabled() ? "§a" : "§c";
        String status      = config.isEnabled() ? "ENABLED" : "DISABLED";

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§6[XXL Enderchest]§r Config reloaded! " +
                "Status: " + statusColor + status +
                "§r | Rows: §e" + config.getRows() +
                "§r | Mode: " + (PermissionHelper.isUsingLuckPerms(config) ? "§bLuckPerms" : "§eConfig")
        ), true);

        XXLEnderChest.LOGGER.info(
                "{} Config reloaded via command.",
                XXLEnderChest.getLogPrefix()
        );
        XXLEnderChest.LOGGER.debug(
                "{} Config reloaded by {}. New state: {}",
                XXLEnderChest.getLogPrefix(),
                ctx.getSource().getTextName(),
                config
        );

        return 1;
    }
}
