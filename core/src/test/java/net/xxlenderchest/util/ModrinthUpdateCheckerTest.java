package net.xxlenderchest.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.module.ModuleDescriptor;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ModrinthUpdateCheckerTest {
    private static Optional<String> extract(String json, String minecraft, String loader) {
        return ModrinthUpdateChecker.extractLatestVersion(json, minecraft, loader,
                (candidate, current) -> ModuleDescriptor.Version.parse(candidate)
                        .compareTo(ModuleDescriptor.Version.parse(current)) > 0);
    }

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
        assertTrue(extract(json, "26.3", "fabric").isEmpty());
    }

    @Test
    void neoForgeSelectsOnlyNeoForgeReleases() {
        String json = "[" + release("1.1.8", "neoforge", "26.3", "release", "2026-10-20T00:00:00Z")
                + "," + release("1.2.0", "fabric", "26.3", "release", "2026-10-21T00:00:00Z") + "]";
        assertEquals("1.1.8", extract(json, "26.3", "neoforge").orElseThrow());
        assertEquals("1.2.0", extract(json, "26.3", "fabric").orElseThrow());
    }

    @Test
    void selectsNewestCompatibleReleaseAndExcludesBeta() {
        String json = "[" + release("1.1.7", "fabric", "26.3", "release", "2026-09-15T00:00:00Z")
                + "," + release("1.1.8", "fabric", "26.3", "release", "2026-10-20T00:00:00Z")
                + "," + release("1.2.0", "fabric", "26.3", "beta", "2026-10-21T00:00:00Z") + "]";
        assertEquals("1.1.8", extract(json, "26.3", "fabric").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fabric", "neoforge"})
    void laterBackportCannotHideHigherVersionInEitherResponseOrder(String loader) {
        String higher = release("1.2.0", loader, "26.3", "release", "2026-10-20T00:00:00Z");
        String backport = release("1.1.9", loader, "26.3", "release", "2026-10-25T00:00:00Z");
        assertEquals("1.2.0", extract("[" + higher + "," + backport + "]", "26.3", loader).orElseThrow());
        assertEquals("1.2.0", extract("[" + backport + "," + higher + "]", "26.3", loader).orElseThrow());
    }

    @Test
    void versionNumbersAreComparedNumericallyInsteadOfLexicographically() {
        String json = "[" + release("1.9.0", "fabric", "26.3", "release", "2026-10-25T00:00:00Z")
                + "," + release("1.10.0", "fabric", "26.3", "release", "2026-10-20T00:00:00Z") + "]";
        assertEquals("1.10.0", extract(json, "26.3", "fabric").orElseThrow());
    }
}
