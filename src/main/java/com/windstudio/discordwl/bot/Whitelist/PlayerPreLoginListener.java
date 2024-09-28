package com.windstudio.discordwl.bot.Whitelist;

import com.windstudio.discordwl.API.Cause.KickCause;
import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PlayerPreLoginListener implements Listener {
    public Main plugin;
    public PlayerPreLoginListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.getConfig().getStringList("Configuration.Plugin.Blacklist.Nickname").contains(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(getString("Plugin.Settings.EWhitelist.Message.Blacklisted")));
            Bukkit.getScheduler().runTask(plugin, ()-> {
                com.windstudio.discordwl.API.PlayerBlacklistedKickedEvent e = new com.windstudio.discordwl.API.PlayerBlacklistedKickedEvent(event.getName(), KickCause.BLACKLISTED);
                Bukkit.getServer().getPluginManager().callEvent(e);
            });
        }
        if (getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                    switch(getString("Database.Type")) {
                        case "SQLite":
                            DoSQLite(event);
                            break;
                        case "MySQL":
                            DoMySQL(event);
                            break;
                    }
                }
        }
        public void DoSQLite(AsyncPlayerPreLoginEvent event) {
            if (plugin.getConfig().getBoolean("Plugin.Settings.EWhitelist.Maintenance") && !plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().contains(event.getName())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(getString("Plugin.Settings.EWhitelist.Message.OnMaintenance")));
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    com.windstudio.discordwl.API.PlayerMaintenanceKickedEvent e = new com.windstudio.discordwl.API.PlayerMaintenanceKickedEvent(event.getName(), KickCause.MAINTENANCE);
                    Bukkit.getServer().getPluginManager().callEvent(e);
                });
            }
            if (plugin.getConfig().getBoolean("Plugin.Settings.EWhitelist.Enabled") &&
                    !plugin.getClassManager().getSqLiteWhitelistData().getPlayers().contains(event.getName())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(getString("Plugin.Settings.EWhitelist.Message.NotWhitelisted")));
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    com.windstudio.discordwl.API.PlayerNotWhitelistedKickedEvent e = new com.windstudio.discordwl.API.PlayerNotWhitelistedKickedEvent(event.getName(), KickCause.NOT_WHITELISTED);
                    Bukkit.getServer().getPluginManager().callEvent(e);
                });
            }
        }
    public void DoMySQL(AsyncPlayerPreLoginEvent event) {
        if (plugin.getConfig().getBoolean("Plugin.Settings.EWhitelist.Maintenance") && !plugin.getClassManager().getMySQLWhitelistData().getAdministrators().contains(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(getString("Plugin.Settings.EWhitelist.Message.OnMaintenance")));
            Bukkit.getScheduler().runTask(plugin, ()-> {
                com.windstudio.discordwl.API.PlayerMaintenanceKickedEvent e = new com.windstudio.discordwl.API.PlayerMaintenanceKickedEvent(event.getName(), KickCause.MAINTENANCE);
                Bukkit.getServer().getPluginManager().callEvent(e);
            });
        }
        if (plugin.getConfig().getBoolean("Plugin.Settings.EWhitelist.Enabled") &&
                !plugin.getClassManager().getMySQLWhitelistData().getPlayers().contains(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(getString("Plugin.Settings.EWhitelist.Message.NotWhitelisted")));
            Bukkit.getScheduler().runTask(plugin, ()-> {
                com.windstudio.discordwl.API.PlayerNotWhitelistedKickedEvent e = new com.windstudio.discordwl.API.PlayerNotWhitelistedKickedEvent(event.getName(), KickCause.NOT_WHITELISTED);
                Bukkit.getServer().getPluginManager().callEvent(e);
            });
        }
    }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
