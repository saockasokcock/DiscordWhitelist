package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserDirectMessageSendEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String whitelistedNickname;
    private User user;
    private Channel whitelistedChannel;

    public UserDirectMessageSendEvent(User user, String whitelistedNickname, Channel whitelistedChannel) {
        this.whitelistedNickname = whitelistedNickname;
        this.user = user;
        this.whitelistedChannel = whitelistedChannel;
    }


    public String getWhitelistedNickname() {
        return whitelistedNickname;
    }

    public User getUser() {
        return user;
    }

    public Channel getWhitelistedChannel() {
        return whitelistedChannel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}