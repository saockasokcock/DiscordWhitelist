package com.windstudio.discordwl.bot.Manager.Plugin;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.Config.ConfigUpdater;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LanguageManager {
    public Main plugin;
    public HashMap<String, String> translate = new HashMap<>();
    public HashMap<String, List<String>> translateLoop = new HashMap<>();
    public LanguageManager(Main plugin) {
        this.plugin = plugin;
        File languageDirectory = new File(plugin.getDataFolder(), "languages/");
        if (!languageDirectory.isDirectory()) {
            languageDirectory.mkdir();
        }
        switch (getString("Service.Language")) {
            case "en_US":
                setLang("en_US");
                break;
            case "ru_RU":
                setLang("ru_RU");
                break;
            case "pl_PL":
                setLang("pl_PL");
                break;
            default:
                setLang("en_US");
            break;
        }
    }
    public String get(String path){
        return translate.get(path);
    }
    public List<String> getStringList(String path){
        return translateLoop.get(path);
    }
    public void setLang(String lang) {
        File LanguageFile = new File(plugin.getDataFolder(), "languages/" + lang+".yml");
        InputStream stream = plugin.getResource(lang+".yml");
        if (!LanguageFile.exists()) {
            try {
                FileUtils.copyInputStreamToFile(stream, LanguageFile);
                LanguageFile.createNewFile();
            } catch (IOException e) {
                plugin.getConsole().sendMessage(ColorManager.translate("&c › Language file '" + lang+".yml' cannot created. Plugin was disabled"));
                Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            }
        }
        FileConfiguration translationL = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "languages/" + lang+".yml"));
        for (String translation : translationL.getKeys(false)) {
            translate.put(translation, translationL.getString(translation));
            translateLoop.put(translation, translationL.getStringList(translation));
        }
    }
    public void updateLang(String lang) {
        File LanguageFile = new File(plugin.getDataFolder(), "languages/" + lang+".yml");
        if (LanguageFile.exists()) {
            try {
                ConfigUpdater.update(plugin, lang+".yml", LanguageFile, Collections.emptyList());
            } catch (IOException e) {
                plugin.getConsole().sendMessage(ColorManager.translate("&c › Language file '"+lang+".yml' cannot updated."));
            }
        }
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
    public File FileUpdate() {
        switch (getString("Service.Language")) {
            case "en_US":
                updateLang("en_US");
                break;
            case "ru_RU":
                updateLang("ru_RU");
                break;
            case "pl_PL":
                updateLang("pl_PL");
                break;
            default:
                updateLang("en_US");
                break;
        }
        return null;
    }
}