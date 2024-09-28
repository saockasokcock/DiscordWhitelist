package com.windstudio.discordwl.API;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SlashCommandUsedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String commandName;
    private String commandID;
    private Member member;

    public SlashCommandUsedEvent(Member member, String commandName, String commandID) {
        this.commandName = commandName;
        this.member = member;
        this.commandID = commandID;
    }

    public String getCommandID() {
        return commandID;
    }

    public String getCommandName() {
        return commandName;
    }

    public Member getMember() {
        return member;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}