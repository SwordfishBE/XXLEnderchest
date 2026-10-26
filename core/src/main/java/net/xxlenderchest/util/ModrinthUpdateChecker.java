package net.xxlenderchest.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.xxlenderchest.XXLEnderChest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;

public final class ModrinthUpdateChecker {

    private static final String PROJECT_ID = "Gq8wsb3l";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();
    private static final AtomicBoolean CHECK_STARTED = new AtomicBoolean(false);

    private ModrinthUpdateChecker() {
    }

    public static void checkOnceAsync() {
        if (!CHECK_STARTED.compareAndSet(false, true)) {
            return;
        }

        Thread thread = new Thread(ModrinthUpdateChecker::checkForUpdate, "xxlenderchest-modrinth-update-check");
        thread.setDaemon(true);
        thread.start();
    }

    private static void checkForUpdate() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/project/" + PROJECT_ID + "/version"))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("User-Agent", "XXLEnderchest/" + XXLEnderChest.getModVersion())
                .GET()
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                XXLEnderChest.LOGGER.debug("{} Update check returned HTTP {}.", XXLEnderChest.getLogPrefix(), response.statusCode());
                return;
            }

            Optional<String> latestVersion = extractLatestVersion(response.body());
            if (latestVersion.isEmpty()) {
                XXLEnderChest.LOGGER.debug("{} Update check returned no usable versions.", XXLEnderChest.getLogPrefix());
                return;
            }

            String currentVersion = currentVersion();
            String newestVersion = latestVersion.get();
            if (isNewerVersion(newestVersion, currentVersion)) {
                XXLEnderChest.LOGGER.info("{} New version available: {} (current: {})",
                        XXLEnderChest.getLogPrefix(),
                        newestVersion, currentVersion);
            } else {
                XXLEnderChest.LOGGER.debug("{} No newer version found. Latest compatible: {} (current: {})",
                        XXLEnderChest.getLogPrefix(),
                        newestVersion, currentVersion);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            XXLEnderChest.LOGGER.debug("{} Update check failed.", XXLEnderChest.getLogPrefix(), e);
        } catch (RuntimeException e) {
            XXLEnderChest.LOGGER.debug("{} Invalid update response.", XXLEnderChest.getLogPrefix(), e);
        }
    }

    private static Optional<String> extractLatestVersion(String responseBody) {
        return extractLatestVersion(responseBody, currentMinecraftVersion(), XXLEnderChest.getPlatform().loaderId(),
                ModrinthUpdateChecker::isNewerVersion);
    }

    static Optional<String> extractLatestVersion(String responseBody, String currentMinecraftVersion, String loader,
                                                BiPredicate<String, String> newerVersion) {
        JsonElement root = JsonParser.parseString(responseBody);
        if (!root.isJsonArray()) {
            return Optional.empty();
        }

        JsonArray versions = root.getAsJsonArray();
        VersionCandidate newestCompatibleRelease = null;

        for (JsonElement versionElement : versions) {
            if (!versionElement.isJsonObject()) {
                continue;
            }

            JsonObject versionObject = versionElement.getAsJsonObject();
            String versionNumber = getString(versionObject, "version_number");
            if (versionNumber == null || versionNumber.isBlank()) {
                continue;
            }

            Instant publishedAt = getPublishedAt(versionObject);
            if (publishedAt == null) {
                continue;
            }

            String versionType = getString(versionObject, "version_type");
            if (!"release".equalsIgnoreCase(versionType)) {
                continue;
            }

            VersionCandidate candidate = new VersionCandidate(versionNumber, publishedAt);
            if (jsonArrayContains(versionObject, "loaders", loader)
                    && jsonArrayContains(versionObject, "game_versions", currentMinecraftVersion)
                    && isNewerCandidate(candidate, newestCompatibleRelease, newerVersion)) {
                newestCompatibleRelease = candidate;
            }
        }

        return newestCompatibleRelease != null
                ? Optional.of(newestCompatibleRelease.versionNumber()) : Optional.empty();
    }

    private static String getString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            return null;
        }

        return value.getAsString();
    }

    private static boolean jsonArrayContains(JsonObject object, String key, String expectedValue) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonArray()) {
            return false;
        }

        for (JsonElement element : value.getAsJsonArray()) {
            if (element != null && element.isJsonPrimitive() && expectedValue.equalsIgnoreCase(element.getAsString())) {
                return true;
            }
        }

        return false;
    }

    private static Instant getPublishedAt(JsonObject object) {
        String publishedAt = getString(object, "date_published");
        if (publishedAt == null || publishedAt.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(publishedAt);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean isNewerCandidate(VersionCandidate candidate, VersionCandidate currentBest,
                                            BiPredicate<String, String> newerVersion) {
        if (currentBest == null || newerVersion.test(candidate.versionNumber(), currentBest.versionNumber())) {
            return true;
        }
        if (newerVersion.test(currentBest.versionNumber(), candidate.versionNumber())) {
            return false;
        }
        return candidate.publishedAt().isAfter(currentBest.publishedAt());
    }

    private static String currentVersion() {
        return XXLEnderChest.getModVersion();
    }

    private static String currentMinecraftVersion() {
        return XXLEnderChest.getPlatform().minecraftVersion();
    }

    private static boolean isNewerVersion(String candidate, String current) {
        return XXLEnderChest.getPlatform().isNewerVersion(candidate, current);
    }

    private record VersionCandidate(String versionNumber, Instant publishedAt) {
    }
}
