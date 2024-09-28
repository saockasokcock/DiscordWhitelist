package com.windstudio.discordwl.bot.Manager.Discord;

import com.windstudio.discordwl.API.Cause.KickCause;
import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.PlayerManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class PresenceManager extends ListenerAdapter {
    public Main plugin;
    public PresenceManager(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public void onReady(@NotNull ReadyEvent e) {
        if (getConfigBoolean("Configuration.Discord.Bot.Activity.Status&Activities")) {
            Status(e); Activities(e);
            Bukkit.getScheduler().runTask(plugin, () -> {
                        com.windstudio.discordwl.API.BotPresenceEvent event = new com.windstudio.discordwl.API.BotPresenceEvent(e, e.getJDA().getSelfUser());
                        Bukkit.getServer().getPluginManager().callEvent(event);
                    });
        }
    }

    private String getConfigPath(String path) {
        return plugin.getConfig().getString(path);
    }
    private boolean getConfigBoolean(String path) {
        return plugin.getConfig().getBoolean(path);
    }
    public void Status(ReadyEvent e) {
        switch (getConfigPath("Configuration.Discord.Bot.Activity.Status")) {
            case "Online":
                e.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
            break;
            case "DND":
                e.getJDA().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                break;
            case "Idle":
                e.getJDA().getPresence().setStatus(OnlineStatus.IDLE);
                break;
            case "Invisible":
                e.getJDA().getPresence().setStatus(OnlineStatus.INVISIBLE);
                break;
        }
    }
    public void Activities(ReadyEvent e) {
        switch (getConfigPath("Configuration.Discord.Bot.Activity.Type")) {
            case "Playing":
                e.getJDA().getPresence().setActivity(Activity.playing(getConfigPath("Configuration.Discord.Bot.Activity.Text").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size()))));
                break;
            case "Listening":
                e.getJDA().getPresence().setActivity(Activity.listening(getConfigPath("Configuration.Discord.Bot.Activity.Text").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size()))));
                break;
            case "Watching":
                e.getJDA().getPresence().setActivity(Activity.watching(getConfigPath("Configuration.Discord.Bot.Activity.Text").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size()))));
                break;
            case "Streaming":
                e.getJDA().getPresence().setActivity(Activity.streaming(getConfigPath("Configuration.Discord.Bot.Activity.Text").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size())), getConfigPath("Configuration.Discord.Bot.Activity.URL")));
                break;
            case "Competing":
                e.getJDA().getPresence().setActivity(Activity.competing(getConfigPath("Configuration.Discord.Bot.Activity.Text").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size()))));
                break;
        }
    }
}