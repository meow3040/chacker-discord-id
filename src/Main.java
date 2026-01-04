import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final long DISCORD_EPOCH = 1420070400000L;
    private static final String DISCORD_API_URL = "https://discord.com/api/v10/users/";

    // OPTIONAL: Add your Discord bot token here to fetch full user details
    // Leave empty to only extract information from the ID itself
    private static final String BOT_TOKEN = "MTI3MDgyODE3NzMwNDA2MDAzNg.Gsel5T._j8ngiPExRKm8GTfCKvwbtP5P8Hcg8b8-pvw8c";

    public static void main(String[] args) {
        // Launch GUI by default, or console mode if "console" argument is passed
        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            runConsoleMode();
        } else {
            // Launch GUI
            DiscordInfoGUI.main(args);
        }
    }

    private static void runConsoleMode() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Discord User Information Fetcher");
        System.out.println("=================================");
        System.out.print("Enter Discord User ID: ");
        String userId = scanner.nextLine().trim();

        if (userId.isEmpty()) {
            System.out.println("Error: User ID cannot be empty!");
            return;
        }

        if (!userId.matches("\\d+")) {
            System.out.println("Error: Invalid User ID format! Discord IDs are numeric.");
            return;
        }

        try {
            // Always show snowflake ID information
            displayDiscordIdInfo(userId);

            // If bot token is provided, fetch full user details from Discord API
            if (!BOT_TOKEN.isEmpty()) {
                System.out.println("\n" + "=".repeat(50));
                String userInfo = fetchDiscordUserInfo(userId);
                displayFullUserInfo(userInfo);
            } else {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("To fetch full user details (username, avatar, badges, etc.),");
                System.out.println("add your Discord bot token to the BOT_TOKEN variable.");
                System.out.println("\nHow to get a bot token:");
                System.out.println("1. Go to https://discord.com/developers/applications");
                System.out.println("2. Create a new application");
                System.out.println("3. Go to Bot section and copy the token");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void displayDiscordIdInfo(String userId) {
        System.out.println("\n=== Snowflake ID Analysis ===\n");

        long id = Long.parseLong(userId);

        // Extract timestamp from snowflake ID
        long timestamp = (id >> 22) + DISCORD_EPOCH;
        Instant creationTime = Instant.ofEpochMilli(timestamp);

        // Format the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(creationTime);

        // Calculate account age
        long ageInMillis = System.currentTimeMillis() - timestamp;
        long days = ageInMillis / (1000 * 60 * 60 * 24);
        long years = days / 365;
        long remainingDays = days % 365;
        long months = remainingDays / 30;
        remainingDays = remainingDays % 30;

        // Extract worker ID, process ID, and increment
        long workerId = (id & 0x3E0000) >> 17;
        long processId = (id & 0x1F000) >> 12;
        long increment = id & 0xFFF;

        System.out.println("User ID: " + userId);
        System.out.println("ID in Binary: " + Long.toBinaryString(id));
        System.out.println("ID in Hex: 0x" + Long.toHexString(id).toUpperCase());
        System.out.println();

        System.out.println("Account Created: " + formattedDate);
        System.out.println("Unix Timestamp: " + timestamp);
        System.out.println("Account Age: " + years + " years, " + months + " months, " + remainingDays + " days");
        System.out.println();

        System.out.println("Snowflake Breakdown:");
        System.out.println("  Timestamp: " + timestamp + " ms since Discord epoch");
        System.out.println("  Worker ID: " + workerId);
        System.out.println("  Process ID: " + processId);
        System.out.println("  Increment: " + increment);
        System.out.println();

        // Age-based account type estimation
        System.out.println("Account Type Estimate:");
        if (years >= 8) {
            System.out.println("  - Very Old Account (OG Discord User!)");
        } else if (years >= 5) {
            System.out.println("  - Old Account");
        } else if (years >= 2) {
            System.out.println("  - Established Account");
        } else if (years >= 1) {
            System.out.println("  - Moderately Old Account");
        } else {
            System.out.println("  - Relatively New Account");
        }
    }

    private static String fetchDiscordUserInfo(String userId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISCORD_API_URL + userId))
                .header("Authorization", "Bot " + BOT_TOKEN)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else if (response.statusCode() == 404) {
            throw new IOException("User not found! The Discord ID may not exist.");
        } else if (response.statusCode() == 401) {
            throw new IOException("Unauthorized! Check your bot token.");
        } else {
            throw new IOException("API request failed with status code: " + response.statusCode());
        }
    }

    private static void displayFullUserInfo(String jsonResponse) {
        System.out.println("\n=== Full Discord User Details ===\n");

        // Parse JSON manually (simple extraction)
        String username = extractJsonValue(jsonResponse, "username");
        String discriminator = extractJsonValue(jsonResponse, "discriminator");
        String id = extractJsonValue(jsonResponse, "id");
        String avatar = extractJsonValue(jsonResponse, "avatar");
        String globalName = extractJsonValue(jsonResponse, "global_name");
        String publicFlags = extractJsonValue(jsonResponse, "public_flags");
        String accentColor = extractJsonValue(jsonResponse, "accent_color");
        String banner = extractJsonValue(jsonResponse, "banner");
        String bannerColor = extractJsonValue(jsonResponse, "banner_color");
        String bot = extractJsonValue(jsonResponse, "bot");

        System.out.println("Username: " + username);

        if (globalName != null && !globalName.equals("null")) {
            System.out.println("Display Name: " + globalName);
        }

        if (discriminator != null && !discriminator.equals("0")) {
            System.out.println("Discriminator: #" + discriminator);
            System.out.println("Full Tag: " + username + "#" + discriminator);
        }

        if (bot != null && bot.equals("true")) {
            System.out.println("Account Type: BOT");
        } else {
            System.out.println("Account Type: USER");
        }

        System.out.println();

        if (avatar != null && !avatar.equals("null")) {
            String extension = avatar.startsWith("a_") ? ".gif" : ".png";
            String avatarUrl = "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + extension;
            System.out.println("Avatar URL: " + avatarUrl);
        } else {
            System.out.println("Avatar: Default Avatar");
        }

        if (banner != null && !banner.equals("null")) {
            String extension = banner.startsWith("a_") ? ".gif" : ".png";
            String bannerUrl = "https://cdn.discordapp.com/banners/" + id + "/" + banner + extension;
            System.out.println("Banner URL: " + bannerUrl);
        }

        if (bannerColor != null && !bannerColor.equals("null")) {
            System.out.println("Banner Color: " + bannerColor);
        }

        if (accentColor != null && !accentColor.equals("null")) {
            try {
                int color = Integer.parseInt(accentColor);
                String hexColor = String.format("#%06X", color);
                System.out.println("Accent Color: " + hexColor + " (Decimal: " + accentColor + ")");
            } catch (NumberFormatException e) {
                System.out.println("Accent Color: " + accentColor);
            }
        }

        if (publicFlags != null && !publicFlags.equals("null") && !publicFlags.equals("0")) {
            System.out.println("\nPublic Flags: " + publicFlags);
            displayUserBadges(Integer.parseInt(publicFlags));
        }

        System.out.println("\n=== Raw JSON Response ===");
        System.out.println(jsonResponse);
    }

    private static String extractJsonValue(String json, String key) {
        // Handle string values
        Pattern stringPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher stringMatcher = stringPattern.matcher(json);
        if (stringMatcher.find()) {
            return stringMatcher.group(1);
        }

        // Handle non-string values (numbers, booleans, null)
        Pattern valuePattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}\\]]+)");
        Matcher valueMatcher = valuePattern.matcher(json);
        if (valueMatcher.find()) {
            return valueMatcher.group(1).trim();
        }

        return null;
    }

    private static void displayUserBadges(int flags) {
        System.out.println("User Badges:");
        if ((flags & (1 << 0)) != 0) System.out.println("  - Discord Employee");
        if ((flags & (1 << 1)) != 0) System.out.println("  - Partnered Server Owner");
        if ((flags & (1 << 2)) != 0) System.out.println("  - HypeSquad Events");
        if ((flags & (1 << 3)) != 0) System.out.println("  - Bug Hunter Level 1");
        if ((flags & (1 << 6)) != 0) System.out.println("  - HypeSquad Bravery");
        if ((flags & (1 << 7)) != 0) System.out.println("  - HypeSquad Brilliance");
        if ((flags & (1 << 8)) != 0) System.out.println("  - HypeSquad Balance");
        if ((flags & (1 << 9)) != 0) System.out.println("  - Early Supporter");
        if ((flags & (1 << 14)) != 0) System.out.println("  - Bug Hunter Level 2");
        if ((flags & (1 << 16)) != 0) System.out.println("  - Verified Bot Developer");
        if ((flags & (1 << 17)) != 0) System.out.println("  - Early Verified Bot Developer");
        if ((flags & (1 << 18)) != 0) System.out.println("  - Discord Certified Moderator");
        if ((flags & (1 << 22)) != 0) System.out.println("  - Active Developer");
    }
}
