package com.windstudio.discordwl.bot.Manager.Plugin.Vanish;

import com.windstudio.discordwl.Main;
import net.ess3.api.events.VanishStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Essentials implements Listener {

    Main plugin;
    public Essentials(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUnVanish(VanishStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        boolean vanished = event.getAffected().isVanished();
        if (vanished) {
            plugin.getPlayerManager().getOnlinePlayers().remove(player);
            plugin.getPresenceManager().Activities(plugin.getReadyEvent());
        } else {
            plugin.getPlayerManager().getOnlinePlayers().add(player);
            plugin.getPresenceManager().Activities(plugin.getReadyEvent());
        }
    }
}
