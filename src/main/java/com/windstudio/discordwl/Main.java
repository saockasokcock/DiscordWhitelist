package com.windstudio.discordwl;

import com.earth2me.essentials.EssentialsConf;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.jeff_media.updatechecker.UserAgentBuilder;
import com.windstudio.discordwl.bot.Commands.CommandManager;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkinfoCommand;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
import com.windstudio.discordwl.bot.Commands.IngameCommands.WhitelistCommand;
import com.windstudio.discordwl.bot.Commands.SlashCommands.SlashCommands;
import com.windstudio.discordwl.bot.Commands.TabCompleters.LinkingTabCompleter;
import com.windstudio.discordwl.bot.Commands.TabCompleters.WhitelistTabCompleter;
import com.windstudio.discordwl.bot.DataBase.MySQL.CPoolManager;
import com.windstudio.discordwl.bot.DataBase.MySQL.MySQLSecureData;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLiteSecureData;
import com.windstudio.discordwl.bot.Encryption;
import com.windstudio.discordwl.bot.Linking.DiscordLeftEvent;
import com.windstudio.discordwl.bot.Linking.PlayerEventHandler;
import com.windstudio.discordwl.bot.Manager.Discord.DiscordButtonManager;
import com.windstudio.discordwl.bot.Manager.Discord.DiscordMessageManager;
import com.windstudio.discordwl.bot.Manager.Discord.PresenceManager;
import com.windstudio.discordwl.bot.Manager.Plugin.*;
import com.windstudio.discordwl.bot.Manager.Plugin.Config.Updater;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Module;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.ModuleMain;
import com.windstudio.discordwl.bot.Manager.Plugin.Vanish.*;
import com.windstudio.discordwl.bot.Whitelist.PlayerPreLoginListener;
import de.myzelyam.api.vanish.VanishAPI;
import me.quantiom.advancedvanish.util.AdvancedVanishAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.sayandev.sayanvanish.api.SayanVanishAPI;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import static net.dv8tion.jda.api.utils.ChunkingFilter.ALL;

public class Main extends JavaPlugin {

    public HashMap<String, String> translate = new HashMap<>();

    /*
     * Modules
     */
    public Map<Module, Command> commandList = new HashMap<>();

    /*
     * Security
     */
    private final SecureRandom rnd = new SecureRandom();
    private final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /*
     * Service
     */
    public static Main plugin;
    public static Main getInstance() { return plugin; }
    private static JDA jda;
    public static JDA getJDA() { return jda; }
    public static ConsoleCommandSender console;
    private File configFile = null;

    /*
     * EmbedBuilder
     */
    private EmbedBuilder embedBuilder;
    private EmbedBuilder logsEmbedBuilder;
    private EmbedBuilder dmEmbedBuilder;

    /*
     * Class
     */
    private LanguageManager languageManager;
    private CPoolManager cPoolManager;
    private ClassManager classManager;
    private PlayerManager playerManager;
    private PresenceManager presenceManager;
    private ReadyEvent readyEvent;
    private CommandManager commandManager;


    /*
     * Database
     */
    private SQLite sqLite;
    protected MySQLSecureData mySQLSecureData;
    protected SQLiteSecureData sqLiteSecureData;

    /*
     * Version
     */
    private String EWhitelistVersion = "v1.25.3";
    private String LinkingVersion = "v1.07";
    private String ModulesVersion = "v1";
    private String APIVersion = "v25";
    String bukkitVersion;
    public static final String[] supportedVersions = {
            "1.14-R0.1-SNAPSHOT",
            "1.14.1-R0.1-SNAPSHOT",
            "1.14.2-R0.1-SNAPSHOT",
            "1.14.3-R0.1-SNAPSHOT",
            "1.14.4-R0.1-SNAPSHOT",
            "1.15-R0.1-SNAPSHOT",
            "1.15.1-R0.1-SNAPSHOT",
            "1.15.2-R0.1-SNAPSHOT",
            "1.16.1-R0.1-SNAPSHOT",
            "1.16.2-R0.1-SNAPSHOT",
            "1.16.3-R0.1-SNAPSHOT",
            "1.16.4-R0.1-SNAPSHOT",
            "1.16.5-R0.1-SNAPSHOT",
            "1.17-R0.1-SNAPSHOT",
            "1.17.1-R0.1-SNAPSHOT",
            "1.18-R0.1-SNAPSHOT",
            "1.18.1-R0.1-SNAPSHOT",
            "1.18.2-R0.1-SNAPSHOT",
            "1.19-R0.1-SNAPSHOT",
            "1.19.1-R0.1-SNAPSHOT",
            "1.19.2-R0.1-SNAPSHOT",
            "1.19.3-R0.1-SNAPSHOT",
            "1.19.4-R0.1-SNAPSHOT",
            "1.20-R0.1-SNAPSHOT",
            "1.20.1-R0.1-SNAPSHOT",
            "1.20.2-R0.1-SNAPSHOT",
            "1.20.3-R0.1-SNAPSHOT",
            "1.20.4-R0.1-SNAPSHOT",
            "1.20.5-R0.1-SNAPSHOT",
            "1.20.6-R0.1-SNAPSHOT",
            "1.21-R0.1-SNAPSHOT",
    };

