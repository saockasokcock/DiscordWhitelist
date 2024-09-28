package com.windstudio.discordwl.bot.Manager.Discord;

import com.windstudio.discordwl.API.Cause.ErrorCause;
import com.windstudio.discordwl.API.Cause.LogsCause;
import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getPlayer;

public class DiscordButtonManager extends ListenerAdapter {
    private final Main plugin;
    private final JDA jda;
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    public DiscordButtonManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.jda = Main.getJDA();
    }
    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {
        LogicA(e);
    }
    public void LogicA(@NotNull ButtonInteractionEvent e) {
        switch (e.getButton().getId()) {
            case "agree":
                e.getChannel().retrieveMessageById(e.getMessage().getEmbeds().get(0).getFooter().getText()).queue(message -> {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(message.getContentDisplay());
                    if (!e.getUser().equals(message.getAuthor())) {
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("MessageError")));
                        e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                        Bukkit.getScheduler().runTask(plugin, ()-> {
                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                    e.getMember(),
                                    message.getContentDisplay(),
                                    e.getChannel(),
                                    ErrorCause.FOREIGN_BUTTONS);
                            Bukkit.getServer().getPluginManager().callEvent(event1);
                        });
                        return;
                    }
                    backupWhitelistFile();
                    if (plugin.getConfig().getBoolean("Plugin.EWhitelist.Sync") && getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                        Bukkit.getScheduler().runTask(plugin, () -> p.setWhitelisted(true));
                        addToEWL(message.getContentDisplay());
                    } else if (!plugin.getConfig().getBoolean("Plugin.EWhitelist.Sync") && getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                        addToEWL(message.getContentDisplay());
                    } else if (plugin.getConfig().getBoolean("Plugin.EWhitelist.Sync") && !getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                        Bukkit.getScheduler().runTask(plugin, () -> p.setWhitelisted(true));
                    }
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.UserWhitelistedEvent event = new com.windstudio.discordwl.API.UserWhitelistedEvent(
                                        e.getMember(),
                                        message.getContentDisplay(),
                                        e.getChannel());
                                Bukkit.getServer().getPluginManager().callEvent(event);
                            });
                    plugin.getEmbedBuilder().setFooter(null);
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("AddedIn").replaceAll("%u", message.getContentDisplay()));
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    for (String s : getStringList("Plugin.Settings.Enabled")) {
                        TextChannel globalTextChannel = e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global"));
                        switch (s) {
                            case "WHITELIST_CHANGE_NAME":
                                try {
                                    e.getMember().modifyNickname(message.getContentDisplay()).queue();
                                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.UserNicknameChangedEvent event5 = new com.windstudio.discordwl.API.UserNicknameChangedEvent(
                                                e.getMember(),
                                                message.getContentDisplay(),
                                                e.getChannel(),
                                                e.getMember().getNickname());
                                        Bukkit.getServer().getPluginManager().callEvent(event5);
                                    });
                                } catch (Exception ex) {
                                    plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't change user's nickname. Seems that user has higher role that bot!"));
                                }
                                break;
                            case "WHITELIST_WELCOME_MESSAGE":
                                List<String> listW = plugin.getLanguageManager().getStringList("Welcome-Message");
                                String resultW = StringUtils.join(listW, "\n");
                                switch (getString("Plugin.Settings.Message.Welcome.Type")) {
                                    case "EMBED":
                                        if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                                            String mentions = message.getAuthor().getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WelcomeMessageTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("WelcomeMessageEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(resultW.replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mentions));
                                            globalTextChannel.sendTyping().queue();
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                                            .delay(Duration.ofSeconds(60))
                                                            .flatMap(Message::delete)
                                                            .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                    1, TimeUnit.SECONDS);


                                        } else {
                                            String mentions = message.getAuthor().getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WelcomeMessageTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("WelcomeMessageEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(resultW.replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mentions));
                                            globalTextChannel.sendTyping().queue();
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                                                    1, TimeUnit.SECONDS);

                                        }
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.UserWhitelistWelcomeMessageSendEvent event5 = new com.windstudio.discordwl.API.UserWhitelistWelcomeMessageSendEvent(
                                                    e.getMember(),
                                                    message.getContentDisplay(),
                                                    e.getChannel(),
                                                    e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")));
                                            Bukkit.getServer().getPluginManager().callEvent(event5);
                                        });
                                        break;
                                    case "TEXT":
                                        if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                                            String mentions = message.getAuthor().getAsMention();
                                            globalTextChannel.sendTyping().queue();
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessage(resultW.replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mentions))
                                                            .delay(Duration.ofSeconds(60))
                                                            .flatMap(Message::delete)
                                                            .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                    1, TimeUnit.SECONDS);

                                        } else {
                                            String mentions = message.getAuthor().getAsMention();
                                            globalTextChannel.sendTyping().queue();
                                            EXECUTOR.schedule(() ->  globalTextChannel.sendMessage(resultW.replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mentions)).queue(),
                                                    1, TimeUnit.SECONDS);

                                        }
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.UserWhitelistWelcomeMessageSendEvent event6 = new com.windstudio.discordwl.API.UserWhitelistWelcomeMessageSendEvent(
                                                    e.getMember(),
                                                    message.getContentDisplay(),
                                                    e.getChannel(),
                                                    e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")));
                                            Bukkit.getServer().getPluginManager().callEvent(event6);
                                        });
                                        break;
                                }
                                break;
                            case "WHITELIST_DM":
                                List<String> list = plugin.getLanguageManager().getStringList("DM-Message");
                                String result = StringUtils.join(list, "\n");
                                EXECUTOR.schedule(() -> message.getAuthor().openPrivateChannel().queue((messages) -> {
                                            plugin.getDMEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("DMEmbedColor")));
                                            plugin.getDMEmbedBuilder().setTitle(plugin.getLanguageManager().get("DMMessageTitle"));
                                            plugin.getDMEmbedBuilder().setDescription(result.replaceAll("%u", message.getContentDisplay()));
                                            messages.sendMessageEmbeds(plugin.getDMEmbedBuilder().build()).queue(null, new ErrorHandler()
                                                    .ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER));
                                        }),
                                        1, TimeUnit.SECONDS);
                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                    com.windstudio.discordwl.API.UserDirectMessageSendEvent event6 = new com.windstudio.discordwl.API.UserDirectMessageSendEvent(
                                            e.getMessage().getAuthor(),
                                            message.getContentDisplay(),
                                            e.getChannel());
                                    Bukkit.getServer().getPluginManager().callEvent(event6);
                                });
                                break;
                            case "WHITELIST_ROLE_ADD":
                                if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Add"), "disable")) {
                                    try {
                                        List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Whitelist.Add");
                                        for (String r : roleStringList) {
                                            if (e.getGuild().getRoleById(r) != null) {
                                                if (hasRole(e.getMember(), e.getGuild(), r)) continue;
                                                e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(r)).queue();
                                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                                    com.windstudio.discordwl.API.UserRoleAddEvent event8 = new com.windstudio.discordwl.API.UserRoleAddEvent(
                                                            e.getMember(),
                                                            message.getContentDisplay(),
                                                            e.getChannel(),
                                                            e.getGuild().getRoleById(r));
                                                    Bukkit.getServer().getPluginManager().callEvent(event8);
                                                });
                                            }
                                        }
                                    } catch (Exception ex) {
                                        plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't add role to user. Either user have higher role that bot, either roleID isn't correct!"));
                                    }
                                }
                                break;
                            case "WHITELIST_ROLE_REMOVE":
                                if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Remove"), "disable")) {
                                    try {
                                    List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Whitelist.Remove");
                                    for (String r : roleStringList) {
                                        if (e.getGuild().getRoleById(r) != null) {
                                            if (!hasRole(e.getMember(), e.getGuild(), r)) continue;
                                            e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(r)).queue();
                                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                                com.windstudio.discordwl.API.UserRoleRemoveEvent event9 = new com.windstudio.discordwl.API.UserRoleRemoveEvent(
                                                        e.getMember(),
                                                        message.getContentDisplay(),
                                                        e.getChannel(),
                                                        e.getGuild().getRoleById(r));
                                                Bukkit.getServer().getPluginManager().callEvent(event9);
                                            });
                                        }
                                    }
                                    } catch (Exception ex) {
                                        plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't remove role from user. Either user have higher role that bot, either roleID isn't correct!"));
                                    }
                                }
                                break;
                            case "LOGGING":
                                if (e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")) != null) {
                                String mention = message.getAuthor().getAsMention();
                                String discord = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
                                plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistLogEmbedTitle"));
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistLogEmbedDescription").replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mention).replaceAll("%d", discord));
                                EXECUTOR.schedule(() -> e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                        1, TimeUnit.SECONDS);
                                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.LogsSendEvent event10 = new com.windstudio.discordwl.API.LogsSendEvent(
                                                e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                                LogsCause.WHITELIST);
                                        Bukkit.getServer().getPluginManager().callEvent(event10);
                                    });
                                } else plugin.getConsole().sendMessage(ColorManager.translate("&c › &fField &cLogsChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                                break;
                        }
                    }
                    e.getInteraction().getMessage().delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_WEBHOOK, ErrorResponse.UNKNOWN_INTERACTION));
                    message.delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_WEBHOOK, ErrorResponse.UNKNOWN_INTERACTION));
                }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_WEBHOOK, ErrorResponse.UNKNOWN_INTERACTION));
                break;
            case "notagree":
                e.getChannel().retrieveMessageById(e.getMessage().getEmbeds().get(0).getFooter().getText()).queue((message) -> {
                    if (!e.getUser().equals(message.getAuthor())) {
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("MessageError")));
                        e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                        Bukkit.getScheduler().runTask(plugin, ()-> {
                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                    e.getMember(),
                                    message.getContentDisplay(),
                                    e.getChannel(),
                                    ErrorCause.FOREIGN_BUTTONS);
                            Bukkit.getServer().getPluginManager().callEvent(event1);
                        });
                        return;
                    }
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("TitleRefused")));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("MessageRefused")));
                    plugin.getEmbedBuilder().setTimestamp(Instant.now());
                    message.delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_WEBHOOK, ErrorResponse.UNKNOWN_INTERACTION));
                    e.getMessage().delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_WEBHOOK, ErrorResponse.UNKNOWN_INTERACTION));
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                e.getMember(),
                                message.getContentDisplay(),
                                e.getChannel(),
                                ErrorCause.REJECTED_CONFIRMATION);
                        Bukkit.getServer().getPluginManager().callEvent(event1);
                    });
                });
                break;
            case "success":
                if (getStringList("Plugin.Settings.Enabled").contains("REACTIONS")) {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("SuccessTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ReactionSuccess"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_ROLE_ADD")) {
                        if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Reactions.Add"), "disable")) {
                            if (e.getGuild().getRoleById(plugin.getConfig().getString("Configuration.Plugin.RoleID.Reactions.Add")) != null)
                                try {
                                    List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Reactions.Add");
                                    for (String r : roleStringList) {
                                        if (e.getGuild().getRoleById(r) != null) {
                                            if (hasRole(e.getMember(), e.getGuild(), e.getGuild().getRoleById(r).getId()))
                                                continue;
                                            e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(r)).queue();
                                        }
                                    }
                                } catch (Exception ex) {
                                    plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't add role to user. Seems that user has higher role that bot!"));
                                }
                        }
                    }
                    if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_ROLE_REMOVE")) {
                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Reactions.Remove"), "disable")) {
                        if (e.getGuild().getRoleById(plugin.getConfig().getString("Configuration.Plugin.RoleID.Reactions.Remove")) != null)
                            try {
                                List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Reactions.Remove");
                                for (String r : roleStringList) {
                                    if (e.getGuild().getRoleById(r) != null) {
                                        if (!hasRole(e.getMember(), e.getGuild(), e.getGuild().getRoleById(r).getId())) continue;
                                        e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(r)).queue();
                                    }
                                }
                            } catch (Exception ex) {
                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't remove role from user. Seems that user has higher role that bot!"));
                            }
                    }
                    }
                } else {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ReactionNotEnabled"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                e.getMember(),
                                null,
                                e.getChannel(),
                                ErrorCause.REACTIONS_NOT_ENABLED);
                        Bukkit.getServer().getPluginManager().callEvent(event1);
                    });
                }
                break;
            case "default":
                ArrayList<String> defaultWhitelistedPlayers = new ArrayList<String>();
                for (OfflinePlayer dwhitelisted : Bukkit.getWhitelistedPlayers()) {
                    defaultWhitelistedPlayers.add(dwhitelisted.getName());
                }
                String dwhitelistedPlayer = defaultWhitelistedPlayers.toString();
                dwhitelistedPlayer = dwhitelistedPlayer.substring(1, dwhitelistedPlayer.length() - 1);
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ListWhitelistedDefaultTitle"));
                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ListWhitelistedDefaultDescription").replaceAll("%p", "" + dwhitelistedPlayer + ""));
                plugin.getEmbedBuilder().setFooter(plugin.getLanguageManager().get("ListWhitelistedDefaultFooter").replaceAll("%p", String.valueOf(defaultWhitelistedPlayers.size())));
                e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                        .delay(Duration.ofSeconds(15))
                        .flatMap(InteractionHook::deleteOriginal)
                        .queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                
                break;
            case "our":
                List<String> list = plugin.getLanguageManager().getStringList("WhitelistListOurWhitelistChooseDescription");
                String result = StringUtils.join(list, "\n");
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseTitle"));
                plugin.getEmbedBuilder().setDescription(result.replaceAll("%0", plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseButtonPlayers")).replaceAll("%1", plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseButtonAdministrators")));
                e.replyEmbeds(plugin.getEmbedBuilder().build()).setActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.success("players", plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseButtonPlayers")), Button.primary("admins", plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseButtonAdministrators"))).setEphemeral(true)
                        .delay(Duration.ofSeconds(15))
                        .flatMap(InteractionHook::deleteOriginal)
                        .queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                

                break;
            case "players":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        switch (getString("Database.Type")) {
                            case "SQLite":
                                DoPlayersSQLite(e, plugin.getEmbedBuilder());
                                break;
                            case "MySQL":
                                DoPlayersMySQL(e, plugin.getEmbedBuilder());
                                break;
                            default:
                                DoPlayersSQLite(e, plugin.getEmbedBuilder());
                                break;
                        }
                    }
                }.runTaskAsynchronously(plugin);
                break;
            case "admins":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        switch (getString("Database.Type")) {
                            case "SQLite":
                                DoAdminsSQLite(e, plugin.getEmbedBuilder());
                                break;
                            case "MySQL":
                                DoAdminsMySQL(e, plugin.getEmbedBuilder());
                                break;
                            default:
                                DoAdminsSQLite(e, plugin.getEmbedBuilder());
                                break;
                        }
                    }
                }.runTaskAsynchronously(plugin);
                break;
        }
    }
    void backupWhitelistFile() {
        File whitelistFileBackup = new File("whitelist-backup-action.json");
        whitelistFileBackup.deleteOnExit();
        try {
            FileUtils.copyFile(new File("whitelist.json"), whitelistFileBackup);
        } catch (IOException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
    }
    public void DoPlayersSQLite(ButtonInteractionEvent e, EmbedBuilder eb) {
                if (plugin.getClassManager().getSqLiteWhitelistData().getPlayers() != null) {
                    String oWhitelisted = plugin.getClassManager().getSqLiteWhitelistData().getPlayers().toString();
                    oWhitelisted = oWhitelisted.substring(1, oWhitelisted.length() - 1);
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistDescription").replaceAll("%p", "" + oWhitelisted + ""));
                    eb.setFooter(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistFooter").replaceAll("%p", String.valueOf(plugin.getClassManager().getSqLiteWhitelistData().getPlayers().size())));
                    e.replyEmbeds(eb.build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(60))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    
                }
            }
    public void DoPlayersMySQL(ButtonInteractionEvent e, EmbedBuilder eb) {
                if (plugin.getClassManager().getMySQLWhitelistData().getPlayers() != null) {
                    String oWhitelisted = plugin.getClassManager().getMySQLWhitelistData().getPlayers().toString();
                    oWhitelisted = oWhitelisted.substring(1, oWhitelisted.length() - 1);
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistDescription").replaceAll("%p", "" + oWhitelisted + ""));
                    eb.setFooter(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistFooter").replaceAll("%p", String.valueOf(plugin.getClassManager().getMySQLWhitelistData().getPlayers().size())));
                    e.replyEmbeds(eb.build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(60))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    
                }
            }
    public void DoAdminsSQLite(ButtonInteractionEvent e, EmbedBuilder eb) {
                if (plugin.getClassManager().getSqLiteWhitelistData().getAdministrators() != null) {
                    String oWhitelisted = plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().toString();
                    oWhitelisted = oWhitelisted.substring(1, oWhitelisted.length() - 1);
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistDescription").replaceAll("%p", "" + oWhitelisted + ""));
                    eb.setFooter(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistFooter").replaceAll("%p", String.valueOf(plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().size())));
                    e.replyEmbeds(eb.build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(60))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    
                }
        }
    public void DoAdminsMySQL(ButtonInteractionEvent e, EmbedBuilder eb) {
                if (plugin.getClassManager().getMySQLWhitelistData().getAdministrators() != null) {
                    String oWhitelisted = plugin.getClassManager().getMySQLWhitelistData().getAdministrators().toString();
                    oWhitelisted = oWhitelisted.substring(1, oWhitelisted.length() - 1);
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistDescription").replaceAll("%p", "" + oWhitelisted + ""));
                    eb.setFooter(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistFooter").replaceAll("%p", String.valueOf(plugin.getClassManager().getMySQLWhitelistData().getAdministrators().size())));
                    e.replyEmbeds(eb.build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(60))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    
                }
            }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
    public void addToEWL(String player) {
        if (getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                public void run() {
                    Date now = new Date();
                    switch (getString("Database.Type")) {
                        case "SQLite":
                            plugin.getClassManager().getSqLiteWhitelistData().addPlayer(player, "player", now);

                            break;
                        case "MySQL":
                            plugin.getClassManager().getMySQLWhitelistData().addPlayer(player, "player", now);

                            break;
                    }
                } });
        }
    }
    public boolean hasRole(Member member, Guild guild, String roleID) {
        List<Role> memberRoles = member.getRoles();
        return memberRoles.contains(guild.getRoleById(roleID));
    }
}
