package net.xxlenderchest.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.client.XXLEnderChestClothConfigScreen;

@Mod(value = XXLEnderChest.MOD_ID, dist = Dist.CLIENT)
public final class XXLEnderChestNeoForgeClient {
    public XXLEnderChestNeoForgeClient(ModContainer container) {
        if (ModList.get().isLoaded("cloth_config")) {
            IConfigScreenFactory factory = (mod, parent) -> XXLEnderChestClothConfigScreen.create(parent);
            container.registerExtensionPoint(IConfigScreenFactory.class, factory);
        }
    }
}
