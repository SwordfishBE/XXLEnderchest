package net.xxlenderchest.command;

import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.config.XXLConfig;
import net.xxlenderchest.permission.PermissionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

/**
 * Registers and handles the /xxlenderchest command.
 *
 * Sub-commands:
 *   /xxlenderchest info   - Shows the current mod state (OP Gamemaster level only).
 *   /xxlenderchest reload - Reloads the config from disk (OP Gamemaster level only).
 *   /enderchest           - Opens the player's own ender chest when enabled in config.
 */
public class XXLCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("enderchest")
                .requires(source -> source.getEntity() instanceof ServerPlayer player
                        && PermissionHelper.canUseEnderChestCommand(player, XXLEnderChest.getConfig()))
                .executes(XXLCommand::executeOpenEnderChest)
        );

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
        String commandStatus = config.isCommandEnabled() ? "§aENABLED" : "§cDISABLED";

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
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§6[XXL Enderchest]§r /enderchest command: " + commandStatus
        ), false);

        if (usingLuckPerms) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6[XXL Enderchest]§r Nodes: §7"
                            + PermissionHelper.PERMISSION_ROW_4 + "§r, §7"
                            + PermissionHelper.PERMISSION_ROW_5 + "§r, §7"
                            + PermissionHelper.PERMISSION_ROW_6 + "§r, §7"
                            + PermissionHelper.PERMISSION_COMMAND_ENDERCHEST
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

    private static int executeOpenEnderChest(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        XXLConfig config = XXLEnderChest.getConfig();
        int rows = PermissionHelper.resolveRows(player, config);
        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();

        player.openMenu(new SimpleMenuProvider(
                (syncId, playerInventory, ignoredPlayer) -> new ChestMenu(
                        rowsToMenuType(rows),
                        syncId,
                        playerInventory,
                        enderChest,
                        rows
                ),
                Component.translatable("container.enderchest")
        ));

        return 1;
    }

    private static MenuType<?> rowsToMenuType(int rows) {
        return switch (rows) {
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }
}
