package com.windstudio.discordwl.bot.Manager.Plugin.Vanish;

import com.windstudio.discordwl.Main;
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent;
import me.quantiom.advancedvanish.event.PlayerVanishEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AdvancedVanish implements Listener {
    Main plugin;
    public AdvancedVanish(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUnVanish(PlayerUnVanishEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().getOnlinePlayers().add(player);
        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanish(PlayerVanishEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().getOnlinePlayers().remove(player);
        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
    }
}
