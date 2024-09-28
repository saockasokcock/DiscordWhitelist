package com.windstudio.discordwl.bot.DataBase.SQLite;

import com.windstudio.discordwl.Main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLite {
    public Main plugin;
    public SQLite(Main plugin) {
        this.plugin = plugin;
    }
    public static Connection con;
    public void connect() {
        File DBFile = new File(plugin.getDataFolder(), getString("Database.Settings.SQLite.DatabaseName") + ".db");
        if (!DBFile.exists()) {
            try {
                DBFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String URL = "jdbc:sqlite:"+DBFile;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection(URL);
            createTableLinking();
            createTableWhtitelist();
            createSecure();
            con.setAutoCommit(true);
        } catch (Exception e) {
            plugin.getConsole().sendMessage(e.toString());
        }
    }
    public void disconnect() {
        try {
            con.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
    }
    public void createTableLinking() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+getString("Database.Settings.SQLite.TableName.Linking")+ "(uuid varchar(36) PRIMARY KEY, nickname varchar(16), discord varchar(37), discord_id varchar(18), linking_date varchar(19))");
            preparedStatement.execute(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
    }
    public void createTableWhtitelist() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+getString("Database.Settings.SQLite.TableName.Whitelist")+ "(nickname varchar(16), player_type varchar(13), whitelist_date varchar(19))");
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
    }
    private void createSecure() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS secure(code varchar(64), salt varchar(64), tick INTEGER)");
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        }
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
