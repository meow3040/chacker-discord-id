# Discord User Information Fetcher

A powerful Java application to fetch and analyze Discord user information using Discord's Snowflake ID system and API. Available in both **GUI** and **Console** modes.

![Java](https://img.shields.io/badge/Java-11+-orange)
![Discord API](https://img.shields.io/badge/Discord-API%20v10-5865F2)
![License](https://img.shields.io/badge/license-MIT-blue)

## Features

### 🔍 Snowflake ID Analysis (No Bot Token Required)
- **Account Creation Date & Time** - Extracted from the Discord ID structure
- **Account Age** - Calculated in years, months, and days
- **Snowflake Breakdown** - Worker ID, Process ID, Increment, Timestamp
- **ID Formats** - Binary and Hexadecimal representations
- **Account Type Estimation** - OG User, Old Account, Established, etc.

### 👤 Full User Details (Bot Token Required)
- **Username & Display Name**
- **Discriminator** (for legacy accounts)
- **Account Type** - User or Bot
- **Avatar URL** - Direct link to user's avatar (supports animated avatars)
- **Banner & Banner Color** - Profile banner information
- **Accent Color** - Profile accent color in hex and decimal
- **User Badges** - All public badges including:
  - Discord Employee
  - Partnered Server Owner
  - HypeSquad (Events, Bravery, Brilliance, Balance)
  - Bug Hunter (Level 1 & 2)
  - Early Supporter
  - Verified Bot Developer
  - Discord Certified Moderator
  - Active Developer

### 🌐 Mutual Servers Analysis (Bot Token Required)
- **Server Discovery** - Find all servers where both your bot and the user are members
- **Join Dates** - See when the user joined each mutual server
- **Role Information** - Number of roles the user has in each server
- **Statistics** - Total bot servers vs mutual servers count

## Screenshots

### GUI Mode
```
┌─────────────────────────────────────────────────────┐
│  Discord User Information Fetcher                   │
├─────────────────────────────────────────────────────┤
│  Discord User ID:   [________________________]      │
│  Bot Token:         [________________________]      │
│  ☑ Check Mutual Servers                             │
│  [Fetch Info]  [Clear]                              │
├─────────────────────────────────────────────────────┤
│  Results:                                           │
│  ┌───────────────────────────────────────────────┐  │
│  │                                               │  │
│  │  === Snowflake ID Analysis ===               │  │
│  │                                               │  │
│  │  User ID: 123456789012345678                 │  │
│  │  Account Created: 2016-04-30 11:18:25        │  │
│  │  Account Age: 7 years, 8 months, 5 days      │  │
│  │  ...                                          │  │
│  │                                               │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

## Requirements

- **Java 11** or higher (uses HttpClient and modern Java features)
- **Discord Bot Token** (optional, for full features)
- **Internet Connection** (to access Discord API)

## Installation

### 1. Clone or Download the Project
```bash
git clone https://github.com/meow3040/chacker-discord-id
cd untitled
```

### 2. Compile the Project
```bash
javac src/*.java
```

### 3. Run the Application

**GUI Mode (Default):**
```bash
java -cp src Main
```

**Console Mode:**
```bash
java -cp src Main console
```

## Usage

### Getting a Discord User ID

1. Open Discord and enable **Developer Mode**:
   - Settings → Advanced → Developer Mode (toggle ON)
2. Right-click any user in Discord
3. Click **"Copy User ID"**
4. Paste the ID into the application

### Getting a Discord Bot Token

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Click **"New Application"**
3. Give your application a name
4. Go to the **"Bot"** section in the left sidebar
5. Click **"Reset Token"** and copy your bot token
6. Paste the token into the application

⚠️ **IMPORTANT:** Never share your bot token publicly! It gives full access to your bot.

### Bot Permissions & Intents

For the mutual servers feature to work, your bot needs:

**Privileged Gateway Intents:**
- `SERVER MEMBERS INTENT` - Enable in Discord Developer Portal

**Bot Permissions:**
- Read Messages/View Channels
- Read Message History

### Adding Your Bot to Servers

To check mutual servers, your bot must be in the servers:

1. Go to Discord Developer Portal → Your Application → OAuth2 → URL Generator
2. Select scopes: `bot`
3. Select permissions: `Read Messages/View Channels`
4. Copy the generated URL and open it in your browser
5. Select servers to add your bot to

## Example Output

### Snowflake ID Analysis
```
=== Snowflake ID Analysis ===

User ID: 123456789012345678
ID in Binary: 110110110101010...
ID in Hex: 0x1B69B4B3C4D5E6F

Account Created: 2016-04-30 11:18:25
Unix Timestamp: 1461954091796
Account Age: 7 years, 8 months, 5 days

Snowflake Breakdown:
  Timestamp: 1461954091796 ms since Discord epoch
  Worker ID: 1
  Process ID: 0
  Increment: 0

Account Type Estimate:
  - Old Account
```

### Full User Details
```
=== Full Discord User Details ===

Username: example_user
Display Name: Example User
Account Type: USER

Avatar URL: https://cdn.discordapp.com/avatars/123.../avatar.png
Banner Color: #5865F2
Accent Color: #FF5733 (Decimal: 16734003)

Public Flags: 131072
User Badges:
  - Active Developer
  - Early Supporter
```

### Mutual Servers
```
=== Mutual Servers Analysis ===

Bot is in 15 server(s)
User is in 3 mutual server(s)

Mutual Servers:

1. My Cool Server
   Server ID: 1234567890123456789
   Joined: 2023-05-15
   Roles: 5 role(s)

2. Gaming Community
   Server ID: 9876543210987654321
   Joined: 2022-11-20
   Roles: 2 role(s)
```

## Privacy & Limitations

### What You CAN See:
- ✅ Any user's account creation date (from their ID)
- ✅ Public user profile information (username, avatar, badges)
- ✅ Servers where BOTH your bot and the user are members

### What You CANNOT See:
- ❌ All servers a user is in (only mutual servers)
- ❌ Private user information (email, phone, etc.)
- ❌ DM history or private messages
- ❌ Servers where your bot is not a member

This is due to Discord's privacy restrictions and API limitations.

## Technical Details

### Discord Snowflake IDs

Discord uses "Snowflake IDs" - unique 64-bit integers that encode information:

```
Snowflake Structure:
┌─────────────┬──────────┬──────────┬──────────┐
│ Timestamp   │ Worker   │ Process  │ Increment│
│ 42 bits     │ 5 bits   │ 5 bits   │ 12 bits  │
└─────────────┴──────────┴──────────┴──────────┘
```

- **Timestamp**: Milliseconds since Discord Epoch (Jan 1, 2015)
- **Worker ID**: Internal Discord worker ID
- **Process ID**: Internal Discord process ID
- **Increment**: Counter for IDs created in the same millisecond

### API Endpoints Used

- `GET /users/{user_id}` - Fetch user information
- `GET /users/@me/guilds` - Fetch bot's guilds
- `GET /guilds/{guild_id}/members/{user_id}` - Check guild membership

## Project Structure

```
untitled/
├── src/
│   ├── Main.java              # Entry point (launches GUI or console)
│   ├── DiscordInfoGUI.java    # GUI application with Swing
│   └── ...
├── README.md                   # This file
└── .gitignore
```

## Troubleshooting

### "Unauthorized! Check your bot token"
- Your bot token is invalid or expired
- Regenerate the token in Discord Developer Portal

### "User not found"
- The Discord ID doesn't exist
- Make sure you copied the full ID (18-19 digits)

### "No mutual servers found"
- Your bot is not in any servers with this user
- Add your bot to more servers

### GUI doesn't launch
- Make sure you have Java 11+ installed
- Check that Java Swing is available on your system
- Try running in console mode: `java -cp src Main console`

## Rate Limiting

The application includes built-in rate limiting protection:
- 100ms delay between server checks
- Prevents hitting Discord's API rate limits
- May take a few seconds for bots in many servers

## Security Best Practices

1. **Never commit your bot token** to version control
2. Store tokens in environment variables or config files
3. Use `.gitignore` to exclude sensitive files
4. Regenerate your token if accidentally exposed
5. Only grant necessary bot permissions

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

This tool is for educational and authorized use only. Always respect Discord's Terms of Service and API rate limits. Do not use this tool to:
- Harass or stalk users
- Scrape user data at scale
- Violate Discord's Terms of Service
- Infringe on user privacy

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check Discord API documentation: https://discord.com/developers/docs

## Acknowledgments

- Discord API Documentation
- Java Swing for GUI framework
- Discord Developer Community

---

**Made with ☕ by Java enthusiasts**
