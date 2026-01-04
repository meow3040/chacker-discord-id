import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordInfoGUI extends JFrame {
    private static final long DISCORD_EPOCH = 1420070400000L;
    private static final String DISCORD_API_URL = "https://discord.com/api/v10/users/";
    private static final String DISCORD_GUILDS_URL = "https://discord.com/api/v10/users/@me/guilds";

    private JTextField userIdField;
    private JTextField botTokenField;
    private JTextArea resultArea;
    private JButton fetchButton;
    private JButton clearButton;
    private JCheckBox checkServersCheckbox;

    public DiscordInfoGUI() {
        setTitle("Discord User Information Fetcher");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top Panel - Input Section
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        inputPanel.setBackground(new Color(44, 47, 51));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel titleLabel = new JLabel("Discord User Information Fetcher");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        inputPanel.add(titleLabel, gbc);

        // User ID Label
        JLabel userIdLabel = new JLabel("Discord User ID:");
        userIdLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        inputPanel.add(userIdLabel, gbc);

        // User ID Field
        userIdField = new JTextField(20);
        userIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        inputPanel.add(userIdField, gbc);

        // Bot Token Label
        JLabel botTokenLabel = new JLabel("Bot Token (Optional):");
        botTokenLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        inputPanel.add(botTokenLabel, gbc);

        // Bot Token Field
        botTokenField = new JTextField(20);
        botTokenField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        inputPanel.add(botTokenField, gbc);

        // Check Servers Checkbox
        checkServersCheckbox = new JCheckBox("Check Mutual Servers");
        checkServersCheckbox.setForeground(Color.WHITE);
        checkServersCheckbox.setBackground(new Color(44, 47, 51));
        checkServersCheckbox.setFont(new Font("Arial", Font.PLAIN, 12));
        checkServersCheckbox.setToolTipText("Check which servers both the bot and user are in (requires bot token)");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        inputPanel.add(checkServersCheckbox, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(44, 47, 51));

        fetchButton = new JButton("Fetch Info");
        fetchButton.setFont(new Font("Arial", Font.BOLD, 14));
        fetchButton.setBackground(new Color(88, 101, 242));
        fetchButton.setForeground(Color.WHITE);
        fetchButton.setFocusPainted(false);
        fetchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fetchButton.addActionListener(e -> fetchUserInfo());
        buttonPanel.add(fetchButton);

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setBackground(new Color(237, 66, 69));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // Center Panel - Results Section
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        resultPanel.setBackground(new Color(54, 57, 63));

        JLabel resultLabel = new JLabel("Results:");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        resultLabel.setForeground(Color.WHITE);
        resultLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resultPanel.add(resultLabel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setBackground(new Color(32, 34, 37));
        resultArea.setForeground(new Color(220, 221, 222));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        add(resultPanel, BorderLayout.CENTER);

        // Set initial text
        resultArea.setText("Enter a Discord User ID and click 'Fetch Info' to get started.\n\n" +
                "Tips:\n" +
                "- To get a user ID, enable Developer Mode in Discord settings\n" +
                "- Right-click any user and select 'Copy User ID'\n" +
                "- Bot token is optional but required for full details (username, avatar, badges)\n" +
                "- Check 'Check Mutual Servers' to see which servers both the bot and user are in\n\n" +
                "Note: Discord's API only shows mutual servers (where both your bot and the user are members).\n" +
                "You cannot see all servers a user is in due to Discord's privacy restrictions.");
    }

    private void fetchUserInfo() {
        String userId = userIdField.getText().trim();

        if (userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Discord User ID!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!userId.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid User ID format! Discord IDs are numeric.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        fetchButton.setEnabled(false);
        fetchButton.setText("Fetching...");
        resultArea.setText("Fetching information...\n");

        // Run in background thread to prevent GUI freezing
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                StringBuilder result = new StringBuilder();

                try {
                    // Always show snowflake ID information
                    result.append(getSnowflakeInfo(userId));

                    // If bot token is provided, fetch full user details
                    String botToken = botTokenField.getText().trim();
                    if (!botToken.isEmpty()) {
                        result.append("\n").append("=".repeat(60)).append("\n");
                        String userInfo = fetchDiscordUserInfo(userId, botToken);
                        result.append(getFullUserInfo(userInfo));

                        // Check mutual servers if checkbox is selected
                        if (checkServersCheckbox.isSelected()) {
                            result.append("\n").append("=".repeat(60)).append("\n");
                            result.append(getMutualServers(userId, botToken));
                        }
                    } else {
                        result.append("\n").append("=".repeat(60)).append("\n");
                        result.append("\nℹ️ Bot Token not provided.\n");
                        result.append("To fetch full user details (username, avatar, badges, etc.),\n");
                        result.append("enter your Discord bot token above.\n");

                        if (checkServersCheckbox.isSelected()) {
                            result.append("\n⚠️ Bot token required to check mutual servers.\n");
                        }
                    }
                } catch (Exception e) {
                    result.append("\n❌ Error: ").append(e.getMessage());
                }

                return result.toString();
            }

            @Override
            protected void done() {
                try {
                    resultArea.setText(get());
                } catch (Exception e) {
                    resultArea.setText("Error: " + e.getMessage());
                }
                fetchButton.setEnabled(true);
                fetchButton.setText("Fetch Info");
            }
        };

        worker.execute();
    }

    private String getSnowflakeInfo(String userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Snowflake ID Analysis ===\n\n");

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

        sb.append("User ID: ").append(userId).append("\n");
        sb.append("ID in Binary: ").append(Long.toBinaryString(id)).append("\n");
        sb.append("ID in Hex: 0x").append(Long.toHexString(id).toUpperCase()).append("\n\n");

        sb.append("Account Created: ").append(formattedDate).append("\n");
        sb.append("Unix Timestamp: ").append(timestamp).append("\n");
        sb.append("Account Age: ").append(years).append(" years, ").append(months).append(" months, ").append(remainingDays).append(" days\n\n");

        sb.append("Snowflake Breakdown:\n");
        sb.append("  Timestamp: ").append(timestamp).append(" ms since Discord epoch\n");
        sb.append("  Worker ID: ").append(workerId).append("\n");
        sb.append("  Process ID: ").append(processId).append("\n");
        sb.append("  Increment: ").append(increment).append("\n\n");

        sb.append("Account Type Estimate:\n");
        if (years >= 8) {
            sb.append("  - Very Old Account (OG Discord User!)\n");
        } else if (years >= 5) {
            sb.append("  - Old Account\n");
        } else if (years >= 2) {
            sb.append("  - Established Account\n");
        } else if (years >= 1) {
            sb.append("  - Moderately Old Account\n");
        } else {
            sb.append("  - Relatively New Account\n");
        }

        return sb.toString();
    }

    private String fetchDiscordUserInfo(String userId, String botToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISCORD_API_URL + userId))
                .header("Authorization", "Bot " + botToken)
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

    private String getFullUserInfo(String jsonResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Full Discord User Details ===\n\n");

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

        sb.append("Username: ").append(username).append("\n");

        if (globalName != null && !globalName.equals("null")) {
            sb.append("Display Name: ").append(globalName).append("\n");
        }

        if (discriminator != null && !discriminator.equals("0")) {
            sb.append("Discriminator: #").append(discriminator).append("\n");
            sb.append("Full Tag: ").append(username).append("#").append(discriminator).append("\n");
        }

        if (bot != null && bot.equals("true")) {
            sb.append("Account Type: BOT\n");
        } else {
            sb.append("Account Type: USER\n");
        }

        sb.append("\n");

        if (avatar != null && !avatar.equals("null")) {
            String extension = avatar.startsWith("a_") ? ".gif" : ".png";
            String avatarUrl = "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + extension;
            sb.append("Avatar URL: ").append(avatarUrl).append("\n");
        } else {
            sb.append("Avatar: Default Avatar\n");
        }

        if (banner != null && !banner.equals("null")) {
            String extension = banner.startsWith("a_") ? ".gif" : ".png";
            String bannerUrl = "https://cdn.discordapp.com/banners/" + id + "/" + banner + extension;
            sb.append("Banner URL: ").append(bannerUrl).append("\n");
        }

        if (bannerColor != null && !bannerColor.equals("null")) {
            sb.append("Banner Color: ").append(bannerColor).append("\n");
        }

        if (accentColor != null && !accentColor.equals("null")) {
            try {
                int color = Integer.parseInt(accentColor);
                String hexColor = String.format("#%06X", color);
                sb.append("Accent Color: ").append(hexColor).append(" (Decimal: ").append(accentColor).append(")\n");
            } catch (NumberFormatException e) {
                sb.append("Accent Color: ").append(accentColor).append("\n");
            }
        }

        if (publicFlags != null && !publicFlags.equals("null") && !publicFlags.equals("0")) {
            sb.append("\nPublic Flags: ").append(publicFlags).append("\n");
            sb.append(getUserBadges(Integer.parseInt(publicFlags)));
        }

        sb.append("\n=== Raw JSON Response ===\n");
        sb.append(jsonResponse);

        return sb.toString();
    }

    private String extractJsonValue(String json, String key) {
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

    private String getUserBadges(int flags) {
        StringBuilder sb = new StringBuilder();
        sb.append("User Badges:\n");
        if ((flags & (1 << 0)) != 0) sb.append("  - Discord Employee\n");
        if ((flags & (1 << 1)) != 0) sb.append("  - Partnered Server Owner\n");
        if ((flags & (1 << 2)) != 0) sb.append("  - HypeSquad Events\n");
        if ((flags & (1 << 3)) != 0) sb.append("  - Bug Hunter Level 1\n");
        if ((flags & (1 << 6)) != 0) sb.append("  - HypeSquad Bravery\n");
        if ((flags & (1 << 7)) != 0) sb.append("  - HypeSquad Brilliance\n");
        if ((flags & (1 << 8)) != 0) sb.append("  - HypeSquad Balance\n");
        if ((flags & (1 << 9)) != 0) sb.append("  - Early Supporter\n");
        if ((flags & (1 << 14)) != 0) sb.append("  - Bug Hunter Level 2\n");
        if ((flags & (1 << 16)) != 0) sb.append("  - Verified Bot Developer\n");
        if ((flags & (1 << 17)) != 0) sb.append("  - Early Verified Bot Developer\n");
        if ((flags & (1 << 18)) != 0) sb.append("  - Discord Certified Moderator\n");
        if ((flags & (1 << 22)) != 0) sb.append("  - Active Developer\n");
        return sb.toString();
    }

    private String getMutualServers(String userId, String botToken) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Mutual Servers Analysis ===\n\n");

        try {
            HttpClient client = HttpClient.newHttpClient();

            // Fetch all guilds the bot is in
            HttpRequest guildsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(DISCORD_GUILDS_URL))
                    .header("Authorization", "Bot " + botToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> guildsResponse = client.send(guildsRequest, HttpResponse.BodyHandlers.ofString());

            if (guildsResponse.statusCode() != 200) {
                sb.append("❌ Failed to fetch bot's guilds. Status: ").append(guildsResponse.statusCode()).append("\n");
                return sb.toString();
            }

            String guildsJson = guildsResponse.body();

            // Parse guild IDs and names
            Pattern guildPattern = Pattern.compile("\\{[^}]*\"id\"\\s*:\\s*\"([^\"]+)\"[^}]*\"name\"\\s*:\\s*\"([^\"]+)\"[^}]*\\}");
            Matcher guildMatcher = guildPattern.matcher(guildsJson);

            int totalGuilds = 0;
            int mutualServers = 0;
            StringBuilder mutualServersList = new StringBuilder();

            while (guildMatcher.find()) {
                String guildId = guildMatcher.group(1);
                String guildName = guildMatcher.group(2);
                totalGuilds++;

                // Check if user is in this guild
                try {
                    String memberUrl = "https://discord.com/api/v10/guilds/" + guildId + "/members/" + userId;
                    HttpRequest memberRequest = HttpRequest.newBuilder()
                            .uri(URI.create(memberUrl))
                            .header("Authorization", "Bot " + botToken)
                            .header("Content-Type", "application/json")
                            .GET()
                            .build();

                    HttpResponse<String> memberResponse = client.send(memberRequest, HttpResponse.BodyHandlers.ofString());

                    if (memberResponse.statusCode() == 200) {
                        mutualServers++;
                        String memberJson = memberResponse.body();

                        // Extract user roles and join date
                        String joinedAt = extractJsonValue(memberJson, "joined_at");
                        String rolesArray = extractRolesFromMember(memberJson);

                        mutualServersList.append("\n").append(mutualServers).append(". ").append(guildName).append("\n");
                        mutualServersList.append("   Server ID: ").append(guildId).append("\n");
                        if (joinedAt != null && !joinedAt.equals("null")) {
                            mutualServersList.append("   Joined: ").append(joinedAt.substring(0, 10)).append("\n");
                        }
                        mutualServersList.append("   Roles: ").append(rolesArray).append("\n");
                    }
                } catch (Exception e) {
                    // User not in this guild or error occurred
                }

                // Small delay to avoid rate limiting
                Thread.sleep(100);
            }

            sb.append("Bot is in ").append(totalGuilds).append(" server(s)\n");
            sb.append("User is in ").append(mutualServers).append(" mutual server(s)\n");

            if (mutualServers > 0) {
                sb.append("\nMutual Servers:\n");
                sb.append(mutualServersList);
            } else {
                sb.append("\nNo mutual servers found.\n");
                sb.append("\nNote: The bot can only see servers where it has been added.\n");
                sb.append("If the user is in other servers without your bot, they won't be shown.\n");
            }

        } catch (Exception e) {
            sb.append("❌ Error checking mutual servers: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    private String extractRolesFromMember(String memberJson) {
        Pattern rolesPattern = Pattern.compile("\"roles\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher rolesMatcher = rolesPattern.matcher(memberJson);

        if (rolesMatcher.find()) {
            String rolesContent = rolesMatcher.group(1);
            if (rolesContent.trim().isEmpty()) {
                return "No roles";
            }

            // Count roles
            int roleCount = rolesContent.split(",").length;
            return roleCount + " role(s)";
        }

        return "No roles";
    }

    private void clearFields() {
        userIdField.setText("");
        botTokenField.setText("");
        checkServersCheckbox.setSelected(false);
        resultArea.setText("Enter a Discord User ID and click 'Fetch Info' to get started.\n\n" +
                "Tips:\n" +
                "- To get a user ID, enable Developer Mode in Discord settings\n" +
                "- Right-click any user and select 'Copy User ID'\n" +
                "- Bot token is optional but required for full details (username, avatar, badges)\n" +
                "- Check 'Check Mutual Servers' to see which servers both the bot and user are in");
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and show GUI
        SwingUtilities.invokeLater(() -> {
            DiscordInfoGUI gui = new DiscordInfoGUI();
            gui.setVisible(true);
        });
    }
}
