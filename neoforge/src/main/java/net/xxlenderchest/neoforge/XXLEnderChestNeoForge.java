package net.xxlenderchest.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.command.XXLCommand;
import net.xxlenderchest.util.ModrinthUpdateChecker;

@Mod(XXLEnderChest.MOD_ID)
public final class XXLEnderChestNeoForge {
    public XXLEnderChestNeoForge() {
        XXLEnderChest.initialize(new NeoForgePlatform());
        NeoForge.EVENT_BUS.addListener(XXLEnderChestNeoForge::registerCommands);
        NeoForge.EVENT_BUS.addListener(XXLEnderChestNeoForge::serverStarted);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        XXLCommand.register(event.getDispatcher());
    }

    private static void serverStarted(ServerStartedEvent event) {
        ModrinthUpdateChecker.checkOnceAsync();
    }
}
