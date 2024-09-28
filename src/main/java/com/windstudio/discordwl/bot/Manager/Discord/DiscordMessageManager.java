package com.windstudio.discordwl.bot.Manager.Discord;

import com.windstudio.discordwl.API.Cause.ErrorCause;
import com.windstudio.discordwl.API.Cause.LogsCause;
import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordMessageManager extends ListenerAdapter {
    private final Main plugin;
    private final JDA jda;
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public DiscordMessageManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.jda = Main.getJDA();
    }
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        String argument = e.getMessage().getContentDisplay();
        Bukkit.getScheduler().runTask(plugin, ()-> {
            com.windstudio.discordwl.API.ChannelWhitelistAttemptEvent event0 = new com.windstudio.discordwl.API.ChannelWhitelistAttemptEvent(
                    e.getMember(),
                    argument,
                    e.getChannel());
            Bukkit.getServer().getPluginManager().callEvent(event0);
        });
        if (!(e.getMessage().getAuthor().isBot() || e.isWebhookMessage()) && (Objects.equals(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Whitelist"), e.getChannel().getId()) && e.isFromType(ChannelType.TEXT))) {
            if ((argument.length() < 3) || (argument.length() > 16)) {
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("LengthError")));
                e.getChannel().sendTyping().queue();
                if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                    e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                            .delay(Duration.ofSeconds(15))
                            .flatMap(Message::delete)
                            .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                            1, TimeUnit.SECONDS);
                } else {
                    EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                            1, TimeUnit.SECONDS);
                }
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    com.windstudio.discordwl.API.ErrorReceivedEvent event = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                            e.getMember(),
                            argument,
                            e.getChannel(),
                            ErrorCause.INVALID_NICKNAME);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                });
                return;
            }
            if (getStringList("Plugin.Settings.Enabled").contains("REQUIRE_ROLE") && !Objects.equals(plugin.getString("Configuration.Plugin.RoleID.Require.Whitelist"), "disable")) {
                List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Require.Whitelist");
                for (String r : roleStringList) {
                    if (e.getGuild().getRoleById(r) != null) {
                        if (hasAtLeastRole(e.getMember(), e.getGuild(), roleStringList)) continue;
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("RequireRoleWhitelist"));
                        e.getChannel().sendTyping().queue();
                        if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                            EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                            .delay(Duration.ofSeconds(15))
                                            .flatMap(Message::delete)
                                            .queue(null, new ErrorHandler()
                                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                    1, TimeUnit.SECONDS);
                        } else {
                            EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()),
                                    1, TimeUnit.SECONDS);
                        }
                    }
                }
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    com.windstudio.discordwl.API.ErrorReceivedEvent event = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                            e.getMember(),
                            argument,
                            e.getChannel(),
                            ErrorCause.HAVE_NO_ROLE);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                });
                return;
            }
            if (getStringList("Plugin.Settings.Enabled").contains("BLACKLIST")) {
                List<String> IDs = plugin.getConfig().getStringList("Configuration.Plugin.Blacklist.ID");
                if (plugin.getConfig().getStringList("Configuration.Plugin.Blacklist.Nickname").contains(argument) || IDs.contains(e.getMessage().getAuthor().getId())) {
                    e.getChannel().sendTyping().queue();
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("Blacklist-message").replaceAll("%u", argument));
                    if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                        EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                        .delay(Duration.ofSeconds(15))
                                        .flatMap(Message::delete)
                                        .queue(null, new ErrorHandler()
                                                .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                1, TimeUnit.SECONDS);
                    } else {
                        EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()),
                                1, TimeUnit.SECONDS);
                        }
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.ErrorReceivedEvent event = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                                e.getMember(),
                                argument,
                                e.getChannel(),
                                ErrorCause.BLACKLISTED);
                        Bukkit.getServer().getPluginManager().callEvent(event);
                    });
                    return;
                    }
                }
            if (!getStringList("Plugin.Settings.Enabled").contains("BEDROCK_SUPPORT") && argument.matches("^\\w{3,16}$")) {
                LogicA(e);
            } else if (getStringList("Plugin.Settings.Enabled").contains("BEDROCK_SUPPORT") && argument.matches("^(["+getString("Plugin.Settings.BedrockSymbol")+"])?([a-zA-Z0-9_ ]{3,16})$")) {
                LogicA(e);
            } else {
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("SymbolsError")));
                e.getChannel().sendTyping().queue();
                if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                    e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                            .delay(Duration.ofSeconds(15))
                            .flatMap(Message::delete)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                            1, TimeUnit.SECONDS);
                } else {
                    EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                            1, TimeUnit.SECONDS);
                }
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    com.windstudio.discordwl.API.ErrorReceivedEvent event = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                            e.getMember(),
                            argument,
                            e.getChannel(),
                            ErrorCause.INVALID_NICKNAME);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                });
            }
        }
    }
    private void LogicA(@NotNull MessageReceivedEvent e) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String argument = e.getMessage().getContentDisplay();
        String argument2 = e.getMessage().getId();
        OfflinePlayer p = Bukkit.getOfflinePlayer(argument);
        Guild guild = e.getGuild();
        Bukkit.getScheduler().runTask(plugin, ()-> {
            com.windstudio.discordwl.API.UserNicknameReceivedEvent event = new com.windstudio.discordwl.API.UserNicknameReceivedEvent(
                    e.getMember(),
                    argument,
                    e.getChannel());
            Bukkit.getServer().getPluginManager().callEvent(event);
        });
        if (p.isWhitelisted()) {
            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("AlreadyIn").replaceAll("%u", argument));
            e.getChannel().sendTyping().queue();
            if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                EXECUTOR.schedule(() -> e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                        1, TimeUnit.SECONDS);
                EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                .delay(Duration.ofSeconds(15))
                                .flatMap(Message::delete)
                                .queue(null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                        1, TimeUnit.SECONDS);
            } else {
                EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                        1, TimeUnit.SECONDS);
            }
            Bukkit.getScheduler().runTask(plugin, ()-> {
                com.windstudio.discordwl.API.ErrorReceivedEvent event2 = new com.windstudio.discordwl.API.ErrorReceivedEvent(
                        e.getMember(),
                        argument,
                        e.getChannel(),
                        ErrorCause.ALREADY_WHITELISTED);
                Bukkit.getServer().getPluginManager().callEvent(event2);
            });
        }
        if (!p.isWhitelisted()) {
            if (getStringList("Plugin.Settings.Enabled").contains("WHITELIST_CONFIRMATION_MENU")) {
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ConfirmTitle"));
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ConfirmMenuEmbedColor")));
                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ConfirmMessage").replaceAll("%u", argument));
                plugin.getEmbedBuilder().setFooter(argument2);
                plugin.getEmbedBuilder().setTimestamp(Instant.now());
                e.getChannel().sendTyping().queue();
                EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("agree", plugin.getLanguageManager().get("ConfirmButtonYes")), Button.danger("notagree", plugin.getLanguageManager().get("ConfirmButtonNo")))
                                .delay(Duration.ofSeconds(30))
                                .flatMap(Message::delete)
                                .queue(null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                        1, TimeUnit.SECONDS);
                e.getMessage().delete().queueAfter(30, TimeUnit.SECONDS, null, new ErrorHandler()
                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                plugin.getEmbedBuilder().setTimestamp(null);
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    com.windstudio.discordwl.API.NicknameConfirmationMenuSendEvent event3 = new com.windstudio.discordwl.API.NicknameConfirmationMenuSendEvent(
                            e.getMember(),
                            argument,
                            e.getChannel());
                    Bukkit.getServer().getPluginManager().callEvent(event3);
                });
            } else {
                if (!getStringList("Plugin.Settings.Enabled").contains("WHITELIST_CONFIRMATION_MENU")) {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("AddedIn").replaceAll("%u", argument));
                    e.getChannel().sendTyping().queue();
                    backupWhitelistFile();
                    if (plugin.getConfig().getBoolean("Plugin.EWhitelist.Sync") && getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                        Bukkit.getScheduler().runTask(plugin, () -> p.setWhitelisted(true));
                        addToEWL(argument);
                    } else if (!plugin.getConfig().getBoolean("Plugin.EWhitelist.Sync") && getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                        addToEWL(argument);
                    } else if (plugin.getConfig().getBoolean("Plugin.EWhitelist.Sync") && !getStringList("Plugin.Settings.Enabled").contains("EWHITELIST")) {
                        Bukkit.getScheduler().runTask(plugin, () -> p.setWhitelisted(true));
                    }
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        com.windstudio.discordwl.API.UserWhitelistedEvent event4 = new com.windstudio.discordwl.API.UserWhitelistedEvent(
                                e.getMember(),
                                argument,
                                e.getChannel());
                        Bukkit.getServer().getPluginManager().callEvent(event4);
                    });
                    if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                        EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                        .delay(Duration.ofSeconds(15))
                                        .flatMap(Message::delete)
                                        .queue(null, new ErrorHandler()
                                                .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                1, TimeUnit.SECONDS);
                    } else {
                        EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                                1, TimeUnit.SECONDS);
                    }
                    for (String s : getStringList("Plugin.Settings.Enabled")) {
                        switch (s) {
                            case "EPHEMERAL_MESSAGES":
                                e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                break;
                            case "WHITELIST_CHANGE_NAME":
                                try {
                                    EXECUTOR.schedule(() -> e.getMember().modifyNickname(argument).queue(),
                                            1, TimeUnit.SECONDS);
                                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.UserNicknameChangedEvent event5 = new com.windstudio.discordwl.API.UserNicknameChangedEvent(
                                                e.getMember(),
                                                argument,
                                                e.getChannel(),
                                                e.getMember().getNickname());
                                        Bukkit.getServer().getPluginManager().callEvent(event5);
                                    });
                                } catch (Exception ex) {
                                    console.sendMessage(ColorManager.translate("&a › &fBot can't change user's nickname. Seems that user has higher role that bot!"));
                                }
                                break;
                            case "WHITELIST_DM":
                                List<String> list = plugin.getLanguageManager().getStringList("DM-Message");
                                String result = StringUtils.join(list, "\n");
                                EXECUTOR.schedule(() -> e.getMessage().getAuthor().openPrivateChannel().queue(messages -> {
                                            plugin.getDMEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("DMEmbedColor")));
                                            plugin.getDMEmbedBuilder().setTitle(plugin.getLanguageManager().get("DMMessageTitle"));
                                            plugin.getDMEmbedBuilder().setDescription(result.replaceAll("%u", argument));
                                    messages.sendMessageEmbeds(plugin.getDMEmbedBuilder().build()).queue(null, new ErrorHandler()
                                                    .ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER));
                                        }),
                                        1, TimeUnit.SECONDS);
                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                    com.windstudio.discordwl.API.UserDirectMessageSendEvent event6 = new com.windstudio.discordwl.API.UserDirectMessageSendEvent(
                                            e.getMessage().getAuthor(),
                                            argument,
                                            e.getChannel());
                                    Bukkit.getServer().getPluginManager().callEvent(event6);
                                });
                                break;
                            case "WHITELIST_WELCOME_MESSAGE":
                                List<String> listW = plugin.getLanguageManager().getStringList("Welcome-Message");
                                String resultW = StringUtils.join(listW, "\n");
                                switch (getString("Plugin.Settings.Message.Welcome.Type")) {
                                    case "EMBED":
                                        if (e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")) != null) {
                                            if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                                                String mentions = e.getMessage().getAuthor().getAsMention();
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WelcomeMessageTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("WelcomeMessageEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(resultW.replaceAll("%p", argument).replaceAll("%u", mentions));
                                                EXECUTOR.schedule(() -> e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")).sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                                                .delay(Duration.ofSeconds(60))
                                                                .flatMap(Message::delete)
                                                                .queue(null, new ErrorHandler()
                                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                        1, TimeUnit.SECONDS);
                                            } else {
                                                String mentions = e.getMessage().getAuthor().getAsMention();
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WelcomeMessageTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("WelcomeMessageEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(resultW.replaceAll("%p", argument).replaceAll("%u", mentions));
                                                EXECUTOR.schedule(() -> e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")).sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                                                        1, TimeUnit.SECONDS);
                                            }
                                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                                com.windstudio.discordwl.API.UserWhitelistWelcomeMessageSendEvent event7 = new com.windstudio.discordwl.API.UserWhitelistWelcomeMessageSendEvent(
                                                        e.getMember(),
                                                        argument,
                                                        e.getChannel(),
                                                        e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")));
                                                Bukkit.getServer().getPluginManager().callEvent(event7);
                                            });
                                        } else
                                            console.sendMessage(ColorManager.translate("&a › &fField &cGlobalChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                                        break;
                                    case "TEXT":
                                        if (e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")) != null) {
                                            if (getStringList("Plugin.Settings.Enabled").contains("EPHEMERAL_MESSAGES")) {
                                                String mentions = e.getMessage().getAuthor().getAsMention();
                                                EXECUTOR.schedule(() -> e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")).sendMessage(resultW.replaceAll("%p", argument).replaceAll("%u", mentions))
                                                                .delay(Duration.ofSeconds(60))
                                                                .flatMap(Message::delete)
                                                                .queue(null, new ErrorHandler()
                                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                        1, TimeUnit.SECONDS);
                                            } else {
                                                String mentions = e.getMessage().getAuthor().getAsMention();
                                                EXECUTOR.schedule(() -> e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")).sendMessage(resultW.replaceAll("%p", argument).replaceAll("%u", mentions)).queue(),
                                                        1, TimeUnit.SECONDS);
                                            }
                                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                                com.windstudio.discordwl.API.UserWhitelistWelcomeMessageSendEvent event7 = new com.windstudio.discordwl.API.UserWhitelistWelcomeMessageSendEvent(
                                                        e.getMember(),
                                                        argument,
                                                        e.getChannel(),
                                                        e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Global")));
                                                Bukkit.getServer().getPluginManager().callEvent(event7);
                                            });
                                        } else
                                            console.sendMessage(ColorManager.translate("&a › &fField &cGlobalChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                                        break;
                                }
                            case "WHITELIST_ROLE_ADD":
                                if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Add"), "disable")) {
                                    try {
                                        List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Whitelist.Add");
                                        for (String r : roleStringList) {
                                            if (hasRole(e.getMember(), guild, r)) continue;
                                            if (e.getGuild().getRoleById(r) != null) {
                                                e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(r)).queue();
                                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                                    com.windstudio.discordwl.API.UserRoleAddEvent event8 = new com.windstudio.discordwl.API.UserRoleAddEvent(
                                                            e.getMember(),
                                                            argument,
                                                            e.getChannel(),
                                                            e.getGuild().getRoleById(r));
                                                    Bukkit.getServer().getPluginManager().callEvent(event8);
                                                });
                                            }
                                        }
                                    } catch (Exception ex) {
                                        console.sendMessage(ColorManager.translate("&a › &fBot can't add role to user. Either user have higher role that bot, either roleID isn't correct!"));
                                    }
                                }
                                break;
                            case "WHITELIST_ROLE_REMOVE":
                                if (!Objects.equals(plugin.getConfig().getString("Configuration.Plugin.RoleID.Whitelist.Remove"), "disable")) {
                                    try {
                                        List<String> roleStringList = plugin.getStringList("Configuration.Plugin.RoleID.Whitelist.Remove");
                                        for (String r : roleStringList) {
                                            if (!hasRole(e.getMember(), guild, r)) continue;
                                            if (e.getGuild().getRoleById(r) != null) {
                                                e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(r)).queue();
                                                Bukkit.getScheduler().runTask(plugin, ()-> {
                                                    com.windstudio.discordwl.API.UserRoleRemoveEvent event9 = new com.windstudio.discordwl.API.UserRoleRemoveEvent(
                                                            e.getMember(),
                                                            argument,
                                                            e.getChannel(),
                                                            e.getGuild().getRoleById(r));
                                                    Bukkit.getServer().getPluginManager().callEvent(event9);
                                                });
                                            }
                                        }
                                    } catch (Exception ex) {
                                        console.sendMessage(ColorManager.translate("&a › &fBot can't remove role from user. Either user have higher role that bot, either roleID isn't correct!"));
                                    }
                                }
                                break;
                            case "LOGGING":
                                if (e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")) != null) {
                                    String mention = e.getMessage().getAuthor().getAsMention();
                                    String discord = e.getMessage().getAuthor().getName() + "#" + e.getMessage().getAuthor().getDiscriminator();
                                    plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistLogEmbedTitle"));
                                    plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                    plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistLogEmbedDescription").replaceAll("%p", argument).replaceAll("%u", mention).replaceAll("%d", discord));
                                    EXECUTOR.schedule(() -> e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                                    .ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                            1, TimeUnit.SECONDS);
                                    Bukkit.getScheduler().runTask(plugin, ()-> {
                                        com.windstudio.discordwl.API.LogsSendEvent event10 = new com.windstudio.discordwl.API.LogsSendEvent(
                                                e.getGuild().getTextChannelById(plugin.getConfig().getString("Configuration.Plugin.ChannelID.Logs")),
                                                LogsCause.WHITELIST);
                                        Bukkit.getServer().getPluginManager().callEvent(event10);
                                    });
                                } else console.sendMessage(ColorManager.translate("&a › &fField &cLogsChannelID &ffilled not correct! Plugin can't find this channel! Check it."));
                                break;
                        }
                    }
                }
            }
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
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}