package com.windstudio.discordwl.bot.Commands.TabCompleters;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WhitelistTabCompleter implements TabCompleter {
    public Main plugin;
    public WhitelistTabCompleter(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ewhitelist")) {
            Player player = (Player) sender;
            List<String> autoCompletes = new ArrayList<>();
            switch (args.length) {
                case 1:
                    if (player.hasPermission("dswl.admin")) {
                        autoCompletes.add("enable");
                        autoCompletes.add("disable");
                        autoCompletes.add("list");
                        autoCompletes.add("add");
                        autoCompletes.add("remove");
                        autoCompletes.add("message");
                    }
                    return autoCompletes;
                case 2:
                    switch (args[0].toLowerCase()) {
                        case "disable":
                        case "enable":
                            autoCompletes.add("whitelist");
                            autoCompletes.add("service");
                            break;
                        case "remove":
                        case "add":
                            autoCompletes.add("player");
                            autoCompletes.add("admin");
                            break;
                        case "message":
                            autoCompletes.add("whitelist");
                            autoCompletes.add("service");
                            autoCompletes.add("blacklist");
                            break;
                        case "list":
                            autoCompletes.add("players");
                            autoCompletes.add("admins");
                            break;
                    }
                    return autoCompletes;
                case 3:
                    switch (args[0].toLowerCase()) {
                        case "add":
                            switch (args[1].toLowerCase()) {
                                case "player":
                                case "admin":
                                    Bukkit.getOnlinePlayers().forEach(p -> {
                                        autoCompletes.add(p.getName());
                                    });
                            }
                            break;
                        case "remove":
                            switch (args[1].toLowerCase()) {
                                case "player":
                                    switch (getString("Database.Type")) {
                                        case "SQLite":
                                            autoCompletes.addAll(plugin.getClassManager().getSqLiteWhitelistData().getPlayers());
                                            break;
                                        case "MySQL":
                                            autoCompletes.addAll(plugin.getClassManager().getMySQLWhitelistData().getPlayers());
                                            break;
                                    }
                                    break;
                                case "admin":
                                    switch (getString("Database.Type")) {
                                        case "SQLite":
                                            autoCompletes.addAll(plugin.getClassManager().getSqLiteWhitelistData().getAdministrators());
                                            break;
                                        case "MySQL":
                                            autoCompletes.addAll(plugin.getClassManager().getMySQLWhitelistData().getAdministrators());
                                            break;
                                    }
                                    break;
                            }
                            break;
                        case "message":
                            switch (args[1].toLowerCase()) {
                                case "whitelist":
                                case "service":
                                case "blacklist":
                                    autoCompletes.add("<message>");
                                    break;
                            }
                            break;
                    }
                    return autoCompletes;
            }
        }
        return null;
    }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
