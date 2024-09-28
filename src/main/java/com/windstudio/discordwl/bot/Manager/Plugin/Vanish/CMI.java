package com.windstudio.discordwl.bot.Manager.Plugin.Vanish;

import com.Zrips.CMI.events.CMIPlayerUnVanishEvent;
import com.Zrips.CMI.events.CMIPlayerVanishEvent;
import com.windstudio.discordwl.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CMI implements Listener {

    Main plugin;
    public CMI(Main plugin) {
        this.plugin = plugin;
    }

    /*
    * This shitty plugin called "CMI" has a cranky fucking API
    * that i hate and won't implement into my plugin.
    * I think even after DiscordWhitelist recode and
    * CMI recode i will not do that, i hope
    * CMI will die soon.
    * */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUnVanish(CMIPlayerUnVanishEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().getOnlinePlayers().add(player);
        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanish(CMIPlayerVanishEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().getOnlinePlayers().remove(player);
        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
    }

}
