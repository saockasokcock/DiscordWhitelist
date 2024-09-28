package com.windstudio.discordwl.bot.Manager.Plugin;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
import com.windstudio.discordwl.bot.Linking.UserdataMySQL;
import com.windstudio.discordwl.bot.Linking.UserdataSQLite;
import com.windstudio.discordwl.bot.Manager.Discord.PresenceManager;
import com.windstudio.discordwl.bot.Whitelist.MySQLWhitelistData;
import com.windstudio.discordwl.bot.Whitelist.SQLiteWhitelistData;

public class ClassManager {
    private Main plugin;
    private UserdataSQLite userdataSQLite;
    private UserdataMySQL userdataMySQL;
    private SQLiteWhitelistData sqLiteWhitelistData;
    private MySQLWhitelistData mySqlWhitelistData;
    private PresenceManager presenceManager;
    private LinkingCommand linkingCommand;
    public ClassManager(Main plugin) {
        this.plugin = plugin;
        presenceManager = new PresenceManager(plugin);
        userdataMySQL = new UserdataMySQL(plugin);
        linkingCommand = new LinkingCommand(plugin);
        mySqlWhitelistData = new MySQLWhitelistData(plugin);
        userdataSQLite = new UserdataSQLite(plugin);
        sqLiteWhitelistData = new SQLiteWhitelistData(plugin);
    }
    public UserdataSQLite getUserdata() { return userdataSQLite; }
    public UserdataMySQL getUserdataMySQL() { return userdataMySQL; }
    public SQLiteWhitelistData getSqLiteWhitelistData() { return sqLiteWhitelistData; }
    public MySQLWhitelistData getMySQLWhitelistData() { return mySqlWhitelistData; }
    public PresenceManager getPresenceManager() { return presenceManager; }
    public LinkingCommand getLinkingCommand() { return linkingCommand; }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
