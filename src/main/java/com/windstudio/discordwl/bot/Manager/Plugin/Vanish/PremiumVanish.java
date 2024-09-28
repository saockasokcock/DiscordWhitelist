package com.windstudio.discordwl.bot.Manager.Plugin.Vanish;

import com.windstudio.discordwl.Main;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.desktop.QuitEvent;
import java.util.ArrayList;

public class PremiumVanish implements Listener {

    Main plugin;
    public PremiumVanish(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUnVanish(PlayerShowEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().getOnlinePlayers().add(player);
        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanish(PlayerHideEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().getOnlinePlayers().remove(player);
        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
    }
}
