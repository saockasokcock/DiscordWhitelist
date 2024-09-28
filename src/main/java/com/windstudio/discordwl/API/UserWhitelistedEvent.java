package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserWhitelistedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String whitelistedNickname;
    private Member guildMember;
    private Channel whitelistedChannel;
    private Player player;

    public UserWhitelistedEvent(Member guildMember, String whitelistedNickname, Channel whitelistedChannel) {
        this.whitelistedNickname = whitelistedNickname;
        this.guildMember = guildMember;
        this.whitelistedChannel = whitelistedChannel;
        this.player = Bukkit.getPlayer(whitelistedNickname);
    }

    public String getWhitelistedNickname() {
        return whitelistedNickname;
    }

    public Member getGuildMember() {
        return guildMember;
    }

    public Channel getWhitelistedChannel() {
        return whitelistedChannel;
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