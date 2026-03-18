package wueffi.showClaims.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.Text;

import static wueffi.showClaims.client.ShowClaimsClient.LOGGER;

public class ConfigLoader {

    public static ClaimConfig load(ClientPlayerEntity player) {
        if (isOnORENetwork()) {
            player.sendMessage(Text.of("§7[§6ShowClaims$7 §dDetected ORE Network. Using GitHub Survival Claims"), false);
            ClaimConfig oreConfig = loadFromGitHub();
            if (oreConfig != null) {
                return oreConfig;
            }
            LOGGER.warn("Failed to load ORE config from GitHub. Falling back to local config.");
        }

        return loadLocal();
    }

    private static boolean isOnORENetwork() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return false;

        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo == null) return false;

        String address = serverInfo.address.toLowerCase();
        int colonIndex = address.lastIndexOf(':');
        if (colonIndex >= 0) {
            address = address.substring(0, colonIndex);
        }

        return address.equals("mc.openredstone.org") || address.endsWith("." + "openredstone.org");
    }

    private static ClaimConfig loadFromGitHub() {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder() .uri(URI.create("https://raw.githubusercontent.com/Wueffi/ShowClaims/refs/heads/main/OREClaimsConfig.yml")) .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.warn("GitHub returned status {} for ORE config.", response.statusCode());
                return null;
            }

            List<String> lines = response.body().lines().toList();
            return parseLines(lines);

        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Failed to fetch ORE config from GitHub: {}", e.getMessage());
            return null;
        }
    }

    private static ClaimConfig loadLocal() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("showClaimsConfig.yml");

        if (!Files.exists(configPath)) {
            createDefault(configPath);
        }

        return parseFile(configPath);
    }

    private static void createDefault(Path path) {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            writer.println("# Show Claims config");
            writer.println("# Place this file at .minecraft/config/showClaimsConfig.yml");
            writer.println("#");
            writer.println("# default = text shown when you are not inside any claim");
            writer.println("# [Claim Name] = the text shown when you are inside that claim");
            writer.println("# corner_a = x y z   (one corner of the claim)");
            writer.println("# corner_b = x y z   (opposite corner of the claim)");
            writer.println();
            writer.println("default = No claim found!");
            writer.println();
            writer.println("[Spawn]");
            writer.println("corner_a = -25 60 -25");
            writer.println("corner_b = 25 80 25");
            writer.println();
        } catch (IOException e) {
            LOGGER.warn("Failed to create default config: {}", e.getMessage());
        }
    }

    private static ClaimConfig parseFile(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            return parseLines(lines);
        } catch (IOException e) {
            LOGGER.warn("Failed to read config: {}", e.getMessage());
            return new ClaimConfig("Not inside a claim", new ArrayList<>());
        }
    }

    private static ClaimConfig parseLines(List<String> lines) {
        String defaultText = "Not inside a claim";
        List<Region> regions = new ArrayList<>();

        String currentLabel = null;
        double[] cornerA = null;
        double[] cornerB = null;

        for (String raw : lines) {
            String line = raw.trim();

            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("default")) {
                int eq = line.indexOf('=');
                if (eq >= 0) {
                    defaultText = line.substring(eq + 1).trim();
                }
                continue;
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                if (currentLabel != null && cornerA != null && cornerB != null) {
                    regions.add(new Region(currentLabel,
                            cornerA[0], cornerA[1], cornerA[2],
                            cornerB[0], cornerB[1], cornerB[2]));
                }
                currentLabel = line.substring(1, line.length() - 1).trim();
                cornerA = null;
                cornerB = null;
                continue;
            }

            if (line.startsWith("corner_a") || line.startsWith("corner_b")) {
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String[] parts = line.substring(eq + 1).trim().split("\\s+");
                if (parts.length < 3) continue;
                try {
                    double[] coords = {
                            Double.parseDouble(parts[0]),
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2])
                    };
                    if (line.startsWith("corner_a")) {
                        cornerA = coords;
                    } else {
                        cornerB = coords;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warn("Error with coordinates on line: {}", raw);
                }
            }
        }

        if (currentLabel != null && cornerA != null && cornerB != null) {
            regions.add(new Region(currentLabel,
                    cornerA[0], cornerA[1], cornerA[2],
                    cornerB[0], cornerB[1], cornerB[2]));
        }

        return new ClaimConfig(defaultText, regions);
    }
}