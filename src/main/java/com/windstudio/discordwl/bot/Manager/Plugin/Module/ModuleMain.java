package com.windstudio.discordwl.bot.Manager.Plugin.Module;

import com.google.common.io.Files;
import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Manager.ModuleManager;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Manager.SuperModuleManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class ModuleMain {

    private static File modulesFolder;

    private static ModuleManager moduleManager;

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static File getModulesFolder() {
        return modulesFolder;
    }

    public void enable() {

        modulesFolder = new File(Main.getInstance().getDataFolder(), "modules");

        try {
        if (!modulesFolder.exists()) {
            Main.getInstance().getConsole().sendMessage(ColorManager.translate("&e › &fCreating &e'&fmodules&e'&f directory..."));
            modulesFolder.mkdirs();
                InputStream in = Main.getInstance().getClass().getResourceAsStream("/Reload-1.1.jar");
                File dest = new File(modulesFolder, "Reload-1.1.jar");
                FileUtils.copyInputStreamToFile(in, dest);
        } else {
                File old = new File(modulesFolder, "ReloadModule-1.0.jar");
                if (old.exists()) FileUtils.delete(old);
                InputStream in = Main.getInstance().getClass().getResourceAsStream("/Reload-1.1.jar");
                File dest = new File(modulesFolder, "Reload-1.1.jar");
                FileUtils.copyInputStreamToFile(in, dest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Main.getInstance().getConsole().sendMessage(ColorManager.translate("&e › &fLoading modules..."));
        moduleManager = (ModuleManager) new SuperModuleManager(modulesFolder);
        moduleManager.loadModules();
        if (getModuleManager().getModules().length > 0) {
            Main.getInstance().getConsole().sendMessage(ColorManager.translate("&e › &f" + getModuleManager().getModules().length + "&f module(s) loaded!"));
        } else if (getModuleManager().getModules().length == 0) {
            Main.getInstance().getConsole().sendMessage(ColorManager.translate("&e › &fThere is no modules found. " + getModuleManager().getModules().length + "&f modules loaded!"));
        }
    }
}
