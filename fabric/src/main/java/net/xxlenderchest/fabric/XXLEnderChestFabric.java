package net.xxlenderchest.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.command.XXLCommand;
import net.xxlenderchest.util.ModrinthUpdateChecker;

public final class XXLEnderChestFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        XXLEnderChest.initialize(new FabricPlatform());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> XXLCommand.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> ModrinthUpdateChecker.checkOnceAsync());
    }
}
