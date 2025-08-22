package com.windstudio.discordwl.bot.Manager.Plugin.Vanish;

import com.windstudio.discordwl.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

//public class SayanVanish implements Listener {
//
//    Main plugin;
//    public SayanVanish(Main plugin) {
//        this.plugin = plugin;
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerUnVanish(BukkitUserUnVanishEvent event) {
//        Player player = event.getUser().player();
//        plugin.getPlayerManager().getOnlinePlayers().add(player);
//        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerVanish(BukkitUserVanishEvent event) {
//        Player player = event.getUser().player();
//        plugin.getPlayerManager().getOnlinePlayers().remove(player);
//        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
//    }
//}
