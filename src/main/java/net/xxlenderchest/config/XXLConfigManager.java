package net.xxlenderchest.config;

import net.xxlenderchest.XXLEnderChest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
        this(FabricLoader.getInstance()
                .getConfigDir()
                .resolve(CONFIG_FILE_NAME));
    }

    XXLConfigManager(Path configPath) {
        this.configPath = configPath;
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

        XXLConfig config;
        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);

            config = GSON.fromJson(jsonReader, XXLConfig.class);

            if (config == null) {
                XXLEnderChest.LOGGER.warn("{} Config file was empty or invalid - using defaults.", XXLEnderChest.getLogPrefix());
                config = new XXLConfig();
            }

            config.validate();
        } catch (IOException | JsonParseException e) {
            XXLEnderChest.LOGGER.error("{} Failed to read config file - using defaults.", XXLEnderChest.getLogPrefix(), e);
            XXLConfig defaults = new XXLConfig();
            save(defaults);
            return defaults;
        } catch (RuntimeException e) {
            XXLEnderChest.LOGGER.error("{} Unexpected config error - using defaults.", XXLEnderChest.getLogPrefix(), e);
            XXLConfig defaults = new XXLConfig();
            save(defaults);
            return defaults;
        }

        // Close the reader before replacing the file, including on Windows.
        save(config);
        XXLEnderChest.LOGGER.debug("{} Config loaded: {}", XXLEnderChest.getLogPrefix(), config);
        return config;
    }

    /**
     * Saves the given config to disk.
     *
     * @param config the config to save
     * @return {@code true} when the config was written successfully
     */
    public boolean save(XXLConfig config) {
        Path temporaryPath = null;
        try {
            // Replace the link's target, never the symbolic link itself.
            Path destination = Files.isSymbolicLink(configPath)
                    ? configPath.toRealPath() : configPath.toAbsolutePath();
            Files.createDirectories(destination.getParent());
            temporaryPath = Files.createTempFile(destination.getParent(), CONFIG_FILE_NAME, ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporaryPath)) {
                writer.write(buildConfigFileContents(config));
            }
            Files.move(temporaryPath, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            XXLEnderChest.LOGGER.debug("{} Config saved to {}", XXLEnderChest.getLogPrefix(), configPath);
            return true;
        } catch (IOException e) {
            XXLEnderChest.LOGGER.error("{} Failed to save config file.", XXLEnderChest.getLogPrefix(), e);
            return false;
        } finally {
            if (temporaryPath != null) {
                try {
                    Files.deleteIfExists(temporaryPath);
                } catch (IOException e) {
                    XXLEnderChest.LOGGER.warn("{} Failed to remove temporary config file {}.",
                            XXLEnderChest.getLogPrefix(), temporaryPath, e);
                }
            }
        }
    }

    private String buildConfigFileContents(XXLConfig config) {
        return """
                {
                  "enabled": %s, // When false, the mod stays inactive and the ender chest remains vanilla.
                  "useLuckPerms": %s, // When true, XXL Enderchest checks LuckPerms row nodes if LuckPerms is installed.
                  "rows": %d, // Fallback row count from 3 to 6. Used when LuckPerms mode is off, or when LuckPerms is missing.
                  "commandEnabled": %s // Enables the /enderchest command. With LuckPerms mode on, players also need xxlenderchest.command.enderchest.
                }
                """.formatted(
                config.isEnabled(),
                config.isUseLuckPerms(),
                config.getRows(),
                config.isCommandEnabled()
        );
    }
}