    public void onLoad() {
        plugin = this;
        console = Bukkit.getServer().getConsoleSender();
        jda = getJDA();
        classManager = new ClassManager(this);
        embedBuilder = new EmbedBuilder();
        dmEmbedBuilder = new EmbedBuilder();
        logsEmbedBuilder = new EmbedBuilder();
        languageManager = new LanguageManager(plugin);
        sqLite = new SQLite(this);
        configFile = new File(getDataFolder(), "config.yml");
        commandManager = new CommandManager(this);
    }
    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        onLoad();
        new Metrics(this, 15019);
        if (Bukkit.getServer().getName().equals("Folia")) {
            getConsole().sendMessage(ColorManager.translate(" &e› &fAs that you run &eFolia&f in your server, many features may be broken! ..."));
            getConsole().sendMessage(ColorManager.translate("     &f...You can't use this plugin with &eFolia&f!"));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getConsole().sendMessage(ColorManager.translate("&r\n" +
                "\n" +
                "  &f┏━━━┳━━━┳┓┏┓┏┳┓     &r\n" +
                "  &f┗┓┏┓┃┏━┓┃┃┃┃┃┃┃     &7│ &5Discord&fWhitelist &7· &e" + getDescription().getVersion() + "&r\n" +
                "  &f ┃┃┃┃┗━━┫┃┃┃┃┃┃     &7│ &6ᴍᴏᴅᴇʀɴ ꜱᴇʀᴠᴇʀ ꜱᴇᴄᴜʀɪᴛʏ ᴀᴜᴛᴏᴍᴀᴛɪᴏɴ &r\n" +
                "  &f ┃┃┃┣━━┓┃┗┛┗┛┃┃ ┏┓  &7│ &r\n" +
                "  &f┏┛┗┛┃┗━┛┣┓┏┓┏┫┗━┛┃  &7│ &f© &bWIND STUDIO &7· &aLoading&7...&r\n" +
                "  &f┗━━━┻━━━┛┗┛┗┛┗━━━┛  &r\n"));
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        if (getSection("Configuration") == null || getSection("Plugin") == null) {
            getConsole().sendMessage(ColorManager.translate("&c › &fWe've detected that you are using old config!"));
            getConsole().sendMessage(ColorManager.translate("     &fCause of stability we strongly recommend you"));
            getConsole().sendMessage(ColorManager.translate("     &fto delete old one and recreate it!"));
        }

        bukkitVersion = Bukkit.getServer().getBukkitVersion();
        getConsole().sendMessage(ColorManager.translate("&e › &fVersion&6: &eAPI &6"+APIVersion+" &7| &eModules &6"+ModulesVersion +" &7| &eEWhitelist &6"+EWhitelistVersion+" &7| &eLinking &6"+LinkingVersion));
        checkForVersion();

        playerManager = new PlayerManager(this);

        if (plugin.getConfig().getBoolean("Service.Check-Updates") && !Bukkit.getServer().getName().equals("Folia")) {
            new UpdateChecker(this, UpdateCheckSource.SPIGOT, "97587")
                    .setDownloadLink("https://www.spigotmc.org/resources/discord-whitelist-third-generation.97587/")
                    .setChangelogLink("https://www.spigotmc.org/resources/discord-whitelist-third-generation.97587/updates")
                    .setNotifyOpsOnJoin(true)
                    .setNotifyByPermissionOnJoin("dswl.admin")
                    .setUserAgent(new UserAgentBuilder().addPluginNameAndVersion())
                    .checkEveryXHours(3)
                    .checkNow();
        }

        if (getResource("license.txt") == null) {
            getConsole().sendMessage(ColorManager.translate("&c › &fFile &clicense.txt &fin plugin jar does not exist. Seems you deleted it. Restore it by rollback it or re-downloading plugin."));
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        File license = new File(getDataFolder(), "license.txt");
        if (!license.exists()) {
            try {
                saveResource("license.txt", false);
            } catch (Exception e) {
                getConsole().sendMessage(ColorManager.translate("&c› &fCannot create license file, be sure to report this issue to the developer!"));
            }
        }
                switch (getString("Database.Type")) {
                    case "SQLite":
                        backupSQLiteFileOnStartup();
                        getConsole().sendMessage(ColorManager.translate("&e › &fConnecting to &eSQLite&f..."));
                        sqLite.connect();
                        getConsole().sendMessage(ColorManager.translate("&a › &fSuccessfully connected to &aSQLite&f!"));
                        sqLiteSecureData = new SQLiteSecureData(this);
                        if (!sqLiteSecureData.isSecureExists()) {
                            sqLiteSecureData.addSecure(Encryption.encrypt(randomString(16), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), Encryption.encrypt(randomString(16), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), 0);
                        }
                        break;
                    case "MySQL":
                        getConsole().sendMessage(ColorManager.translate("&e › &fConnecting to &eMySQL&f..."));
                        try {
                            cPoolManager = new CPoolManager(this);
                            getConsole().sendMessage(ColorManager.translate("&a › &fConnected to &eMySQL&f!&r"));
                            mySQLSecureData = new MySQLSecureData(this);
                            if (!mySQLSecureData.isSecureExists()) {
                                mySQLSecureData.addSecure(Encryption.encrypt(randomString(16), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), Encryption.encrypt(randomString(16), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), 0);
                            }
                        } catch (Exception e) {
                            getConsole().sendMessage(ColorManager.translate("&c › &fAn error occurred while connecting to &cMySQL&f:&r"));
                            getConsole().sendMessage(ColorManager.translate(e.toString()));
                            Bukkit.getPluginManager().disablePlugin(this);
                            return;
                        }
                        break;
                    default:
                        getConsole().sendMessage(ColorManager.translate("&e › &fEither plugin see DataBase type incorrectly, either you include incorrect DataBase type, ..."));
                        getConsole().sendMessage(ColorManager.translate("&e › &f... but plugin has force set DataBase type to &eSQLite&f! If that was mistake - contact with developer! "));
                        backupSQLiteFileOnStartup();
                        plugin.getConfig().set("Database.Type", "SQLite");
                        sqLite.connect();
                        sqLiteSecureData = new SQLiteSecureData(this);
                        if (!sqLiteSecureData.isSecureExists()) {
                            sqLiteSecureData.addSecure(Encryption.encrypt(randomString(16), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=Ye", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), Encryption.encrypt(randomString(16), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), 0);
                        }
                        break;
                }

        backupConfig(configFile);

        try {
            makeEncryption();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Updater.update(this, "config.yml", configFile, Collections.emptyList());
        } catch (IOException e) {
            getConsole().sendMessage(ColorManager.translate("&a › &fCannot update config.yml!"));
        }
        plugin.getLanguageManager().FileUpdate();
        if (!getString("Service.Version.Plugin").equals("v"+getDescription().getVersion())) {
            plugin.getConfig().set("Service.Version.Plugin", "v"+getDescription().getVersion());
            saveConfig();
        }
        if (!getString("Service.Version.API").equals(APIVersion)) {
            plugin.getConfig().set("Service.Version.API", APIVersion);
            saveConfig();
        }
        if (!getString("Service.Version.EWhitelist").equals(EWhitelistVersion)) {
            plugin.getConfig().set("Service.Version.EWhitelist", EWhitelistVersion);
            saveConfig();
        }
        if (!getString("Service.Version.Linking").equals(LinkingVersion)) {
            plugin.getConfig().set("Service.Version.Linking", LinkingVersion);
            saveConfig();
        }
        if (!getString("Service.Version.Modules").equals(ModulesVersion)) {
            plugin.getConfig().set("Service.Version.Modules", ModulesVersion);
            saveConfig();
        }

        try {
            try {
                jda = JDABuilder.createLight(getToken())
                        .addEventListeners(new SlashCommands(this), new PresenceManager(this), new LinkingCommand(this), new DiscordMessageManager(this), new DiscordLeftEvent(this), new DiscordButtonManager(this))
                        .enableIntents(
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.GUILD_MEMBERS,
                                GatewayIntent.MESSAGE_CONTENT
                                )
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .setChunkingFilter(ALL)
                        .build().awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (jda != null) {
                if (getString("Service.ServerID").equals("ReplaceThisWithYourServerID")) {
                    getConsole().sendMessage(ColorManager.translate("&a › &fYou must specify valid ServerID to run this plugin!\n"));
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
                getConsole().sendMessage(ColorManager.translate("&a › &fRegistering events..."));
                presenceManager = new PresenceManager(this);
                readyEvent = new ReadyEvent(jda);
                Bukkit.getPluginManager().registerEvents(new PlayerPreLoginListener(this), this);
                Bukkit.getPluginManager().registerEvents(playerManager, this);
                Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(jda, this), this);
                getConsole().sendMessage(ColorManager.translate("&a › &fRegistering commands..."));
                PluginCommand whitelistCommand = getCommand("ewhitelist");
                if (whitelistCommand == null)
                    throw new IllegalStateException("Command 'ewhitelist' is null, have you modified the plugin.yml file?");
                whitelistCommand.setExecutor((CommandExecutor) new WhitelistCommand(this));
                whitelistCommand.setTabCompleter((TabCompleter) new WhitelistTabCompleter(this));
                PluginCommand linkCommand = getCommand("link");
                if (linkCommand == null)
                    throw new IllegalStateException("Command 'link' is null, have you modified the plugin.yml file?");
                linkCommand.setExecutor((CommandExecutor) new LinkingCommand(this));
                linkCommand.setTabCompleter((TabCompleter) new LinkingTabCompleter(this));
                PluginCommand accountCommand = getCommand("account");
                if (accountCommand == null)
                    throw new IllegalStateException("Command 'account' is null, have you modified the plugin.yml file?");
                accountCommand.setExecutor((CommandExecutor) new LinkinfoCommand(this));
                
                if (jda.getGuildById(getString("Service.ServerID")) != null) {
                    jda.updateCommands().addCommands(
                            Commands.slash("whitelist", "Allows add/remove players to/from whitelist.")
                                    .setGuildOnly(true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "type", "Type of usage of command", true)
                                                    .addChoice("add", "add")
                                                    .addChoice("remove", "remove")
                                                    .addChoice("list", "list"))
                                    .addOption(OptionType.STRING, "username", "Insert nickname here to add/remove it ", false)
                                    .addOption(OptionType.USER, "user", "Choose user who will receive/lose role", false),
                            Commands.slash("list", "Shows list of server players")
                                    .setGuildOnly(true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "list", "Type of usage of command", false)
                                                    .addChoice("players", "players")
                                                    .addChoice("banned", "banned")),
                            Commands.slash("account", "Allows link discord account with minecraft account.")
                                    .setGuildOnly(true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "type", "Type of usage of command", true)
                                                    .addChoice("link", "link")
                                                    .addChoice("unlink", "unlink"))
                                    .addOption(OptionType.STRING, "username", "Insert nickname here to link with", true),
                            Commands.slash("setupreaction", "Allows setup message with reaction button to add/remove role to/from user"),
                            Commands.slash("checkwhitelist", "Provides information about user, is he in whitelist or not")
                                    .setGuildOnly(true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "whitelisttype", "Type of whitelist", true)
                                                    .addChoice("default", "default")
                                                    .addChoice("our", "our"))
                                    .addOption(OptionType.STRING, "username", "Insert nickname here to link with", true),
                            Commands.slash("checklink", "Provides information about user, is he linked his account or not")
                                    .setGuildOnly(true)
                                    .addOption(OptionType.STRING, "username", "Insert nickname here to check linked account with this name and UUID", false)
                                    .addOption(OptionType.STRING, "did", "Insert DiscordID here to check linked account with this DiscordID", false)
                    ).queue();
                } else {
                    getConsole().sendMessage(ColorManager.translate("&a › &cWe can't retrieve server by your ServerID, so we cant update slash-commands!"));
                }
                getConsole().sendMessage(ColorManager.translate("&a › &fRegistering interactions..."));
                    com.windstudio.discordwl.API.PluginStartupEvent event = new com.windstudio.discordwl.API.PluginStartupEvent(getPlugin());
                    Bukkit.getServer().getPluginManager().callEvent(event);

                lookForVanishPlugins();
                loadPlayers();

                backupWhitelistFileOnStartup();
                new ModuleMain().enable();
                long startedTime = System.currentTimeMillis() - startTime;
                getConsole().sendMessage(ColorManager.translate("&a› &5DiscordWhitelist&f has started &acorrectly&f in &a%time% &fms! Have a nice day &c<3\n")
                        .replaceAll("%time%", String.valueOf(startedTime)));
                changeIndicator("installed");
            } else {
                getConsole().sendMessage(ColorManager.translate("&a › &fJDA still not initialized, returning..."));
                changeIndicator("jda");
                Bukkit.getServer().getPluginManager().disablePlugin(this);
            }
        } catch (Exception ex) {
            getConsole().sendMessage(ColorManager.translate("&c › &fSomething has aborted normal plugin functionality. Check your config.yml!"));
            getConsole().sendMessage(ColorManager.translate("&c › &fSome possible reasons of that issue: "));
            getConsole().sendMessage(ColorManager.translate("   &7☉ &fYour token is invalid and plugin can't connect to bot;"));
            getConsole().sendMessage(ColorManager.translate("   &7☉ &fYour ServerID is invalid, so bot can't update SlashCommands on your server;"));
            getConsole().sendMessage(ColorManager.translate("   &7☉ &fYou forget to enable all &cPrivileged Gateway Intents&f into Developer Portal;"));
            getConsole().sendMessage(ColorManager.translate("   &7☉ &fYour plugin is outdated and you need to update it.\n"));
        }
    }

