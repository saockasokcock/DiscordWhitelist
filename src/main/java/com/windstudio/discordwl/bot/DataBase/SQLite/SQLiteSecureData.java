package com.windstudio.discordwl.bot.DataBase.SQLite;

import com.windstudio.discordwl.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLiteSecureData {
    public Main plugin;
    public SQLiteSecureData(Main plugin) {
        this.plugin = plugin;
    }
    public void addSecure(String code, String salt, Integer tick) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("INSERT INTO secure(code, salt, tick) VALUES (?, ?, ?)");
            preparedStatement.setString(1, code);
            preparedStatement.setString(2, salt);
            preparedStatement.setInt(3, tick);
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
    }

    public boolean isSecureExists() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM secure");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            resultSet.close(); preparedStatement.close();
            return false;
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
        return false;
    }
    public void setTick(Integer newTick) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("UPDATE secure SET tick=?");
            preparedStatement.setInt(1, newTick);
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
    }

    public String getCode() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM secure");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("code");
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
        return null;
    }

    public String getSalt() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM secure");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("salt");
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
        return null;
    }

    public Integer getTick() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM secure");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("tick");
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
        return null;
    }

    public boolean isTicked() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM secure");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("tick") == 1;
            }
            resultSet.close(); preparedStatement.close();
            return false;
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
        return false;
    }
}
