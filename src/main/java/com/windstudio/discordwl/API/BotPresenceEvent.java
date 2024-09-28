package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BotPresenceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private ReadyEvent readyEvent;
    private SelfUser selfUser;

    public BotPresenceEvent(ReadyEvent readyEvent, SelfUser selfUser) {
        this.readyEvent = readyEvent;
        this.selfUser = selfUser;
    }

    public ReadyEvent getReadyEvent() {
        return readyEvent;
    }

    public SelfUser getSelfUser() {
        return selfUser;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}