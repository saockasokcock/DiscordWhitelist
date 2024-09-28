package com.windstudio.discordwl.API;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class InGameCommandUsedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String commandName;
    private Command command;
    private CommandSender sender;

    public InGameCommandUsedEvent(CommandSender sender, String commandName, Command command) {
        this.command = command;
        this.commandName = commandName;
        this.sender = sender;
    }

    public String getCommandName() {
        return commandName;
    }

    public Command getCommand() {
        return command;
    }

    public CommandSender getSender() {
        return sender;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}