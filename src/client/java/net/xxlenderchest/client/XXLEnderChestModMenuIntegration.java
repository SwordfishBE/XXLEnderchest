package net.xxlenderchest.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Mod Menu integration entrypoint.
 *
 * <p>Mod Menu itself is optional, and Cloth Config is optional too. When Cloth
 * Config is missing we expose no config screen so the mod still runs cleanly on
 * clients and dedicated servers without extra requirements.</p>
 */
public class XXLEnderChestModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return parent -> null;
        }

        return XXLEnderChestClothConfigScreen::create;
    }
}
