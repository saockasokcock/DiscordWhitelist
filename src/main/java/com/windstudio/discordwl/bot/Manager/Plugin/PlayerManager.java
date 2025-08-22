package com.windstudio.discordwl.bot.Manager.Plugin;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Discord.PresenceManager;
import de.myzelyam.api.vanish.VanishAPI;
import me.quantiom.advancedvanish.util.AdvancedVanishAPI;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerManager implements Listener {
    Main plugin;
    public ArrayList<Player> onlinePlayers = new ArrayList<Player>();
    public PlayerManager(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (String s : plugin.getConfig().getStringList("Plugin.Vanish.Supported")) {
            switch (s) {
                case "PremiumVanish", "SuperVanish" -> {
                    if (!VanishAPI.isInvisible(player)) getOnlinePlayers().add(player);
                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
                    return;
                }
                case "Essentials" -> {
                        com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                        if (essentials != null && !essentials.getVanishedPlayers().contains(player.getName())) getOnlinePlayers().add(player);
                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
                    return;
                }
                case "CMI" -> {
                        if (!com.Zrips.CMI.CMI.getInstance().getVanishManager().getVanishedOnlineList().contains(player.getUniqueId())) getOnlinePlayers().add(player);
                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
                    return;
                }
                case "AdvancedVanish" -> {
                        if (!AdvancedVanishAPI.INSTANCE.isPlayerVanished(player)) getOnlinePlayers().add(player);
                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
                    return;
                }
//                case "SayanVanish" -> {
//                        if (!SayanVanishBukkitAPI.bukkitUser(player.getUniqueId()).isVanished()) getOnlinePlayers().add(player);
//                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
//                    return;
//                }
            }
        }
        Bukkit.getScheduler().runTask(plugin, ()-> {
            com.windstudio.discordwl.API.PlayerJoinEvent e = new com.windstudio.discordwl.API.PlayerJoinEvent(event.getPlayer());
            Bukkit.getServer().getPluginManager().callEvent(e);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (String s : plugin.getConfig().getStringList("Plugin.Vanish.Supported")) {
            switch (s) {
                case "PremiumVanish", "SuperVanish" -> {
                    if (!VanishAPI.isInvisible(player)) getOnlinePlayers().remove(player);
                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
                    return;
                }
                case "Essentials" -> {
                    com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                    if (essentials != null && !essentials.getVanishedPlayers().contains(player.getName())) getOnlinePlayers().remove(player);
                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
                    return;
                }
                case "CMI" -> {
                    if (!com.Zrips.CMI.CMI.getInstance().getVanishManager().getVanishedOnlineList().contains(player.getUniqueId())) getOnlinePlayers().remove(player);
                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
                    return;
                }
                case "AdvancedVanish" -> {
                    if (!AdvancedVanishAPI.INSTANCE.isPlayerVanished(player)) getOnlinePlayers().remove(player);
                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
                    return;
                }
//                case "SayanVanish" -> {
//                    if (!SayanVanishBukkitAPI.bukkitUser(player.getUniqueId()).isVanished()) getOnlinePlayers().remove(player);
//                    plugin.getPresenceManager().Activities(plugin.getReadyEvent());
//                    return;
//                }

            }
        }
        Bukkit.getScheduler().runTask(plugin, ()-> {
            com.windstudio.discordwl.API.PlayerQuitEvent e = new com.windstudio.discordwl.API.PlayerQuitEvent(event.getPlayer());
            Bukkit.getServer().getPluginManager().callEvent(e);
        });
    }
    public ArrayList<Player> getOnlinePlayers() { return onlinePlayers; }
}