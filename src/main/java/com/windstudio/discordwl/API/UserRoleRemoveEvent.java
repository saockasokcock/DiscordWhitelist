package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserRoleRemoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String whitelistedNickname;
    private Member guildMember;
    private Channel whitelistedChannel;
    private Role role;

    public UserRoleRemoveEvent(Member guildMember, String whitelistedNickname, Channel whitelistedChannel, Role role) {
        this.whitelistedNickname = whitelistedNickname;
        this.guildMember = guildMember;
        this.whitelistedChannel = whitelistedChannel;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}