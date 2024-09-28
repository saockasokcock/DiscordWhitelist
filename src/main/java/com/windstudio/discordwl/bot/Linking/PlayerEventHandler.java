package com.windstudio.discordwl.bot.Linking;

import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
import net.dv8tion.jda.api.JDA;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PlayerEventHandler implements Listener {
    public Main plugin;
    JDA jda;
    public PlayerEventHandler(JDA jda, Main plugin) {
        this.jda = jda;
        this.plugin = plugin;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String playerUUID = e.getPlayer().getUniqueId().toString();
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (getString("Database.Type")) {
                    case "SQLite":
                        if (plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
                            LinkingCommand.verifiedmembers.add(e.getPlayer().getUniqueId());
                            DoSQLite(e.getPlayer());
                        }
                        break;
                    case "MySQL":
                        if (plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
                            LinkingCommand.verifiedmembers.add(e.getPlayer().getUniqueId());
                            DoMySQL(e.getPlayer());
                        }
                        break;
                }
            }
        }.runTaskAsynchronously(plugin);
        Bukkit.getScheduler().runTask(plugin, ()-> {
            com.windstudio.discordwl.API.PlayerJoinEvent event = new com.windstudio.discordwl.API.PlayerJoinEvent(e.getPlayer());
            Bukkit.getServer().getPluginManager().callEvent(event);
        });
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        String playerUUID = e.getPlayer().getUniqueId().toString();
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (getString("Database.Type")) {
                    case "SQLite":
                        if (plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
                            LinkingCommand.verifiedmembers.remove(e.getPlayer().getUniqueId());
                            LinkingCommand.uuidCodeMap.remove(e.getPlayer().getUniqueId());
                            LinkingCommand.uuidIdMap.remove(e.getPlayer().getUniqueId());
                            DoSQLite(e.getPlayer());
                        }
                        break;
                    case "MySQL":
                        if (plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
                            LinkingCommand.verifiedmembers.remove(e.getPlayer().getUniqueId());
                            LinkingCommand.uuidCodeMap.remove(e.getPlayer().getUniqueId());
                            LinkingCommand.uuidIdMap.remove(e.getPlayer().getUniqueId());
                            DoMySQL(e.getPlayer());
                        }
                        break;
                }
            }
        }.runTaskAsynchronously(plugin);
        Bukkit.getScheduler().runTask(plugin, ()-> {
            com.windstudio.discordwl.API.PlayerQuitEvent event = new com.windstudio.discordwl.API.PlayerQuitEvent(e.getPlayer());
            Bukkit.getServer().getPluginManager().callEvent(event);
        });
    }
    public void DoSQLite(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (getStringList("Plugin.Settings.Enabled").contains("LINKING_LEFT_USERS_REMOVE") && plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE uuid=?");
                preparedStatement.setString(1, playerUUID);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String uuID = resultSet.getString("uuid");
                    String nickname = resultSet.getString("nickname");
                    String discord = resultSet.getString("discord");
                    String did = resultSet.getString("discord_id");
                    String date = resultSet.getString("linking_date");
                    if (jda.getGuildById(getString("Service.ServerID")).getMemberById(did) == null) {
                        player.setWhitelisted(false);
                        if (getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                            plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", nickname);
                            
                        }
                        player.kickPlayer(ColorManager.translate(plugin.getLanguageManager().get("LeftDiscordKickReason")));
                        plugin.getClassManager().getUserdata().deleteInformationFromUserProfile("uuid", player.getUniqueId().toString());
                    }

                }
                resultSet.close(); preparedStatement.close();
            } catch (SQLException ex) {
                ex.printStackTrace(); }
        }
    }
    public void DoMySQL(Player player) {
        String playerUUID = player.getUniqueId().toString();
                if (getStringList("Plugin.Settings.Enabled").contains("LINKING_LEFT_USERS_REMOVE") && plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
                    PreparedStatement preparedStatement = null;
                    ResultSet resultSet = null;
                    try {
                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("Database.Settings.MySQL.TableName.Linking") + " WHERE uuid=?");
                        preparedStatement.setString(1, playerUUID);
                        resultSet = preparedStatement.executeQuery();
                        while (resultSet.next()) {
                            String uuID = resultSet.getString("uuid");
                            String nickname = resultSet.getString("nickname");
                            String discord = resultSet.getString("discord");
                            String did = resultSet.getString("discord_id");
                            String date = resultSet.getString("linking_date");
                            if (jda.getGuildById(getString("Service.ServerID")).getMemberById(did) == null) {
                                player.setWhitelisted(false);
                                if (getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                                    plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", nickname);
                                    
                                }
                                player.kickPlayer(ColorManager.translate(plugin.getLanguageManager().get("LeftDiscordKickReason")));
                                plugin.getClassManager().getUserdata().deleteInformationFromUserProfile("uuid", player.getUniqueId().toString());
                            }

                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } finally {
                        plugin.getPoolManager().close(null, preparedStatement, resultSet);
                    }
                }
    }
    public List<String> getStringList(String path){ return plugin.getConfig().getStringList(path); }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
