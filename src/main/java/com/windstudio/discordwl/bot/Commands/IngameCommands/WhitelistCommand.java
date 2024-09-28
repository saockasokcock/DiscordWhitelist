package com.windstudio.discordwl.bot.Commands.IngameCommands;

import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import com.windstudio.discordwl.Main;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class WhitelistCommand implements CommandExecutor {
    private final Main plugin;
    public WhitelistCommand(Main plugin) {
        this.plugin = plugin;
    }
    static String[] args0 = {
            "enable",
            "disable",
            "add",
            "remove",
            "list",
            "message"
    };
    static String[] args1 = {
            "player",
            "admin",
            "whitelist",
            "blacklist",
            "service",
            "players",
            "admins"
    };
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
            List<String> args0List = Arrays.asList(args0);
            if (args.length == 0 || args[0].isEmpty() || !args0List.contains(args[0])) {
                sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                return true;
            }
            Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.InGameCommandUsedEvent event = new com.windstudio.discordwl.API.InGameCommandUsedEvent(sender, command.getName(), command);
                        Bukkit.getServer().getPluginManager().callEvent(event);
                    });
            List<String> args1List = Arrays.asList(args1);
            String message, cmdPlayer;
            switch (args[0]) {
                case "enable":
                    if (args.length != 2 || !args1List.contains(args[1]) || args[1].isEmpty()) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                        return true;
                    }
                    switch (args[1]) {
                        case "whitelist":
                            if (!plugin.getConfig().getBoolean("Plugin.Settings.EWhitelist.Enabled")) {
                                plugin.getConfig().set("Plugin.Settings.EWhitelist.Enabled", true);
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistOn"), new String[0]);
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyOn"), new String[0]);
                            }
                            return true;
                        case "service":
                            if (!plugin.getConfig().getBoolean("Plugin.Settings.EWhitelist.Maintenance")) {
                                plugin.getConfig().set("Plugin.Settings.EWhitelist.Maintenance", true);
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockOn"), new String[0]);
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyOn"), new String[0]);
                            }
                            return true;
                    }
                    return true;
                case "disable":
                    if (args.length != 2 || args[1].isEmpty() || !args1List.contains(args[1])) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                        return true;
                    }
                    switch (args[1]) {
                        case "whitelist":
                            if (plugin.getConfig().getBoolean("Plugin.Settings.EWhitelist.Enabled")) {
                                plugin.getConfig().set("Plugin.Settings.EWhitelist.Enabled", false);
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistOff"), new String[0]);
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyOff"), new String[0]);
                            }
                            return true;
                        case "service":
                            if (plugin.getConfig().getBoolean("Plugin.Settings.EWhitelist.Maintenance")) {
                                plugin.getConfig().set("Plugin.Settings.EWhitelist.Maintenance", false);

                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockOff"), new String[0]);
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyOff"), new String[0]);
                            }
                            return true;
                    }
                    return true;
                case "add":
                    if (args.length != 3 || args[1].isEmpty() || !args1List.contains(args[1])) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                        return true;
                    }
                    if (args[2].isEmpty()) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                        return true;
                    }
                    cmdPlayer = args[2];
                    switch (args[1]) {
                        case "player":
                            switch (getString("Database.Type")) {
                                case "SQLite":
                                    if (!plugin.getClassManager().getSqLiteWhitelistData().userPlayerExists(cmdPlayer)) {
                                        Date now = new Date();
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAdded"), new String[]{cmdPlayer});
                                        plugin.getClassManager().getSqLiteWhitelistData().addPlayer(cmdPlayer, "player", now);

                                    } else {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyAdded"), new String[0]);
                                    }
                                    return true;
                                case "MySQL":
                                    if (!plugin.getClassManager().getMySQLWhitelistData().userPlayerExists(cmdPlayer)) {
                                        Date now = new Date();
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAdded"), new String[]{cmdPlayer});
                                        plugin.getClassManager().getMySQLWhitelistData().addPlayer(cmdPlayer, "player", now);

                                    } else {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyAdded"), new String[0]);
                                    }
                                    return true;
                            }
                            return true;
                        case "admin":
                            switch (getString("Database.Type")) {
                                case "SQLite":
                                    if (!plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().contains(cmdPlayer)) {
                                        Date now = new Date();
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockAdded"), new String[]{cmdPlayer});
                                        plugin.getClassManager().getSqLiteWhitelistData().addPlayer(cmdPlayer, "administrator", now);

                                    } else {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyAdded"), new String[0]);
                                    }
                                    return true;
                                case "MySQL":
                                    if (!plugin.getClassManager().getMySQLWhitelistData().getAdministrators().contains(cmdPlayer)) {
                                        Date now = new Date();
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockAdded"), new String[]{cmdPlayer});
                                        plugin.getClassManager().getMySQLWhitelistData().addPlayer(cmdPlayer, "administrator", now);

                                    } else {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyAdded"), new String[0]);
                                    }
                                    return true;
                            }
                            return true;
                    }
                    return true;
                case "remove":
                    if (args.length != 3 || args[1].isEmpty() || !args1List.contains(args[1])) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                        return true;
                    }
                    if (args[2].isEmpty()) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                        return true;
                    }
                    cmdPlayer = args[2];
                    switch (args[1]) {
                        case "player":
                            switch (getString("Database.Type")) {
                                case "SQLite":
                                    if (plugin.getClassManager().getSqLiteWhitelistData().userPlayerExists(cmdPlayer)) {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistRemoved"), new String[]{cmdPlayer});
                                        plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", cmdPlayer);

                                    } else {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                                    }
                                    return true;
                                case "MySQL":
                                    if (plugin.getClassManager().getMySQLWhitelistData().userPlayerExists(cmdPlayer)) {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistRemoved"), new String[]{cmdPlayer});
                                        plugin.getClassManager().getMySQLWhitelistData().removePlayer("nickname", cmdPlayer);

                                    } else {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                                    }
                                    return true;
                            }
                            return true;
                        case "admin":
                            switch (getString("Database.Type")) {
                                case "SQLite":
                                    if (plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().contains(cmdPlayer)) {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockRemoved"), new String[]{cmdPlayer});
                                        plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", cmdPlayer);

                                    } else {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                                    }
                                    return true;
                                case "MySQL":
                                    if (plugin.getClassManager().getMySQLWhitelistData().getAdministrators().contains(cmdPlayer)) {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockRemoved"), new String[]{cmdPlayer});
                                        plugin.getClassManager().getMySQLWhitelistData().removePlayer("nickname", cmdPlayer);

                                    } else {
                                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                                    }
                                    return true;
                            }
                            return true;
                    }
                    return true;
                case "message":
                    if (args.length != 3 || args[1].isEmpty() || !args1List.contains(args[1]) || args[2].isEmpty()) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                        return true;
                    }
                    message = ColorManager.buildString(args, 2);
                    if (message.isEmpty()) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                        return true;
                    }
                    switch (args[1]) {
                        case "whitelist":
                            plugin.getConfig().set("EWhitelist.Message.NotWhitelisted", message);
                            plugin.saveConfig();
                            plugin.reloadConfig();
                            sendMessage(sender, plugin.getLanguageManager().get("WhitelistMsgSet"), new String[]{message});
                            return true;
                        case "service":
                            plugin.getConfig().set("EWhitelist.Message.OnMaintenance", message);
                            plugin.saveConfig();
                            plugin.reloadConfig();
                            sendMessage(sender, plugin.getLanguageManager().get("WhitelistMsgSet"), new String[]{message});
                            return true;
                        case "blacklist":
                            plugin.getConfig().set("EWhitelist.Message.Blacklisted", message);
                            plugin.saveConfig();
                            plugin.reloadConfig();
                            sendMessage(sender, plugin.getLanguageManager().get("WhitelistMsgSet"), new String[]{message});
                            return true;
                    }
                    
                    return true;
                case "list":
                    if (args.length != 2 || args[1].isEmpty() || !args1List.contains(args[1])) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                        return true;
                    }
                    switch (args[1]) {
                        case "players":
                            switch (getString("Database.Type")) {
                                case "SQLite":
                                    sender.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("WhitelistList").replaceAll("%s", StringUtils.join(plugin.getClassManager().getSqLiteWhitelistData().getPlayers(), ", ")).replaceAll("%p", String.valueOf(plugin.getClassManager().getSqLiteWhitelistData().getPlayers().size()))));
                                    return true;
                                case "MySQL":
                                    sender.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("WhitelistList").replaceAll("%s", StringUtils.join(plugin.getClassManager().getMySQLWhitelistData().getPlayers(), ", ")).replaceAll("%p", String.valueOf(plugin.getClassManager().getMySQLWhitelistData().getPlayers().size()))));
                                    return true;
                            }
                            return true;
                        case "admins":
                            switch (getString("Database.Type")) {
                                case "SQLite":
                                    sender.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("AdministratorsList").replaceAll("%s", StringUtils.join(plugin.getClassManager().getSqLiteWhitelistData().getAdministrators(), ", ")).replaceAll("%p", String.valueOf(plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().size()))));
                                    return true;
                                case "MySQL":
                                    sender.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("AdministratorsList").replaceAll("%s", StringUtils.join(plugin.getClassManager().getMySQLWhitelistData().getAdministrators(), ", ")).replaceAll("%p", String.valueOf(plugin.getClassManager().getMySQLWhitelistData().getAdministrators().size()))));
                                    return true;
                            }
                            return true;
                    }
                    return true;
            }
            sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
            return true;
        } else {
            plugin.getConsole().sendMessage(ColorManager.translate(" &e› &fYou should enable &eEWHITELIST&f feature in the config in order to use this command!"));
        }
        return false;
    }


    public void sendMessage(CommandSender sender, String path, String... placeholder) {
        sender.sendMessage(String.format(ColorManager.translate(path), (Object[])placeholder));
    }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}