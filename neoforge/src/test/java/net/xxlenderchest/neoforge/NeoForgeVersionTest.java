package net.xxlenderchest.neoforge;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeoForgeVersionTest {
    @ParameterizedTest
    @CsvSource({
            "1.10.0, 1.9.0, true",
            "1.2.0, 1.1.9, true",
            "1.1.9, 1.2.0, false",
            "1.1.8, 1.1.8, false",
            "1.2.0, 1.2.0-rc.1, true",
            "1.2.0-rc.1, 1.2.0, false"
    })
    void usesNeoForgeMavenVersionOrdering(String candidate, String current, boolean newer) {
        assertEquals(newer, new NeoForgePlatform().isNewerVersion(candidate, current));
    }
}
