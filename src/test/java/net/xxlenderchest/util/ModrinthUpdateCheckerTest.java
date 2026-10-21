package net.xxlenderchest.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModrinthUpdateCheckerTest {
    private static String release(String version, String loader, String minecraft, String type, String date) {
        return """
                {"version_number":"%s","loaders":["%s"],"game_versions":["%s"],
                 "version_type":"%s","date_published":"%s"}
                """.formatted(version, loader, minecraft, type, date);
    }

    @Test
    void noCompatibleReleaseDoesNotFallBackToAnotherLoaderOrMinecraftVersion() {
        String json = "[" + release("9.0", "neoforge", "26.3", "release", "2026-10-20T00:00:00Z")
                + "," + release("8.0", "fabric", "26.4", "release", "2026-10-19T00:00:00Z") + "]";
        assertTrue(ModrinthUpdateChecker.extractLatestVersion(json, "26.3").isEmpty());
    }

    @Test
    void selectsNewestCompatibleReleaseAndExcludesBeta() {
        String json = "[" + release("1.1.7", "fabric", "26.3", "release", "2026-09-15T00:00:00Z")
                + "," + release("1.1.8", "fabric", "26.3", "release", "2026-10-20T00:00:00Z")
                + "," + release("1.2.0", "fabric", "26.3", "beta", "2026-10-21T00:00:00Z") + "]";
        assertEquals("1.1.8", ModrinthUpdateChecker.extractLatestVersion(json, "26.3").orElseThrow());
    }
}
