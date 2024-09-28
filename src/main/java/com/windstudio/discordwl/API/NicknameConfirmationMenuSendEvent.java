package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NicknameConfirmationMenuSendEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String whitelistNickname;
    private Member guildMember;
    private Channel whitelistedChannel;
    private Player player;

    public NicknameConfirmationMenuSendEvent(Member guildMember, String whitelistNickname, Channel whitelistedChannel) {
        this.whitelistNickname = whitelistNickname;
        this.guildMember = guildMember;
        this.whitelistedChannel = whitelistedChannel;
        this.player = Bukkit.getPlayer(whitelistNickname);
    }

    public String getWhitelistNickname() {
        return whitelistNickname;
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