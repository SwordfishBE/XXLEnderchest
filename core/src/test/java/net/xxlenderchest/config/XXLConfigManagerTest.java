package net.xxlenderchest.config;

import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.platform.TestPlatform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class XXLConfigManagerTest {
    @TempDir
    Path directory;

    @Test
    void replacesExistingConfigAndReloadsWithoutLeavingTemporaryFiles() throws Exception {
        Path path = directory.resolve("xxlenderchest.json");
        XXLConfigManager manager = new XXLConfigManager(path);
        assertTrue(manager.save(new XXLConfig()));

        XXLConfig edited = new XXLConfig();
        edited.setRows(4);
        edited.setUseLuckPerms(true);
        edited.setCommandEnabled(true);
        assertTrue(manager.save(edited));

        XXLConfig reloaded = manager.load();
        assertEquals(4, reloaded.getRows());
        assertTrue(reloaded.isUseLuckPerms());
        assertTrue(reloaded.isCommandEnabled());
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void failedReplacementPreservesExistingTargetAndCleansTemporaryFile() throws Exception {
        Path target = Files.createDirectory(directory.resolve("xxlenderchest.json"));
        Path existing = target.resolve("existing-data");
        Files.writeString(existing, "preserve me");

        assertFalse(new XXLConfigManager(target).save(new XXLConfig()));
        assertEquals("preserve me", Files.readString(existing));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void malformedConfigFallsBackToReadableDefaults() throws Exception {
        Path path = directory.resolve("xxlenderchest.json");
        Files.writeString(path, "{\"rows\":");
        XXLConfigManager manager = new XXLConfigManager(path);
        assertEquals(6, manager.load().getRows());
        assertEquals(6, manager.load().getRows());
    }

    @Test
    void appliedConfigIsDetachedFromFurtherGuiEdits() throws Exception {
        withRuntimeConfig(new XXLConfigManager(directory.resolve("xxlenderchest.json")), () -> {
            XXLConfig edited = new XXLConfig();
            edited.setRows(4);
            edited.setUseLuckPerms(true);
            XXLEnderChest.applyEditedConfig(edited);
            edited.setRows(6);
            edited.setUseLuckPerms(false);
            assertEquals(4, XXLEnderChest.getConfig().getRows());
            assertTrue(XXLEnderChest.getConfig().isUseLuckPerms());
            XXLConfig anotherEdit = XXLEnderChest.loadConfigForEditing();
            anotherEdit.setRows(5);
            assertEquals(4, XXLEnderChest.getConfig().getRows());
        });
    }

    @Test
    void failedSaveDoesNotReplaceActiveConfig() throws Exception {
        Path target = Files.createDirectory(directory.resolve("xxlenderchest.json"));
        Files.writeString(target.resolve("existing-data"), "preserve me");
        withRuntimeConfig(new XXLConfigManager(target), () -> {
            XXLConfig active = XXLEnderChest.getConfig();
            XXLConfig edited = new XXLConfig();
            edited.setRows(3);
            XXLEnderChest.applyEditedConfig(edited);
            assertSame(active, XXLEnderChest.getConfig());
            assertEquals(6, XXLEnderChest.getConfig().getRows());
        });
    }

    private static void withRuntimeConfig(XXLConfigManager manager, Runnable check) throws Exception {
        var managerField = XXLEnderChest.class.getDeclaredField("configManager");
        var configField = XXLEnderChest.class.getDeclaredField("config");
        var platformField = XXLEnderChest.class.getDeclaredField("platform");
        managerField.setAccessible(true);
        configField.setAccessible(true);
        platformField.setAccessible(true);
        Object previousManager = managerField.get(null);
        Object previousConfig = configField.get(null);
        Object previousPlatform = platformField.get(null);
        try {
            managerField.set(null, manager);
            configField.set(null, new XXLConfig());
            platformField.set(null, new TestPlatform(Path.of("."), "test", Set.of(), Set.of()));
            check.run();
        } finally {
            managerField.set(null, previousManager);
            configField.set(null, previousConfig);
            platformField.set(null, previousPlatform);
        }
    }
}
