package com.windstudio.discordwl.bot.Linking;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserdataSQLite {
    public Main plugin;
    public UserdataSQLite(Main plugin) {
        this.plugin = plugin;
    }
    public void createUserProfile(String playerUUID, String playerName, String Discord, String DiscordID) {
        if (!userProfileExists(playerUUID)) {
            PreparedStatement preparedStatement = null;
            Date now = new Date();
            java.sql.Date converted = new java.sql.Date(now.getTime());
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            try {
                preparedStatement = SQLite.con.prepareStatement("INSERT INTO "+getString("Database.Settings.SQLite.TableName.Linking") + "(uuid, nickname, discord, discord_id, linking_date) VALUES (?, ?, ?, ?, ?)");
                preparedStatement.setString(1, playerUUID);
                preparedStatement.setString(2, playerName);
                preparedStatement.setString(3, Discord);
                preparedStatement.setString(4, DiscordID);
                preparedStatement.setString(5, format.format(converted));
                preparedStatement.executeUpdate(); preparedStatement.close();
            } catch (SQLException ex) {
                plugin.getConsole().sendMessage(ex.toString());
            }
        }
    }
    public String getInformationFromUserProfile(String whatYouNeedExactly, String placeholderSet) {
        PreparedStatement preparedStatement = null;
        try {
                preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE " + whatYouNeedExactly + "=?");
                preparedStatement.setString(1, placeholderSet);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                    return gotYouNeedExactly;
                }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException ex){
            plugin.getConsole().sendMessage(ex.toString());
    }
        return null;
    }
    public String getInfoFromUserProfile(String condition, String placeholderSet, String whatYouNeedExactly) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE " + condition + "=?");
            preparedStatement.setString(1, placeholderSet);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                return gotYouNeedExactly;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException ex){
            plugin.getConsole().sendMessage(ex.toString());
        }
        return null;
    }
    public String getSingleInformationFromUserProfile(String condition, String conditionNew, String whatYouNeedExactly) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE " + condition + "=?");
            preparedStatement.setString(1, conditionNew);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                return gotYouNeedExactly;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException ex) {
            plugin.getConsole().sendMessage(ex.toString());
        }
        return null;
    }
    public void deleteInformationFromUserProfile(String whatYouNeedExactly, String placeholderSet) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("DELETE FROM "+getString("Database.Settings.SQLite.TableName.Linking") + " WHERE " + whatYouNeedExactly+"=?");
            preparedStatement.setString(1, placeholderSet);
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException ex) {
            plugin.getConsole().sendMessage(ex.toString());
        }
    }
    public void setInformationToUserProfile(Player player, Integer parameterIndex, String x) {
        PreparedStatement preparedStatement = null;
        /*Date now = new Date();
        java.sql.Date converted = new java.sql.Date(now.getTime());
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); */
        try {
            preparedStatement = SQLite.con.prepareStatement("INSERT INTO "+getString("Database.Settings.SQLite.TableName.Linking") + "(uuid, nickname, discord, discord_id, linking_date) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setString(parameterIndex, x);
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException ex) {
            plugin.getConsole().sendMessage(ex.toString());
        }
    }
    public boolean userProfileExists(String playerUUID) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
        return false;
    }
    public boolean userProfileExistsString(String condition, String condition2) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE "+condition+"=?");
            preparedStatement.setString(1, condition2);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
        return false;
    }
    public boolean discordUserProfileExists(String DiscordID) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("Database.Settings.SQLite.TableName.Linking") + " WHERE discord_id=?");
            preparedStatement.setString(1, DiscordID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
        return false;
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
