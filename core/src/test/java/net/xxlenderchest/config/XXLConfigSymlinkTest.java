package net.xxlenderchest.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XXLConfigSymlinkTest {
    private FileSystem fileSystem;
    private Path directory;

    @BeforeEach
    void createFileSystem() throws Exception {
        // Exercise symlinks on every platform without requiring Windows privileges.
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        directory = Files.createDirectory(fileSystem.getPath("/config"));
    }

    @AfterEach
    void closeFileSystem() throws Exception {
        fileSystem.close();
    }

    @Test
    void savingAndLoadingThroughRelativeLinkChainPreservesLinksAndUpdatesTarget() throws Exception {
        Path targetDirectory = Files.createDirectory(directory.resolve("shared"));
        Path target = targetDirectory.resolve("settings.json");
        assertTrue(new XXLConfigManager(target).save(new XXLConfig()));
        Path intermediate = directory.resolve("shared-link.json");
        Path relativeTarget = directory.relativize(target);
        Files.createSymbolicLink(intermediate, relativeTarget);
        Path configLink = directory.resolve("xxlenderchest.json");
        Files.createSymbolicLink(configLink, intermediate.getFileName());

        XXLConfig edited = new XXLConfig();
        edited.setRows(4);
        edited.setUseLuckPerms(true);
        XXLConfigManager manager = new XXLConfigManager(configLink);
        assertTrue(manager.save(edited));
        assertEquals(4, manager.load().getRows());
        assertTrue(Files.isSymbolicLink(configLink));
        assertTrue(Files.isSymbolicLink(intermediate));
        assertEquals(intermediate.getFileName(), Files.readSymbolicLink(configLink));
        assertEquals(relativeTarget, Files.readSymbolicLink(intermediate));
        XXLConfig stored = new XXLConfigManager(target).load();
        assertEquals(4, stored.getRows());
        assertTrue(stored.isUseLuckPerms());
        try (var files = Files.list(targetDirectory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void brokenSymbolicLinkIsNotReplacedOnSaveFailure() throws Exception {
        Path configLink = directory.resolve("xxlenderchest.json");
        Path missingTarget = fileSystem.getPath("missing.json");
        Files.createSymbolicLink(configLink, missingTarget);
        assertFalse(new XXLConfigManager(configLink).save(new XXLConfig()));
        assertTrue(Files.isSymbolicLink(configLink));
        assertEquals(missingTarget, Files.readSymbolicLink(configLink));
    }
}