    @Override
    public void onDisable() {
            com.windstudio.discordwl.API.PluginShutdownEvent event = new com.windstudio.discordwl.API.PluginShutdownEvent(getPlugin());
            Bukkit.getServer().getPluginManager().callEvent(event);
        getConsole().sendMessage(ColorManager.translate("&r\n" +
                "\n" +
                "  &f┏━━━┳━━━┳┓┏┓┏┳┓    &r\n" +
                "  &f┗┓┏┓┃┏━┓┃┃┃┃┃┃┃    &7│ &5Discord&fWhitelist &7· &e" + getDescription().getVersion() + "&r\n" +
                "  &f ┃┃┃┃┗━━┫┃┃┃┃┃┃    &7│ &6ᴍᴏᴅᴇʀɴ ꜱᴇʀᴠᴇʀ ꜱᴇᴄᴜʀɪᴛʏ ᴀᴜᴛᴏᴍᴀᴛɪᴏɴ &r\n" +
                "  &f ┃┃┃┣━━┓┃┗┛┗┛┃┃ ┏┓ &7│ &r\n" +
                "  &f┏┛┗┛┃┗━┛┣┓┏┓┏┫┗━┛┃ &7│ &f© &bWIND STUDIO &7· &cShutting down&7...&r\n" +
                "  &f┗━━━┻━━━┛┗┛┗┛┗━━━┛ &r\n"));
        backupConfig(configFile);
        backupWhitelistFileOnShutdown();
        switch (getString("Database.Type")) {
            case "SQLite":
                getConsole().sendMessage(ColorManager.translate("&e › &fDisconnecting from &eSQLite&f..."));
                try {
                    sqLite.disconnect();
                    getConsole().sendMessage(ColorManager.translate("&a › &fDisconnected from &eSQLite&f!"));
                } catch (Exception e) {
                    getConsole().sendMessage(ColorManager.translate("&a › &fAn error occurred while disconnecting from &cSQLite&f:"));
                    getConsole().sendMessage(ColorManager.translate(e.toString()));
                }
                break;
            case "MySQL":
                getConsole().sendMessage(ColorManager.translate("&e › &fClosing &eMySQL&f connection..."));
                try {
                    getPoolManager().close(getPoolManager().getConnection(), null, null);
                    getConsole().sendMessage(ColorManager.translate("&a › &fClosed &aMySQL&f connection!"));
                } catch (Exception e) {
                    getConsole().sendMessage(ColorManager.translate("&a › &fAn error occurred while closing &cMySQL&f connection:"));
                    getConsole().sendMessage(ColorManager.translate(e.toString()));
                }
                break;
        }
        if (jda != null) jda.shutdown();
        getConsole().sendMessage(ColorManager.translate("&e › &fPlugin disabled"));
        plugin = null;
        }

