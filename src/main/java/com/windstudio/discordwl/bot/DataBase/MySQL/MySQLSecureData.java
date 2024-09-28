package com.windstudio.discordwl.bot.DataBase.MySQL;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLSecureData {
    public Main plugin;
    public MySQLSecureData(Main plugin) {
        this.plugin = plugin;
    }
    public void addSecure(String code, String salt, Integer tick) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("INSERT INTO secure(code, salt, tick) VALUES (?, ?, ?)");
            preparedStatement.setString(1, code);
            preparedStatement.setString(2, salt);
            preparedStatement.setInt(3, tick);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, null);
        }
    }

    public boolean isSecureExists() {
        PreparedStatement preparedStatement = null;  ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM secure");
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return false;
    }
    public void setTick(Integer newTick) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("UPDATE secure SET tick=?");
            preparedStatement.setInt(1, newTick);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, null);
        }
    }

    public String getCode() {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM secure");
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("code");
            }
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return null;
    }

    public String getSalt() {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM secure");
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("salt");
            }
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return null;
    }

    public Integer getTick() {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM secure");
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("tick");
            }
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return null;
    }
    public boolean isTicked() {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM secure");
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("tick") == 1;
            }
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return false;
    }
}
