package net.xxlenderchest.config;

import net.xxlenderchest.XXLEnderChest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * If the file does not exist it is created with sensible defaults (enabled, 6 rows).
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
            XXLEnderChest.LOGGER.info("[XXL Enderchest] Config not found - creating default config at {}", configPath);
            XXLConfig defaults = new XXLConfig();
            save(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            XXLConfig config = GSON.fromJson(reader, XXLConfig.class);

            if (config == null) {
                XXLEnderChest.LOGGER.warn("[XXL Enderchest] Config file was empty or invalid - using defaults.");
                config = new XXLConfig();
            }

            config.validate();
            save(config);
            XXLEnderChest.LOGGER.info("[XXL Enderchest] Config loaded: {}", config);
            return config;

        } catch (IOException e) {
            XXLEnderChest.LOGGER.error("[XXL Enderchest] Failed to read config file - using defaults.", e);
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
                GSON.toJson(config, writer);
            }
            XXLEnderChest.LOGGER.info("[XXL Enderchest] Config saved to {}", configPath);
        } catch (IOException e) {
            XXLEnderChest.LOGGER.error("[XXL Enderchest] Failed to save config file.", e);
        }
    }
}
