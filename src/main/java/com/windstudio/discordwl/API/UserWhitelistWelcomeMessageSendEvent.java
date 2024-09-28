package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserWhitelistWelcomeMessageSendEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String whitelistedNickname;
    private Member member;
    private Channel whitelistedChannel;
    private Channel globalChannel;

    public UserWhitelistWelcomeMessageSendEvent(Member member, String whitelistedNickname, Channel whitelistedChannel, Channel globalChannel) {
        this.whitelistedNickname = whitelistedNickname;
        this.member = member;
        this.whitelistedChannel = whitelistedChannel;
        this.globalChannel = globalChannel;
    }

    public Channel getGlobalChannel() {
        return globalChannel;
    }

    public String getWhitelistedNickname() {
        return whitelistedNickname;
    }

    public Member getMember() {
        return member;
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