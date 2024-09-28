package com.windstudio.discordwl.API;

import com.windstudio.discordwl.API.Cause.KickCause;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerMaintenanceKickedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private KickCause cause;
    private Player player;

    public PlayerMaintenanceKickedEvent(String playerNickname, KickCause kickCause) {
        this.cause = cause;
        this.player = Bukkit.getPlayer(playerNickname);
    }

    public KickCause getCause() {
        return cause;
    }

    public Player getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}