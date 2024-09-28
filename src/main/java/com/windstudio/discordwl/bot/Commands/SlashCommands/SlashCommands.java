package com.windstudio.discordwl.bot.Commands.SlashCommands;

import com.windstudio.discordwl.API.Cause.ErrorCause;
import com.windstudio.discordwl.API.Cause.LogsCause;
import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SlashCommands extends ListenerAdapter implements Listener {
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private final Main plugin;
    public SlashCommands(Main plugin) { this.plugin = plugin; }
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Bukkit.getScheduler().runTask(plugin, ()-> {
            com.windstudio.discordwl.API.SlashCommandUsedEvent e = new com.windstudio.discordwl.API.SlashCommandUsedEvent(event.getMember(), event.getName(), event.getCommandId());
            Bukkit.getServer().getPluginManager().callEvent(e);
        });
        switch (event.getName()) {
            case "whitelist":
                if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Admin"), "disable")) {
                    try {
                        List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Admin");
                        for (String r : roleStringList) {
                            if (event.getGuild().getRoleById(r) != null) {
                                if (hasAtLeastRole(event.getMember(), event.getGuild(), roleStringList)) continue;
                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                plugin.getEmbedBuilder().setDescription(null);
                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                        .delay(Duration.ofSeconds(15))
                                        .flatMap(InteractionHook::deleteOriginal)
                                        .queue(null, new ErrorHandler()
                                                .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                    com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                            event.getMember(),
                                            event.getMember().getNickname(),
                                            event.getChannel(),
                                            ErrorCause.HAVE_NO_ROLE);
                                    Bukkit.getServer().getPluginManager().callEvent(event1);
                                });
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't add role to user. Either user have higher role that bot, either roleID isn't correct!"));
                    }
                }
                OptionMapping typeOption = event.getOption("type");
                OptionMapping nickOption = event.getOption("username");
                if (typeOption == null) {
                    event.reply("You need to choose one of this options: add/remove").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                String TYPE = typeOption.getAsString();
                switch (TYPE) {
                    case "add":
                        if (nickOption == null) {
                            event.reply("You need to write valid nickname to add/remove it!").setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            return;
                        }
                        String NICK = nickOption.getAsString();
                        if (getStringList("Plugin.Settings.Enabled").contains("REGEX_CHECK")) {
                            if (getStringList("Plugin.Settings.Enabled").contains("BEDROCK_SUPPORT") && !NICK.matches("^(["+getString("Plugin.Settings.BedrockSymbol")+"])?([a-zA-Z0-9_ ]{3,16})$")) {
                                err(event);
                            } else if (!NICK.matches("^\\w{3,16}$")) {
                                err(event);
                            }
                        }
                        OfflinePlayer p = Bukkit.getOfflinePlayer(NICK);
                        if (!p.isWhitelisted()) {
                            backupWhitelistFile();
                            Bukkit.getScheduler().runTask(plugin, () -> p.setWhitelisted(true));
                            if (getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                    public void run() {
                                        Date now = new Date();
                                        switch (getString("Database.Type")) {
                                            case "SQLite":
                                                plugin.getClassManager().getSqLiteWhitelistData().addPlayer(NICK, "player", now);
                                                
                                                break;
                                            case "MySQL":
                                                plugin.getClassManager().getMySQLWhitelistData().addPlayer(NICK, "player", now);
                                                
                                                break;
                                        }
                                    } });
                            }
                            if (event.getOption("user") != null) {
                                OptionMapping userOption = event.getOption("user");
                                Member Member = userOption.getAsMember();
                                if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_CHANGE_NAME")) {
                                    try {
                                        EXECUTOR.schedule(() -> Member.modifyNickname(NICK).queue(),
                                                1, TimeUnit.SECONDS);
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.UserNicknameChangedEvent event5 = new com.windstudio.discordwl.API.UserNicknameChangedEvent(
                                                    Member,
                                                    NICK,
                                                    event.getChannel(),
                                                    event.getMember().getNickname());
                                            Bukkit.getServer().getPluginManager().callEvent(event5);
                                        });
                                    } catch (Exception ex) {
                                        plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't change user's nickname. Seems that user has higher role that bot!"));
                                    }
                                }
                                if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_ROLE_ADD")) {
                                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Add"), "disable")) {
                                        if (Member != null) {
                                            try {
                                                List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Whitelist.Add");
                                                for (String r : roleStringList) {
                                                    if (event.getGuild().getRoleById(r) != null) {
                                                        if (hasRole(event.getMember(), event.getGuild(), event.getGuild().getRoleById(r).getId()))
                                                            continue;
                                                        event.getGuild().addRoleToMember(Member, event.getGuild().getRoleById(r)).queue();
                                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                                            com.windstudio.discordwl.API.UserRoleAddEvent event8 = new com.windstudio.discordwl.API.UserRoleAddEvent(
                                                                    event.getMember(),
                                                                    Member.getNickname(),
                                                                    event.getChannel(),
                                                                    event.getGuild().getRoleById(r));
                                                            Bukkit.getServer().getPluginManager().callEvent(event8);
                                                        });
                                                    }
                                                }
                                            } catch (Exception ex) {
                                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't add role to user. Either user have higher role that bot, either roleID isn't correct!"));
                                            }
                                        }
                                    }
                                }
                                if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_ROLE_REMOVE")) {
                                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Remove"), "disable")) {
                                        if (Member != null) {
                                            try {
                                                if (event.getGuild().getRoleById(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Remove")) != null) {
                                                    List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Whitelist.Remove");
                                                    for (String r : roleStringList) {
                                                        if (event.getGuild().getRoleById(r) != null) {
                                                            if (!hasRole(event.getMember(), event.getGuild(), event.getGuild().getRoleById(r).getId()))
                                                                continue;
                                                            event.getGuild().removeRoleFromMember(Member, event.getGuild().getRoleById(r)).queue();
                                                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                                                com.windstudio.discordwl.API.UserRoleRemoveEvent event8 = new com.windstudio.discordwl.API.UserRoleRemoveEvent(
                                                                        event.getMember(),
                                                                        Member.getNickname(),
                                                                        event.getChannel(),
                                                                        event.getGuild().getRoleById(r));
                                                                Bukkit.getServer().getPluginManager().callEvent(event8);
                                                            });
                                                        }
                                                    }
                                                }
                                            } catch (Exception ex) {
                                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't remove role from user. Either user have higher role that bot, either roleID isn't correct!"));
                                            }
                                        }
                                    }
                                }
                            }
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.UserWhitelistedEvent eve = new com.windstudio.discordwl.API.UserWhitelistedEvent(event.getOption("user").getAsMember(), NICK, event.getChannel());
                                        Bukkit.getServer().getPluginManager().callEvent(eve);
                                    });
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("SuccessTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistedSuccessful").replaceAll("%p", NICK));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.INVALID_FORM_BODY));
                            if (getStringList("Plugin.Settings.Enabled").contains("LOGGING")) {
                                if (event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")) != null) {
                                String mention = event.getInteraction().getMember().getAsMention();
                                String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                                plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistLogEmbedTitle"));
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistSlashCommandAddLogEmbedDescription").replaceAll("%a", mention).replaceAll("%d", discord).replaceAll("%p", NICK));
                                event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                                    com.windstudio.discordwl.API.LogsSendEvent event1 = new com.windstudio.discordwl.API.LogsSendEvent(
                                            event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                            LogsCause.WHITELIST);
                                    Bukkit.getServer().getPluginManager().callEvent(event1);
                                } else plugin.getConsole().sendMessage(ColorManager.translate("&c › &fField &cLogsChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                            }
                        } else {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistedAlready").replaceAll("%p", NICK));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                event.getMember(),
                                                event.getMember().getNickname(),
                                                event.getChannel(),
                                                ErrorCause.ALREADY_WHITELISTED);
                            Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                        }
                        break;
                    case "remove":
                        if (nickOption == null) {
                            event.reply("You need to write valid nickname to add/remove it!").setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            return;
                        }
                        String NICKNAME = nickOption.getAsString();
                        if (getStringList("Plugin.Settings.Enabled").contains("REGEX_CHECK")) {
                            if (getStringList("Plugin.Settings.Enabled").contains("BEDROCK_SUPPORT") && !NICKNAME.matches("^([" + getString("Plugin.Settings.BedrockSymbol") + "])?([a-zA-Z0-9_ ]{3,16})$")) {
                                err(event);
                            } else if (!NICKNAME.matches("^\\w{3,16}$")) {
                                err(event);
                            }
                        }
                        OfflinePlayer player = Bukkit.getOfflinePlayer(NICKNAME);
                        if (player.isWhitelisted()) {
                            backupWhitelistFile();
                            Bukkit.getScheduler().runTask(plugin, () -> player.setWhitelisted(false));
                            if (getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                    public void run() {
                                        switch (getString("Database.Type")) {
                                            case "SQLite":
                                                plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", NICKNAME);
                                                
                                                break;
                                            case "MySQL":
                                                plugin.getClassManager().getMySQLWhitelistData().removePlayer("nickname", NICKNAME);
                                                
                                                break;
                                        }
                                    } });
                            }
                            if (event.getOption("user") != null) {
                                OptionMapping userOption1 = event.getOption("user");
                                Member Member1 = userOption1.getAsMember();
                                if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_CHANGE_NAME")) {
                                    try {
                                        EXECUTOR.schedule(() -> Member1.modifyNickname(null).queue(),
                                                1, TimeUnit.SECONDS);
                                        com.windstudio.discordwl.API.UserNicknameChangedEvent event5 = new com.windstudio.discordwl.API.UserNicknameChangedEvent(
                                                Member1,
                                                null,
                                                event.getChannel(),
                                                event.getMember().getNickname());
                                        Bukkit.getServer().getPluginManager().callEvent(event5);
                                    } catch (Exception ex) {
                                        plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't change user's nickname. Seems that user has higher role that bot!"));
                                    }
                                }
                                if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_ROLE_ADD")) {
                                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Add"), "disable")) {
                                        if (Member1 != null) {
                                            try {
                                                if (event.getGuild().getRoleById(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Add")) != null) {
                                                    List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Whitelist.Add");
                                                    for (String r : roleStringList) {
                                                        if (event.getGuild().getRoleById(r) != null) {
                                                            if (!hasRole(event.getMember(), event.getGuild(), event.getGuild().getRoleById(r).getId()))
                                                                continue;
                                                            event.getGuild().removeRoleFromMember(Member1, event.getGuild().getRoleById(r)).queue();
                                                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                                                com.windstudio.discordwl.API.UserRoleRemoveEvent event8 = new com.windstudio.discordwl.API.UserRoleRemoveEvent(
                                                                        event.getMember(),
                                                                        NICKNAME,
                                                                        event.getChannel(),
                                                                        event.getGuild().getRoleById(r));
                                                                Bukkit.getServer().getPluginManager().callEvent(event8);
                                                            });
                                                        }
                                                    }
                                                }
                                            } catch (Exception ex) {
                                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't remove role from user. Either user have higher role that bot, either roleID isn't correct!"));
                                            }
                                        }
                                    }
                                }
                                if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_ROLE_REMOVE")) {
                                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Remove"), "disable")) {
                                        if (Member1 != null) {
                                            try {
                                                if (event.getGuild().getRoleById(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Remove")) != null) {
                                                    List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Whitelist.Remove");
                                                    for (String r : roleStringList) {
                                                        if (event.getGuild().getRoleById(r) != null) {
                                                            if (hasRole(event.getMember(), event.getGuild(), event.getGuild().getRoleById(r).getId()))
                                                                continue;
                                                            event.getGuild().addRoleToMember(Member1, event.getGuild().getRoleById(r)).queue();
                                                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                                                com.windstudio.discordwl.API.UserRoleAddEvent event8 = new com.windstudio.discordwl.API.UserRoleAddEvent(
                                                                        event.getMember(),
                                                                        Member1.getNickname(),
                                                                        event.getChannel(),
                                                                        event.getGuild().getRoleById(r));
                                                                Bukkit.getServer().getPluginManager().callEvent(event8);
                                                            });
                                                        }
                                                    }
                                                }
                                            } catch (Exception ex) {
                                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't add role to user. Either user have higher role that bot, either roleID isn't correct!"));
                                            }
                                        }
                                    }
                                }
                            }
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.UserUnWhitelistedEvent eve = new com.windstudio.discordwl.API.UserUnWhitelistedEvent(event.getOption("user").getAsMember(), NICKNAME, event.getChannel());
                                        Bukkit.getServer().getPluginManager().callEvent(eve);
                                    });
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("SuccessTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistRemovedSuccessful").replaceAll("%p", NICKNAME));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            if (getStringList("Plugin.Settings.Enabled").contains("LOGGING")) {
                                if (event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")) != null) {
                                    String mention = event.getInteraction().getMember().getAsMention();
                                    String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                                    plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                    plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistLogEmbedTitle"));
                                    plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistSlashCommandRemovedLogEmbedDescription").replaceAll("%a", mention).replaceAll("%d", discord).replaceAll("%p", NICKNAME));
                                    event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.LogsSendEvent event1 = new com.windstudio.discordwl.API.LogsSendEvent(
                                                event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                                LogsCause.WHITELIST);
                                        Bukkit.getServer().getPluginManager().callEvent(event1);
                                    });
                                } else plugin.getConsole().sendMessage(ColorManager.translate("&c › &fField &cLogsChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                            }
                        } else {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("NotInWhitelist").replaceAll("%p", NICKNAME));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.NOT_WHITELSTED);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                        }
                        break;
                    case "list":
                        List<String> list = plugin.getLanguageManager().getStringList("WhitelistListChooseDescription");
                        String result = StringUtils.join(list, "\n");
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistListChooseTitle"));
                        plugin.getEmbedBuilder().setDescription(result.replaceAll("%0", plugin.getLanguageManager().get("WhitelistListChooseDefaultWhitelistButton")).replaceAll("%1", plugin.getLanguageManager().get("WhitelistListChooseOurWhitelistButton")));
                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("default", plugin.getLanguageManager().get("WhitelistListChooseDefaultWhitelistButton")), Button.primary("our", plugin.getLanguageManager().get("WhitelistListChooseOurWhitelistButton"))).setEphemeral(true)
                                .delay(Duration.ofSeconds(15))
                                .flatMap(InteractionHook::deleteOriginal)
                                .queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        break;
                }
                break;
            case "checkwhitelist":
                OptionMapping optionWhitelist = event.getOption("whitelisttype");
                if (optionWhitelist == null) {
                    event.reply("You need to provide valid whitelist type: default/our").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                OptionMapping optionNick = event.getOption("username");
                if (optionNick == null) {
                    event.reply("You need to provide valid username").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                String type = optionWhitelist.getAsString();
                String nickname = optionNick.getAsString();
                switch (type) {
                    case "default":
                        ArrayList<String> defaultWhitelistedPlayers = new ArrayList<String>();
                        for (OfflinePlayer dwhitelisted : Bukkit.getWhitelistedPlayers()) {
                            defaultWhitelistedPlayers.add(dwhitelisted.getName());
                        }
                        if (defaultWhitelistedPlayers.contains(nickname)) {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistCheckFoundTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistCheckFoundDescription").replaceAll("%p", nickname));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        } else {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistCheckNotFoundTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistCheckNotFoundDescription").replaceAll("%p", nickname));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.CHECK_NOT_FOUND);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                        }
                        if (getStringList("Plugin.Settings.Enabled").contains("LOGGING")) {
                            if (event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")) != null) {
                                String mention = event.getInteraction().getMember().getAsMention();
                                String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                                plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("CheckLogEmbedTitle"));
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckWhitelistLogEmbedDescription").replaceAll("%p", nickname).replaceAll("%a", mention).replaceAll("%d", discord));
                                EXECUTOR.schedule(() -> event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                        1, TimeUnit.SECONDS);
                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                    com.windstudio.discordwl.API.LogsSendEvent event1 = new com.windstudio.discordwl.API.LogsSendEvent(
                                            event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                            LogsCause.CHECK);
                                    Bukkit.getServer().getPluginManager().callEvent(event1);
                                });
                            } else plugin.getConsole().sendMessage(ColorManager.translate("&c › &fField &cLogsChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                        }
                        break;
                    case "our":
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                            public void run() {
                                checkDB(plugin.getEmbedBuilder(), event, nickname);
                                if (getStringList("Plugin.Settings.Enabled").contains("LOGGING")) {
                                    if (event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")) != null) {
                                        
                                        String mention = event.getInteraction().getMember().getAsMention();
                                        String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                                        plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                        plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("CheckLogEmbedTitle"));
                                        plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckWhitelistLogEmbedDescription").replaceAll("%p", nickname).replaceAll("%a", mention).replaceAll("%d", discord));
                                        EXECUTOR.schedule(() -> event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                                1, TimeUnit.SECONDS);
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.LogsSendEvent event1 = new com.windstudio.discordwl.API.LogsSendEvent(
                                                    event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                                    LogsCause.CHECK);
                                            Bukkit.getServer().getPluginManager().callEvent(event1);
                                        });
                                    } else plugin.getConsole().sendMessage(ColorManager.translate("&c › &fField &cLogsChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                                }
                            } });
                    }
                break;
            case "checklink":
                OptionMapping optionNickname = event.getOption("username");
                OptionMapping optionDID = event.getOption("did");
                new BukkitRunnable() {
                    public void run() {
                        switch (getString("Database.Type")) {
                            case "SQLite":
                                if (optionNickname == null && optionDID != null) {
                                    String did = optionDID.getAsString();
                                    if (!plugin.getClassManager().getUserdata().userProfileExistsString("discord_id", did)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    ErrorCause.CHECK_NOT_FOUND);
                                            Bukkit.getServer().getPluginManager().callEvent(event1);
                                        });
                                        return;
                                    }
                                    if (did.length() != 18) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckDiscordIDNotFound"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    ErrorCause.CHECK_NOT_FOUND);
                                            Bukkit.getServer().getPluginManager().callEvent(event1);
                                        });
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    try {
                                        preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE discord_id=?");
                                        preparedStatement.setString(1, did);
                                        ResultSet resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(did).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replace("%u", uuID).replace("%n", name).replace("%t", date).replace("%d", discord).replace("%i", d_id).replace("%m", mention));
                                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                    .delay(Duration.ofSeconds(60))
                                                    .flatMap(InteractionHook::deleteOriginal)
                                                    .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        }
                                        resultSet.close(); preparedStatement.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (optionNickname != null && optionDID == null) {
                                    String nick = optionNickname.getAsString();
                                    if (!plugin.getClassManager().getUserdata().userProfileExistsString("nickname", nick)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    ErrorCause.CHECK_NOT_FOUND);
                                            Bukkit.getServer().getPluginManager().callEvent(event1);
                                        });
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    try {
                                        preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE nickname=?");
                                        preparedStatement.setString(1, nick);
                                        ResultSet resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(d_id).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                    .delay(Duration.ofSeconds(60))
                                                    .flatMap(InteractionHook::deleteOriginal)
                                                    .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        }
                                        resultSet.close();
                                        preparedStatement.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (optionNickname != null && optionDID != null) {
                                    String nick = optionNickname.getAsString();
                                    String did = optionDID.getAsString();
                                    if (!plugin.getClassManager().getUserdata().userProfileExistsString("discord_id", did)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    ErrorCause.CHECK_NOT_FOUND);
                                            Bukkit.getServer().getPluginManager().callEvent(event1);
                                        });
                                        return;
                                    }
                                    if (did.length() != 18) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckDiscordIDNotFound"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    ErrorCause.CHECK_NOT_FOUND);
                                            Bukkit.getServer().getPluginManager().callEvent(event1);
                                        });
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    try {
                                        preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE discord_id=?");
                                        preparedStatement.setString(1, did);
                                        ResultSet resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(did).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                    .delay(Duration.ofSeconds(60))
                                                    .flatMap(InteractionHook::deleteOriginal)
                                                    .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        }
                                        resultSet.close();
                                        preparedStatement.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                            .delay(Duration.ofSeconds(15))
                                            .flatMap(InteractionHook::deleteOriginal)
                                            .queue(null, new ErrorHandler()
                                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                event.getMember(),
                                                event.getMember().getNickname(),
                                                event.getChannel(),
                                                ErrorCause.CHECK_NOT_FOUND);
                                        Bukkit.getServer().getPluginManager().callEvent(event1);
                                    });
                                }
                                break;
                            case "MySQL":
                                //event.deferReply(true).queue();
                                if (optionNickname == null && optionDID != null) {
                                    String did = optionDID.getAsString();
                                    if (!plugin.getClassManager().getUserdataMySQL().userProfileExistsString("discord_id", did)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    ErrorCause.CHECK_NOT_FOUND);
                                            Bukkit.getServer().getPluginManager().callEvent(event1);
                                        });
                                        return;
                                    }
                                    if (did.length() != 18) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckDiscordIDNotFound"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                event.getMember(),
                                                event.getMember().getNickname(),
                                                event.getChannel(),
                                                ErrorCause.CHECK_NOT_FOUND);
                                        Bukkit.getServer().getPluginManager().callEvent(event1);
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    ResultSet resultSet = null;
                                    try {
                                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("Database.Settings.MySQL.TableName.Linking") + " WHERE discord_id=?");
                                        preparedStatement.setString(1, did);
                                        resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(did).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        }
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        plugin.getPoolManager().close(null, preparedStatement, resultSet);
                                    }
                                } else if (optionNickname != null && optionDID == null) {
                                    String nick = optionNickname.getAsString();
                                    if (!plugin.getClassManager().getUserdataMySQL().userProfileExistsString("nickname", nick)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    ErrorCause.CHECK_NOT_FOUND);
                                            Bukkit.getServer().getPluginManager().callEvent(event1);
                                        });
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    ResultSet resultSet = null;
                                    try {
                                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("Database.Settings.MySQL.TableName.Linking") + " WHERE nickname=?");
                                        preparedStatement.setString(1, nick);
                                        resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(d_id).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(60, TimeUnit.SECONDS, Message::delete);
                                        }
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        plugin.getPoolManager().close(null, preparedStatement, resultSet);
                                    }
                                } else if (optionNickname != null && optionDID != null) {
                                    String did = optionDID.getAsString();
                                    if (!plugin.getClassManager().getUserdataMySQL().userProfileExistsString("discord_id", did)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                event.getMember(),
                                                event.getMember().getNickname(),
                                                event.getChannel(),
                                                ErrorCause.CHECK_NOT_FOUND);
                                        Bukkit.getServer().getPluginManager().callEvent(event1);
                                        return;
                                    }
                                    if (did.length() != 18) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckDiscordIDNotFound"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                event.getMember(),
                                                event.getMember().getNickname(),
                                                event.getChannel(),
                                                ErrorCause.CHECK_NOT_FOUND);
                                        Bukkit.getServer().getPluginManager().callEvent(event1);
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    ResultSet resultSet = null;
                                    try {
                                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("Database.Settings.MySQL.TableName.Linking") + " WHERE discord_id=?");
                                        preparedStatement.setString(1, did);
                                        resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(did).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(60, TimeUnit.SECONDS, Message::delete);
                                        }
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        plugin.getPoolManager().close(null, preparedStatement, resultSet);
                                    }
                                } else {
                                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                    event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                event.getMember(),
                                                event.getMember().getNickname(),
                                                event.getChannel(),
                                                ErrorCause.CHECK_NOT_FOUND);
                                        Bukkit.getServer().getPluginManager().callEvent(event1);
                                    });
                                }
                                break;
                        }
                        if (getStringList("Plugin.Settings.Enabled").contains("LOGGING")) {
                            
                            String mention = event.getInteraction().getMember().getAsMention();
                            String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                            plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                            plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("CheckLogEmbedTitle"));
                            if (optionNickname == null && optionDID != null) {
                                String did = optionDID.getAsString();
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckLinkDiscordIDLogEmbedDescription").replaceAll("%a", mention).replaceAll("%p", did).replaceAll("%i", discord));
                            } else if (optionNickname != null && optionDID == null) {
                                String nick = optionNickname.getAsString();
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckLinkNickLogEmbedDescription").replaceAll("%p", nick).replaceAll("%a", mention).replaceAll("%i", discord));
                            } else if (optionNickname != null && optionDID != null) {
                                String nick = optionNickname.getAsString();
                                String did = optionDID.getAsString();
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckLinkLogEmbedDescription").replaceAll("%p", nick).replaceAll("%a", mention).replaceAll("%d", did).replaceAll("%i", discord).replaceAll("%l", did));
                            }
                            EXECUTOR.schedule(() -> event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                    1, TimeUnit.SECONDS);
                            com.windstudio.discordwl.API.LogsSendEvent event1 = new com.windstudio.discordwl.API.LogsSendEvent(
                                    event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                    LogsCause.CHECK);
                            Bukkit.getServer().getPluginManager().callEvent(event1);
                        }
                    }
                }.runTaskAsynchronously(plugin);
                break;
            case "setupreaction":
                if (getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                    String title = plugin.getLanguageManager().get("ReactionEmbedTitle");
                    String color = plugin.getLanguageManager().get("ReactionEmbedColor");
                    List<String> listOne = plugin.getLanguageManager().getStringList("ReactionEmbedDescription");
                    String description = StringUtils.join(listOne, "\n");
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("SuccessTitle"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ConfirmMenuEmbedColor")));
                    event.getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    plugin.getEmbedBuilder().setTitle(title);
                    plugin.getEmbedBuilder().setDescription(description);
                    plugin.getEmbedBuilder().setColor(Color.decode(color.toString()));
                    event.getInteraction().getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("success", plugin.getLanguageManager().get("ReactionButton"))).queue();
                } else {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ReactionNotEnabled"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                event.getMember(),
                                event.getMember().getNickname(),
                                event.getChannel(),
                                ErrorCause.REACTIONS_NOT_ENABLED);
                        Bukkit.getServer().getPluginManager().callEvent(event1);
                    });
                }
                break;
            case "list":
                OptionMapping optionOption = event.getOption("list");
                if (optionOption == null) {
                    event.reply("You need to choose valid option to use it!").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                    String OPTION = optionOption.getAsString();
                    switch (OPTION) {
                        case "players":
                            ArrayList<String> onlineUsualPlayers = new ArrayList<String>();
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                onlineUsualPlayers.add(players.getName());
                            }
                            String online = onlineUsualPlayers.toString();
                            online = online.substring(1, online.length() - 1);
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ListPlayersTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ListPlayersDescription").replaceAll("%p", "" + online + ""));
                            plugin.getEmbedBuilder().setFooter(plugin.getLanguageManager().get("ListPlayersFooter").replaceAll("%p", String.valueOf(onlineUsualPlayers.size())));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            break;
                        case "banned":
                            ArrayList<String> BannedPlayers = new ArrayList<String>();
                            for (OfflinePlayer banned : Bukkit.getBannedPlayers()) {
                                BannedPlayers.add(banned.getName());
                            }
                            String banned = BannedPlayers.toString();
                            banned = banned.substring(1, banned.length() - 1);
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ListBannedPlayersTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ListBannedPlayersDescription").replaceAll("%p", "" + banned + ""));
                            plugin.getEmbedBuilder().setFooter(plugin.getLanguageManager().get("ListBannedPlayersFooter").replaceAll("%p", String.valueOf(BannedPlayers.size())));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            break;
                    }
                    break;
            case "account":
                OptionMapping type1Option = event.getOption("type");
                OptionMapping nick1Option = event.getOption("username");
                if (!getStringList("Plugin.Settings.Enabled").contains("LINKING")) {
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingTurnedOffEmbed"));
                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                event.getMember(),
                                event.getMember().getNickname(),
                                event.getChannel(),
                                ErrorCause.LINKING_TURNED_OFF);
                        Bukkit.getServer().getPluginManager().callEvent(event1);
                    });
                    return;
                }
                if (type1Option == null) {
                    event.reply("You need to choose one of this options: link/unlink").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                if (nick1Option == null) {
                    event.reply("You need to write valid nickname to add or remove it!").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                String TYPE1 = type1Option.getAsString();
                String NICK1 = nick1Option.getAsString();
                if (!getStringList("Plugin.Settings.Enabled").contains("BEDROCK_SUPPORT") && !NICK1.matches("^\\w{3,16}$")) {
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistRegexErrorDescription"));
                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                event.getMember(),
                                event.getMember().getNickname(),
                                event.getChannel(),
                                ErrorCause.REGEX_ERROR);
                        Bukkit.getServer().getPluginManager().callEvent(event1);
                    });
                    return;
                } else if (getStringList("Plugin.Settings.Enabled").contains("BEDROCK_SUPPORT") && !NICK1.matches("^(["+getString("Plugin.Settings.BedrockSymbol")+"])?([a-zA-Z0-9_ ]{3,16})$")) {
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistRegexErrorDescription"));
                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                event.getMember(),
                                event.getMember().getNickname(),
                                event.getChannel(),
                                ErrorCause.REGEX_ERROR);
                        Bukkit.getServer().getPluginManager().callEvent(event1);
                    });
                    return;
                }
                Player player = Bukkit.getPlayerExact(NICK1);
                OfflinePlayer p1 = Bukkit.getOfflinePlayer(NICK1);
                switch (TYPE1) {
                    case "link":
                        if (getStringList("Plugin.Settings.Enabled").contains("REQUIRE_ROLE") && !Objects.equals(plugin.getString("Configuration.Plugin.RoleID.Require.Link"), "disable")) {
                            List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Require.Link");
                            for (String r : roleStringList) {
                                if (event.getGuild().getRoleById(r) != null) {
                                    if (hasAtLeastRole(event.getMember(), event.getGuild(), roleStringList)) continue;
                                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("RequireRoleLink"));
                                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                event.getMember(),
                                                event.getMember().getNickname(),
                                                event.getChannel(),
                                                ErrorCause.HAVE_NO_ROLE);
                                        Bukkit.getServer().getPluginManager().callEvent(event1);
                                    });
                                    return;
                                }
                                    event.getChannel().sendTyping().queue();
                                    if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                                        EXECUTOR.schedule(() -> event.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                                        .delay(Duration.ofSeconds(15))
                                                        .flatMap(Message::delete)
                                                        .queue(null, new ErrorHandler()
                                                                .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                1, TimeUnit.SECONDS);
                                    } else {
                                        EXECUTOR.schedule(() -> event.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()),
                                                1, TimeUnit.SECONDS);
                                }
                            }
                            return;
                        }
                        event.deferReply();
                        if (!p1.isOnline()) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandPlayerNotOnlne"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.PLAYER_OFFLINE);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                            return;
                        }
                        String playerUUID = player.getUniqueId().toString();

                                    switch (getString("Database.Type")) {
                                        case "SQLite":
                                            if (plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAlreadyLinked"));
                                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                                    com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                            event.getMember(),
                                                            event.getMember().getNickname(),
                                                            event.getChannel(),
                                                            ErrorCause.ALREADY_LINKED);
                                                    Bukkit.getServer().getPluginManager().callEvent(event1);
                                                });
                                                return;
                                            }
                                            break;
                                        case "MySQL":
                                            if (plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAlreadyLinked"));
                                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                                    com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                            event.getMember(),
                                                            event.getMember().getNickname(),
                                                            event.getChannel(),
                                                            ErrorCause.ALREADY_LINKED);
                                                    Bukkit.getServer().getPluginManager().callEvent(event1);
                                                });
                                                return;
                                            }
                                            break;
                                    }

                        if ((event.getMember().getRoles().stream().filter(role -> role.getName().equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Link.Add"))).findAny().orElse(null) != null)) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAlreadyLinked"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.ALREADY_LINKED);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                            return;
                        }
                        if (NICK1.length() <= 3 || NICK1.length() > 16) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandNicknameError"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.INVALID_NICKNAME);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                            return;
                        }
                        if (LinkingCommand.getUuidIdMap().containsValue(event.getInteraction().getUser().getId())) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandCodeGenerated"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.LINKING);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                            return;
                        }
                        LinkingCommand.getUuidCodeMap().put(p1.getUniqueId(), plugin.getClassManager().getLinkingCommand().getCODE());
                        LinkingCommand.getUuidIdMap().put(p1.getUniqueId(), event.getInteraction().getUser().getId());
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandLink").replaceAll("%u", plugin.getClassManager().getLinkingCommand().getCODE()));
                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                        break;
                    case "unlink":
                        if (!p1.isOnline()) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandPlayerNotOnlne"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.PLAYER_OFFLINE);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                            return;
                        }
                        //event.getInteraction().getHook().sendMessage("...").setEphemeral(true).queue();
                        String playerOUUID = player.getUniqueId().toString();
                        if (event.getMember().getRoles().stream().filter(role -> role.getName().equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Link.Add"))).findAny().orElse(null) == null) {
                            new BukkitRunnable() {
                                public void run() {
                                    switch (getString("Database.Type")) {
                                        default:
                                        case "SQLite":
                                            if (!(plugin.getClassManager().getUserdata().userProfileExists(playerOUUID))) {
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandNotLinked"));
                                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                                    com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                            event.getMember(),
                                                            event.getMember().getNickname(),
                                                            event.getChannel(),
                                                            ErrorCause.LINKING);
                                                    Bukkit.getServer().getPluginManager().callEvent(event1);
                                                });
                                            }
                                            break;
                                        case "MySQL":
                                            if (!(plugin.getClassManager().getUserdataMySQL().userProfileExists(playerOUUID))) {
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandNotLinked"));
                                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                                    com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                                            event.getMember(),
                                                            event.getMember().getNickname(),
                                                            event.getChannel(),
                                                            ErrorCause.LINKING);
                                                    Bukkit.getServer().getPluginManager().callEvent(event1);
                                                });
                                            }
                                            break;
                                    }
                                }
                            }.runTaskAsynchronously(plugin);
                        }
                        if (NICK1.length() <= 3 || NICK1.length() > 16) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandNicknameError"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.INVALID_NICKNAME);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                            return;
                        }
                        if (LinkingCommand.getUuidCodeMap().containsValue(event.getInteraction().getUser().getId())) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandCodeGenerated"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.LINKING);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                            return;
                        }
                        switch (getString("Database.Type")) {
                            case "SQLite":
                                DoSQLite(player, event, plugin.getEmbedBuilder());
                                break;
                            case "MySQL":
                                DoMySQL(player, event, plugin.getEmbedBuilder());
                                break;
                            default:
                                DoSQLite(player, event, plugin.getEmbedBuilder());
                                break;
                        }
                        break;
                }
                break;
        }
    }
    public void DoSQLite(Player player, SlashCommandInteractionEvent event, EmbedBuilder eb) {
        new BukkitRunnable() {
            public void run() {
                if (plugin.getClassManager().getUserdata().getSingleInformationFromUserProfile("uuid", player.getUniqueId().toString(), "discord_id").equals(event.getInteraction().getUser().getId())) {
                    plugin.getClassManager().getUserdata().deleteInformationFromUserProfile("uuid", player.getUniqueId().toString());
                    eb.setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingSlashCommandUnLink"));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                    String Discord = event.getUser().getName() + "#" + event.getUser().getDiscriminator();
                    player.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("LinkingUnlinkedMessage").replaceAll("%u", Discord)));
                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Link.Add"), "disable")) {
                            try {
                                List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Link.Add");
                                for (String r : roleStringList) {
                                    if (event.getGuild().getRoleById(r) != null) {
                                        event.getGuild().removeRoleFromMember(event.getInteraction().getMember(), event.getGuild().getRoleById(r)).queue();
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.UserRoleRemoveEvent event8 = new com.windstudio.discordwl.API.UserRoleRemoveEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    event.getGuild().getRoleById(r));
                                            Bukkit.getServer().getPluginManager().callEvent(event8);
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't add role to user. Seems that user has higher role that bot!"));
                                player.sendMessage(ColorManager.translate("&cBot can't add you role!"));
                            }
                        }
                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Link.Remove"), "disable")) {
                            try {
                                List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Link.Remove");
                                for (String r : roleStringList) {
                                    if (event.getGuild().getRoleById(r) != null) {
                                        event.getGuild().addRoleToMember(event.getInteraction().getMember(), event.getGuild().getRoleById(r)).queue();
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.UserRoleAddEvent event8 = new com.windstudio.discordwl.API.UserRoleAddEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    event.getGuild().getRoleById(r));
                                            Bukkit.getServer().getPluginManager().callEvent(event8);
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't remove role from user. Seems that user has higher role that bot!"));
                                player.sendMessage(ColorManager.translate("&cBot can't remove role from you!"));
                            }
                        }
                    if (getStringList("Plugin.Settings.Enabled").contains("LINKING_NAME_CHANGE")) event.getMember().modifyNickname(null).queue();
                    if (getStringList("Plugin.Settings.Enabled").contains("LOGGING")) {
                        if (event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")) != null) {
                            String mention = event.getInteraction().getMember().getAsMention();
                            plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                            plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingLogTitle"));
                            plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingLogUnLinkedDescription").replaceAll("%u", mention).replaceAll("%d", Discord).replaceAll("%p", player.getName()).replaceAll("%i", player.getUniqueId().toString()));
                            event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.LogsSendEvent event1 = new com.windstudio.discordwl.API.LogsSendEvent(
                                        event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                        LogsCause.LINK);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                        } else plugin.getConsole().sendMessage(ColorManager.translate("&c › &fField &cLogsChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                    }
                } else {
                    eb.setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAccountNotYours"));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                event.getMember(),
                                event.getMember().getNickname(),
                                event.getChannel(),
                                ErrorCause.LINKING);
                        Bukkit.getServer().getPluginManager().callEvent(event1);
                    });
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public void DoMySQL(Player player, SlashCommandInteractionEvent event, EmbedBuilder eb) {
        new BukkitRunnable() {
            public void run() {
                if (plugin.getClassManager().getUserdataMySQL().getSingleInformationFromUserProfile("uuid", player.getUniqueId().toString(), "discord_id").equals(event.getInteraction().getUser().getId())) {
                    plugin.getClassManager().getUserdataMySQL().deleteInformationFromUserProfile("uuid", player.getUniqueId().toString());
                    eb.setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingSlashCommandUnLink"));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                    String Discord = event.getUser().getName() + "#" + event.getUser().getDiscriminator();
                    player.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("LinkingUnlinkedMessage").replaceAll("%u", Discord)));
                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Link.Add"), "disable")) {
                            try {
                                List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Link.Add");
                                for (String r : roleStringList) {
                                    if (event.getGuild().getRoleById(r) != null) {
                                        event.getGuild().removeRoleFromMember(event.getInteraction().getMember(), event.getGuild().getRoleById(r)).queue();
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.UserRoleRemoveEvent event8 = new com.windstudio.discordwl.API.UserRoleRemoveEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    event.getGuild().getRoleById(r));
                                            Bukkit.getServer().getPluginManager().callEvent(event8);
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't add role to user. Seems that user has higher role that bot!"));
                                player.sendMessage(ColorManager.translate("&cBot can't add you role!"));
                            }
                        }
                    if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Link.Remove"), "disable")) {
                        if (Main.getJDA().getGuildById(plugin.getConfig().getString("Service.ServerID")).getRoleById(plugin.getConfig().getString("Configuration.Plugin.RoleID.Link.Remove")) != null) {
                            try {
                                List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Link.Remove");
                                for (String r : roleStringList) {
                                    if (event.getGuild().getRoleById(r) != null) {
                                        event.getGuild().addRoleToMember(event.getInteraction().getMember(), event.getGuild().getRoleById(r)).queue();
                                        Bukkit.getScheduler().runTask(plugin, ()-> {
                                            com.windstudio.discordwl.API.UserRoleAddEvent event8 = new com.windstudio.discordwl.API.UserRoleAddEvent(
                                                    event.getMember(),
                                                    event.getMember().getNickname(),
                                                    event.getChannel(),
                                                    event.getGuild().getRoleById(r));
                                            Bukkit.getServer().getPluginManager().callEvent(event8);
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                plugin.getConsole().sendMessage(ColorManager.translate("&c › &fBot can't remove role from user. Seems that user has higher role that bot!"));
                                player.sendMessage(ColorManager.translate("&cBot can't remove role from you!"));
                            }
                        }
                    }
                    if (getStringList("Plugin.Settings.Enabled").contains("LINKING_NAME_CHANGE")) event.getMember().modifyNickname(null).queue();
                    if (getStringList("Plugin.Settings.Enabled").contains("LOGGING")) {
                        if (event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")) != null) {
                        String mention = event.getInteraction().getMember().getAsMention();
                        plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                        plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingLogTitle"));
                        plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingLogUnLinkedDescription").replaceAll("%u", mention).replaceAll("%d", Discord).replaceAll("%p", player.getName()).replaceAll("%i", player.getUniqueId().toString()));
                        event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.LogsSendEvent event1 = new com.windstudio.discordwl.API.LogsSendEvent(
                                        event.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                        LogsCause.LINK);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                        } else plugin.getConsole().sendMessage(ColorManager.translate("&c › &fField &cLogsChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                    }
                } else {
                    eb.setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAccountNotYours"));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                event.getMember(),
                                event.getMember().getNickname(),
                                event.getChannel(),
                                ErrorCause.LINKING);
                        Bukkit.getServer().getPluginManager().callEvent(event1);
                    });
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public void checkDB(EmbedBuilder eb, SlashCommandInteractionEvent event, String nickname) {
        new BukkitRunnable() {
            public void run() {
                switch (getString("Database.Type")) {
                    case "SQLite":
                        if (plugin.getClassManager().getSqLiteWhitelistData().userPlayerExists(nickname)) {
                            eb.setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            eb.setTitle(plugin.getLanguageManager().get("WhitelistCheckFoundTitle"));
                            eb.setDescription(plugin.getLanguageManager().get("WhitelistCheckFoundOurWhitelistDescription").replaceAll("%t", plugin.getClassManager().getSqLiteWhitelistData().getPlayerType("nickname", nickname, "player_type")).replaceAll("%p", nickname));
                            event.replyEmbeds(eb.build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        } else {
                            eb.setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            eb.setTitle(plugin.getLanguageManager().get("WhitelistCheckNotFoundTitle"));
                            eb.setDescription(plugin.getLanguageManager().get("WhitelistCheckNotFoundDescription").replaceAll("%p", nickname));
                            event.replyEmbeds(eb.build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.CHECK_NOT_FOUND);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                        }
                        break;
                    case "MySQL":
                        if (plugin.getClassManager().getMySQLWhitelistData().userPlayerExists(nickname)) {
                            eb.setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            eb.setTitle(plugin.getLanguageManager().get("WhitelistCheckFoundTitle"));
                            eb.setDescription(plugin.getLanguageManager().get("WhitelistCheckFoundOurWhitelistDescription").replaceAll("%t", plugin.getClassManager().getMySQLWhitelistData().getPlayerType("nickname", nickname, "player_type")).replaceAll("%p", nickname));
                            event.replyEmbeds(eb.build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        } else {
                            eb.setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            eb.setTitle(plugin.getLanguageManager().get("WhitelistCheckNotFoundTitle"));
                            eb.setDescription(plugin.getLanguageManager().get("WhitelistCheckNotFoundDescription").replaceAll("%p", nickname));
                            event.replyEmbeds(eb.build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                        event.getMember(),
                                        event.getMember().getNickname(),
                                        event.getChannel(),
                                        ErrorCause.CHECK_NOT_FOUND);
                                Bukkit.getServer().getPluginManager().callEvent(event1);
                            });
                        }
                        break;
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    void err(SlashCommandInteractionEvent event) {
        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistRegexErrorDescription"));
        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                .delay(Duration.ofSeconds(15))
                .flatMap(InteractionHook::deleteOriginal)
                .queue(null, new ErrorHandler()
                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
        Bukkit.getScheduler().runTask(plugin, ()-> {
            com.windstudio.discordwl.API.ErrorReceivedEvent event1 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                    event.getMember(),
                    event.getMember().getNickname(),
                    event.getChannel(),
                    ErrorCause.REGEX_ERROR);
            Bukkit.getServer().getPluginManager().callEvent(event1);
        });
        return;
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
    public boolean hasRole(Member member, Guild guild, String roleID) {
        List<Role> memberRoles = member.getRoles();
        return memberRoles.contains(guild.getRoleById(roleID));
    }
    public boolean hasAtLeastRole(Member member, Guild guild, List<String> roleID) {
        List<Role> memberRoles = member.getRoles();
        for (String s : roleID) {
            if (guild.getRoleById(s) != null) {
                if (memberRoles.contains(guild.getRoleById(s))) return true;
            }
        }
        return false;
    }
}