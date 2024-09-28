package com.windstudio.discordwl.API;

import com.windstudio.discordwl.API.Cause.LogsCause;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LogsSendEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Channel adminChannel;
    private LogsCause logsCause;

    public LogsSendEvent(Channel adminChannel, LogsCause logsCause) {
        this.adminChannel = adminChannel;
        this.logsCause = logsCause;
    }

    public Channel getAdminChannel() {
        return adminChannel;
    }

    public LogsCause getLogsCause() {
        return logsCause;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}