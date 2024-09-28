package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.User;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DiscordLeftEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String nickname;
    private User user;

    public DiscordLeftEvent(String nickname, User user) {
        this.nickname = nickname;
        this.user = user;
    }

    public String getNickname() {
        return nickname;
    }

    public User getUser() {
        return user;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}