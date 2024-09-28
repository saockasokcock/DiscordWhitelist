package com.windstudio.discordwl.bot.DataBase.MySQL;

import com.windstudio.discordwl.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CPoolManager {
    public Main plugin;
    private HikariDataSource dataSource;
    private String hostname;
    private String port;
    private String database;
    private String username;
    private String password;
    private long connectionTimeout;

    public CPoolManager(Main plugin) {
        this.plugin = plugin;
        init();
        setupPool();
        createTableSecure(); createTableLinking(); createTableWhitelist();
    }

    private void init() {
        hostname = getString("Database.Settings.MySQL.Host.IP");
        port = getString("Database.Settings.MySQL.Host.Port");
        database = getString("Database.Settings.MySQL.DatabaseName");
        username = getString("Database.Settings.MySQL.Host.Login");
        password = getString("Database.Settings.MySQL.Host.Password");
        connectionTimeout = getInt("Database.Settings.MySQL.Host.ConnectionTimeOut");
    }

    private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(connectionTimeout);
        dataSource = new HikariDataSource(config);
    }
    private void createTableLinking() {
        PreparedStatement preparedStatement = null;
        Connection con = null;
        String tableLinking = getString("Database.Settings.MySQL.TableName.Linking");
        try {
            con = getConnection();
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS " +tableLinking+"(uuid varchar(36) PRIMARY KEY, nickname varchar(16), discord varchar(37), discord_id varchar(18), linking_date varchar(19));");
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            close(con, preparedStatement, null);
        }
    }
    private void createTableSecure() {
        PreparedStatement preparedStatement = null;
        Connection con = null;
        try {
            con = getConnection();
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS secure(code varchar(64), salt varchar(64), tick INTEGER);");
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            close(con, preparedStatement, null);
        }
    }
    private void createTableWhitelist() {
        PreparedStatement preparedStatement = null;
        Connection con = null;
        String tableWhitelist = getString("Database.Settings.MySQL.TableName.Whitelist");
        try {
            con = getConnection();
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+tableWhitelist+"(nickname varchar(16), player_type varchar(13), whitelist_date varchar(19));");
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.getConsole().sendMessage(e.toString());
        } finally {
            close(con, preparedStatement, null);
        }
    }
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) { plugin.getConsole().sendMessage(ignored.toString());}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) { plugin.getConsole().sendMessage(ignored.toString());}
        if (res != null) try { res.close(); } catch (SQLException ignored) { plugin.getConsole().sendMessage(ignored.toString());}
    }
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    public String getString(String path) { return plugin.getString(path); }
    public Integer getInt(String path) { return plugin.getConfig().getInt(path); }
}
