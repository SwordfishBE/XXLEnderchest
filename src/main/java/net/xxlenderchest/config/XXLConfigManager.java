package net.xxlenderchest.config;

import net.xxlenderchest.XXLEnderChest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages reading and writing the {@link XXLConfig} JSON file.
 *
 * The config file is located at {@code <game_dir>/config/xxlenderchest.json}.
 * If the file does not exist it is created with sensible defaults plus inline comments
 * that explain each option.
 */
public class XXLConfigManager {

    private static final String CONFIG_FILE_NAME = "xxlenderchest.json";

    /** Pretty-printing Gson instance (bundled with Minecraft/Fabric). */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Path configPath;

    public XXLConfigManager() {
        this.configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(CONFIG_FILE_NAME);
    }

    /**
     * Loads the config from disk, creating a default file if none exists.
     *
     * @return the loaded (and validated) {@link XXLConfig}
     */
    public XXLConfig load() {
        if (!Files.exists(configPath)) {
            XXLEnderChest.LOGGER.debug("{} Config not found - creating default config at {}", XXLEnderChest.getLogPrefix(), configPath);
            XXLConfig defaults = new XXLConfig();
            save(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);

            XXLConfig config = GSON.fromJson(jsonReader, XXLConfig.class);

            if (config == null) {
                XXLEnderChest.LOGGER.warn("{} Config file was empty or invalid - using defaults.", XXLEnderChest.getLogPrefix());
                config = new XXLConfig();
            }

            config.validate();
            save(config);
            XXLEnderChest.LOGGER.debug("{} Config loaded: {}", XXLEnderChest.getLogPrefix(), config);
            return config;

        } catch (IOException e) {
            XXLEnderChest.LOGGER.error("{} Failed to read config file - using defaults.", XXLEnderChest.getLogPrefix(), e);
            return new XXLConfig();
        }
    }

    /**
     * Saves the given config to disk.
     *
     * @param config the config to save
     */
    public void save(XXLConfig config) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                writer.write(buildConfigFileContents(config));
            }
            XXLEnderChest.LOGGER.debug("{} Config saved to {}", XXLEnderChest.getLogPrefix(), configPath);
        } catch (IOException e) {
            XXLEnderChest.LOGGER.error("{} Failed to save config file.", XXLEnderChest.getLogPrefix(), e);
        }
    }

    private String buildConfigFileContents(XXLConfig config) {
        return """
                {
                  "enabled": %s, // When false, the mod stays inactive and the ender chest remains vanilla.
                  "useLuckPerms": %s, // When true, XXL Enderchest checks LuckPerms row nodes if LuckPerms is installed.
                  "rows": %d // Fallback row count from 3 to 6. Used when LuckPerms mode is off, or when LuckPerms is missing.
                }
                """.formatted(config.isEnabled(), config.isUseLuckPerms(), config.getRows());
    }
}
