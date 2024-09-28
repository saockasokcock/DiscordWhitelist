package com.windstudio.discordwl.API;

import com.windstudio.discordwl.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PluginStartupEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Plugin plugin;

    public PluginStartupEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}