package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserNicknameReceivedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String receivedNickname;
    private Member guildMember;
    private Channel receivedChannel;

    public UserNicknameReceivedEvent(Member guildMember, String receivedNickname, Channel receivedChannel) {
        this.receivedNickname = receivedNickname;
        this.guildMember = guildMember;
        this.receivedChannel = receivedChannel;
    }

    public String getReceivedNickname() {
        return receivedNickname;
    }

    public Member getGuildMember() {
        return guildMember;
    }

    public Channel getReceivedChannel() {
        return receivedChannel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
