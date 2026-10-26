package net.xxlenderchest.permission;

import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.config.XXLConfig;
import net.xxlenderchest.platform.TestPlatform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermissionHelperTest {
    private Field platformField;
    private Object previousPlatform;

    @BeforeEach
    void savePlatform() throws Exception {
        platformField = XXLEnderChest.class.getDeclaredField("platform");
        platformField.setAccessible(true);
        previousPlatform = platformField.get(null);
    }

    @AfterEach
    void restorePlatform() throws Exception {
        platformField.set(null, previousPlatform);
    }

    private void usePlatform(Set<String> mods, Set<String> permissions) throws Exception {
        platformField.set(null, new TestPlatform(Path.of("."), "test", mods, permissions));
    }

    @Test
    void commandPermissionRemainsRequiredWhenExpansionIsDisabled() throws Exception {
        XXLConfig config = new XXLConfig();
        config.setEnabled(false);
        config.setUseLuckPerms(true);
        config.setCommandEnabled(true);
        usePlatform(Set.of("luckperms"), Set.of());
        assertFalse(PermissionHelper.canUseEnderChestCommand(null, config));
        usePlatform(Set.of("luckperms"), Set.of(PermissionHelper.PERMISSION_COMMAND_ENDERCHEST));
        assertTrue(PermissionHelper.canUseEnderChestCommand(null, config));
        assertEquals(3, PermissionHelper.resolveRows(null, config));
        config.setCommandEnabled(false);
        assertFalse(PermissionHelper.canUseEnderChestCommand(null, config));
    }

    @Test
    void missingLuckPermsFallsBackToConfig() throws Exception {
        usePlatform(Set.of(), Set.of());
        XXLConfig config = new XXLConfig();
        config.setRows(4);
        config.setUseLuckPerms(true);
        config.setCommandEnabled(true);
        assertEquals(4, PermissionHelper.resolveRows(null, config));
        assertTrue(PermissionHelper.canUseEnderChestCommand(null, config));
    }

    @Test
    void highestGrantedRowWinsAndNoPermissionKeepsVanillaAccess() throws Exception {
        XXLConfig config = new XXLConfig();
        config.setUseLuckPerms(true);
        usePlatform(Set.of("luckperms"), Set.of(PermissionHelper.PERMISSION_ROW_4, PermissionHelper.PERMISSION_ROW_6));
        assertEquals(6, PermissionHelper.resolveRows(null, config));
        usePlatform(Set.of("luckperms"), Set.of());
        assertEquals(3, PermissionHelper.resolveRows(null, config));
    }
}
