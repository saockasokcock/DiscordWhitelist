package com.windstudio.discordwl.API;

import com.windstudio.discordwl.API.Cause.ErrorCause;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ErrorReceivedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String receivedNickname;
    private Member guildMember;
    private Channel receivedChannel;
    private ErrorCause errorCause;

    public ErrorReceivedEvent(Member guildMember, String receivedNickname, Channel receivedChannel, ErrorCause errorCause) {
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

    public ErrorCause getErrorCause() {
        return errorCause;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}