        public void loadPlayers() {

            for (String s : getStringList("Plugin.Vanish.Supported")) {
                switch (s) {
                    case "SuperVanish", "PremiumVanish" -> {
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            if (!VanishAPI.isInvisible(player)) playerManager.getOnlinePlayers().add(player);
                        });
                        return;
                        }
                    case "Essentials" -> {
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                            if (essentials != null && !essentials.getVanishedPlayers().contains(player.getName())) playerManager.getOnlinePlayers().add(player);
                        });
                        return;
                    }
                    case "CMI" -> {
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            if (!com.Zrips.CMI.CMI.getInstance().getVanishManager().getVanishedOnlineList().contains(player.getUniqueId())) playerManager.getOnlinePlayers().add(player);
                        });
                        return;
                    }
                    case "AdvancedVanish" -> {
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            if (!AdvancedVanishAPI.INSTANCE.isPlayerVanished(player)) playerManager.getOnlinePlayers().add(player);
                        });
                        return;
                    }
                    case "SayanVanish" -> {
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            if (!SayanVanishAPI.getInstance().isVanished(player.getUniqueId())) playerManager.getOnlinePlayers().add(player);
                        });
                        return;
                    }
                }
            }
            presenceManager.Activities(readyEvent);
        }

        // Needs to be improved
        public void lookForVanishPlugins() {
            getConsole().sendMessage(ColorManager.translate(" &e› &fSearching for supported vanish plugins running on server..."));

            List<String> detected = new ArrayList<>();
            if (Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) { detected.add("PremiumVanish");
            Bukkit.getPluginManager().registerEvents(new PremiumVanish(this), this); }
            if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish")) { detected.add("SuperVanish");
            Bukkit.getPluginManager().registerEvents(new PremiumVanish(this), this); }
            if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) { detected.add("Essentials");
                Bukkit.getPluginManager().registerEvents(new Essentials(this), this); }
            if (Bukkit.getPluginManager().isPluginEnabled("CMI")) { detected.add("CMI");
                Bukkit.getPluginManager().registerEvents(new CMI(this), this); }
            if (Bukkit.getPluginManager().isPluginEnabled("AdvancedVanish")) { detected.add("AdvancedVanish");
                Bukkit.getPluginManager().registerEvents(new AdvancedVanish(this), this); }
            if (Bukkit.getPluginManager().isPluginEnabled("SayanVanish")) { detected.add("SayanVanish");
                Bukkit.getPluginManager().registerEvents(new SayanVanish(this), this); }

            if (detected.isEmpty()) {
                getConsole().sendMessage(ColorManager.translate(" &c› &fNo one vanish plugin running detected"));
                getConfig().set("Plugin.Vanish.Detected", "None");
            } else {
                getConsole().sendMessage(ColorManager.translate(" &a› &fDetected %count% vanish plugin(s)!").replaceAll("%count%", String.valueOf(detected.size())));
                getConfig().set("Plugin.Vanish.Detected", detected);
            }
        }

    public void checkForVersion() {
        getConsole().sendMessage(ColorManager.translate(" &e› &fThis server is running &e" + Bukkit.getServer().getName() + " &fversion &e"+ Bukkit.getServer().getBukkitVersion() +" &8(&7"+Bukkit.getBukkitVersion().split("-")[0]+"&8)&r"));
        getConsole().sendMessage(ColorManager.translate(" &e› &fScanning for supporting this server version..."));
        if (ArrayUtils.contains(supportedVersions, bukkitVersion)) {
            switch (bukkitVersion) {
                case "1.14-R0.1-SNAPSHOT":
                case "1.14.1-R0.1-SNAPSHOT":
                case "1.14.2-R0.1-SNAPSHOT":
                case "1.14.3-R0.1-SNAPSHOT":
                case "1.14.4-R0.1-SNAPSHOT":
                case "1.15-R0.1-SNAPSHOT":
                case "1.15.1-R0.1-SNAPSHOT":
                case "1.15.2-R0.1-SNAPSHOT":
                case "1.16.1-R0.1-SNAPSHOT":
                case "1.16.2-R0.1-SNAPSHOT":
                case "1.16.3-R0.1-SNAPSHOT":
                case "1.16.4-R0.1-SNAPSHOT":
                case "1.16.5-R0.1-SNAPSHOT":
                case "1.17-R0.1-SNAPSHOT":
                case "1.17.1-R0.1-SNAPSHOT":
                case "1.18-R0.1-SNAPSHOT":
                case "1.18.1-R0.1-SNAPSHOT":
                case "1.18.2-R0.1-SNAPSHOT":
                case "1.19-R0.1-SNAPSHOT":
                case "1.19.1-R0.1-SNAPSHOT":
                case "1.19.2-R0.1-SNAPSHOT":
                case "1.19.3-R0.1-SNAPSHOT":
                case "1.19.4-R0.1-SNAPSHOT":
                case "1.20-R0.1-SNAPSHOT":
                case "1.20.1-R0.1-SNAPSHOT":
                case "1.20.2-R0.1-SNAPSHOT":
                case "1.20.3-R0.1-SNAPSHOT":
                case "1.20.4-R0.1-SNAPSHOT":
                case "1.20.5-R0.1-SNAPSHOT":
                case "1.20.6-R0.1-SNAPSHOT":
                case "1.21-R0.1-SNAPSHOT":
                    getConsole().sendMessage(ColorManager.translate(" &a› &fThis version is supported!&r"));
                    break;
                default:
                    getConsole().sendMessage(ColorManager.translate(" &c› &fServer version where you're running on is not supported!"));
                    Bukkit.getPluginManager().disablePlugin(this);
            }
        } else {
            getConsole().sendMessage(ColorManager.translate(" &c› &fServer version where you're running on is not supported!"));
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public void makeEncryption() throws IOException {
        switch (getString("Database.Type")) {
            default:
            case "SQLite":
                if (getConfig().getBoolean("Service.TokenReplacement")) {
                    sqLiteSecureData.setTick(0);
                }
                if (!sqLiteSecureData.isTicked()) {
                    if (!getString("Service.Token").equals("ReplaceThisWithYourBotToken")) {
                        getConfig().set("Service.Token", Encryption.encrypt(getConfig().getString("Service.Token"), Encryption.decrypt(sqLiteSecureData.getCode(), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), Encryption.decrypt(sqLiteSecureData.getSalt(), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY=")));
                        sqLiteSecureData.setTick(1);
                        getConfig().set("Service.TokenReplacement", false);
                    }
            }
                break;
            case "MySQL":
                if (getConfig().getBoolean("Service.TokenReplacement")) {
                    mySQLSecureData.setTick(0);
                }
                if (!mySQLSecureData.isTicked()) {
                    if (!getString("Service.Token").equals("ReplaceThisWithYourBotToken")) {
                        getConfig().set("Service.Token", Encryption.encrypt(getConfig().getString("Service.Token"), Encryption.decrypt(mySQLSecureData.getCode(), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), Encryption.decrypt(mySQLSecureData.getSalt(), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY=")));
                        mySQLSecureData.setTick(1);
                        getConfig().set("Service.TokenReplacement", false);
                    }
                }
                break;
        }
        saveConfig();
        reloadConfig();
    }

    public String getToken() {
            switch (getString("Database.Type")) {
                default:
                case "SQLite":
                    if (!sqLiteSecureData.isTicked()) {
                        return getString("Service.Token");
                    } else {
                        return Encryption.decrypt(getConfig().getString("Service.Token"), Encryption.decrypt(sqLiteSecureData.getCode(), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), Encryption.decrypt(sqLiteSecureData.getSalt(), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="));
                    }

                case "MySQL":
                    if (!mySQLSecureData.isTicked()) {
                        return getString("Service.Token");
                    } else {
                        return Encryption.decrypt(getConfig().getString("Service.Token"), Encryption.decrypt(mySQLSecureData.getCode(), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="), Encryption.decrypt(mySQLSecureData.getSalt(), "5oZ74fCffp1LLMOZoCvU3c33LYxSq7tcI3o84hM1xYk=", "zpHkRYn8XkaVfOPdIBdZXV1RtUckDEGvAd3rXOpSIJY="));
                    }
            }
    }

    public void changeIndicator(String type) {
            switch (type) {
                case "token": getConfig().set("Service.InstallationIndicator", "TOKEN_ERROR"); break;
                case "jda": getConfig().set("Service.InstallationIndicator", "JDA_ERROR"); break;
                case "installed": getConfig().set("Service.InstallationIndicator", "INSTALLED"); break;
            }
        saveConfig();
    }

    public void backupConfig(File config) {
        switch (getString("Database.Type")) {
            default:
            case "SQLite":
                if (sqLiteSecureData == null) return;
                if (config.exists() && !sqLiteSecureData.isTicked()) {
                    File configBackup = new File(plugin.getDataFolder() + File.separator + "config-backup.yml");
                    configBackup.deleteOnExit();
                    try {
                        FileUtils.copyFile(config, configBackup);
                    } catch (IOException e) {
                        getConsole().sendMessage(e.toString());
                    }
                }
                break;

            case "MySQL":
                if (mySQLSecureData == null) return;
                if (config.exists() && !mySQLSecureData.isTicked()) {
                    File configBackup = new File(plugin.getDataFolder() + File.separator + "config-backup.yml");
                    configBackup.deleteOnExit();
                    try {
                        FileUtils.copyFile(config, configBackup);
                    } catch (IOException e) {
                        getConsole().sendMessage(e.toString());
                    }
                }
                break;
        }
    }

    public void backupSQLiteFileOnStartup() {
        File whitelistFileBackup = new File(plugin.getDataFolder() + File.separator + "Database.Settings.SQLite.DatabaseName" +"-backup.db");
        File whitelistFile = new File(plugin.getDataFolder() + File.separator + "Database.Settings.SQLite.DatabaseName" +".db");
        whitelistFileBackup.deleteOnExit();
        if (!whitelistFile.exists()) return;

        try {
            FileUtils.copyFile(new File(plugin.getDataFolder() + File.separator + "Database.Settings.SQLite.DatabaseName" +".db"), whitelistFileBackup);
        } catch (IOException e) {
            getConsole().sendMessage(e.toString());
        }
    }

    public void backupWhitelistFileOnStartup() {
        File whitelistFileBackup = new File("whitelist-backup-startup.json");
        whitelistFileBackup.deleteOnExit();

        try {
            FileUtils.copyFile(new File("whitelist.json"), whitelistFileBackup);
        } catch (IOException e) {
            getConsole().sendMessage(e.toString());
        }
    }

    public void backupWhitelistFileOnShutdown() {
        File whitelistFileBackup = new File("whitelist-backup-shutdown.json");
        whitelistFileBackup.deleteOnExit();

        try {
            FileUtils.copyFile(new File("whitelist.json"), whitelistFileBackup);
        } catch (IOException e) {
            getConsole().sendMessage(e.toString());
        }
    }

    public Plugin getPlugin() { return plugin; }

    public ConsoleCommandSender getConsole() { return console; }

    public String getString(String path){ return plugin.getConfig().getString(path); }

    public List<String> getStringList(String path){ return plugin.getConfig().getStringList(path); }

    public ConfigurationSection getSection(String path) { return plugin.getConfig().getConfigurationSection(path); }

    public ClassManager getClassManager() { return classManager; }

    public EmbedBuilder getEmbedBuilder() { return embedBuilder; }

    public EmbedBuilder getDMEmbedBuilder() { return dmEmbedBuilder; }

    public EmbedBuilder getLogsEmbedBuilder() { return logsEmbedBuilder; }

    public LanguageManager getLanguageManager() { return languageManager; }

    public CPoolManager getPoolManager() { return cPoolManager; }

    public CommandManager getCommandManager() { return this.commandManager; }

    public Map<Module, Command> getCommandList() { return this.commandList; }

    public PlayerManager getPlayerManager() { return playerManager; }

    public PresenceManager getPresenceManager() { return presenceManager; }

    public ReadyEvent getReadyEvent() { return readyEvent; }

}