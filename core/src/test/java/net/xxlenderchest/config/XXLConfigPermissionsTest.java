package net.xxlenderchest.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XXLConfigPermissionsTest {
    @Test
    void saveAndLoadPreservePosixPermissionsOwnerAndGroupThroughSymlink() throws Exception {
        try (var fs = Jimfs.newFileSystem(Configuration.unix().toBuilder()
                .setAttributeViews("basic", "owner", "posix", "unix").build())) {
            var target = fs.getPath("/settings.json");
            Files.writeString(target, "{\"rows\":4}");
            var view = Files.getFileAttributeView(target, PosixFileAttributeView.class);
            var lookup = fs.getUserPrincipalLookupService();
            var owner = lookup.lookupPrincipalByName("config-owner");
            var group = lookup.lookupPrincipalByGroupName("server-config");
            var permissions = PosixFilePermissions.fromString("rw-rw----");
            view.setOwner(owner);
            view.setGroup(group);
            view.setPermissions(permissions);
            var link = fs.getPath("/xxlenderchest.json");
            Files.createSymbolicLink(link, target);

            var manager = new XXLConfigManager(link);
            assertEquals(4, manager.load().getRows());
            var edited = new XXLConfig();
            edited.setRows(5);
            assertTrue(manager.save(edited));
            assertEquals(5, manager.load().getRows());
            var attributes = Files.readAttributes(target, java.nio.file.attribute.PosixFileAttributes.class);
            assertEquals(permissions, attributes.permissions());
            assertEquals(owner, attributes.owner());
            assertEquals(group, attributes.group());
            assertTrue(Files.isSymbolicLink(link));
            try (var files = Files.list(fs.getPath("/"))) {
                assertEquals(0, files.filter(path -> path.getFileName().toString().endsWith(".tmp")).count());
            }
        }
    }

    @Test
    void saveAndLoadPreserveAclAndOwner() throws Exception {
        try (var fs = Jimfs.newFileSystem(Configuration.unix().toBuilder()
                .setAttributeViews("basic", "owner", "acl").build())) {
            var target = fs.getPath("/xxlenderchest.json");
            Files.writeString(target, "{\"rows\":4}");
            var owner = fs.getUserPrincipalLookupService().lookupPrincipalByName("config-owner");
            var acl = List.of(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(owner)
                    .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA).build());
            var view = Files.getFileAttributeView(target, AclFileAttributeView.class);
            view.setOwner(owner);
            view.setAcl(acl);

            var manager = new XXLConfigManager(target);
            assertEquals(4, manager.load().getRows());
            var edited = new XXLConfig();
            edited.setRows(5);
            assertTrue(manager.save(edited));
            assertEquals(5, manager.load().getRows());
            var savedView = Files.getFileAttributeView(target, AclFileAttributeView.class);
            assertEquals(owner, savedView.getOwner());
            assertEquals(acl, savedView.getAcl());
        }
    }
}
