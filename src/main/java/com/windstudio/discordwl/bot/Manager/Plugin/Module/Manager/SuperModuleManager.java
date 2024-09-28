package com.windstudio.discordwl.bot.Manager.Plugin.Module.Manager;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Exception.InvalidModuleException;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Loader.ModuleLoader;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Module;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.ModuleMain;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SuperModuleManager implements ModuleManager {
    private static final Pattern jarPattern = Pattern.compile("(.+?)(\\.jar)");

    private final List<Module> modules = new ArrayList<>();

    public SuperModuleManager(File addonDir) {
        Validate.notNull(addonDir, "Directory cannot be null");
        Validate.isTrue(addonDir.isDirectory(), "Directory is invalid");
    }

    public synchronized Module loadModule(File file) throws InvalidModuleException {
        if (!jarPattern.matcher(file.getName()).matches())
            throw new InvalidModuleException("FileHelp '" + file.getName() + "' is not a Jar FileHelp!");
        Module result = ModuleLoader.loadModule(file);
        if (result != null) {
            this.modules.add(result);
            Validate.notNull(result.getDescription());
            Main.getInstance().getConsole().sendMessage(ColorManager.translate(
                    "&a › &fModule &e" + result.getDescription().getName() + "&6(&fv&e"
                            + result.getDescription().getVersion()
                            + "&f by &e" + result.getDescription().getAuthor() + "&6)&f loaded!"
            ));
        }
        return result;
    }

    public Module[] loadModules(File directory) {
        Validate.notNull(directory, "Directory cannot be null");
        Validate.isTrue(directory.isDirectory(), "Directory must be a directory");
        List<Module> result = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (jarPattern.matcher(file.getName()).matches())
                try {
                    result.add(loadModule(file));
                } catch (InvalidModuleException e) {
                    Main.getInstance().getConsole().sendMessage(ColorManager.translate(
                            "&c › &fFile(module) &c" + file.getName() +
                                    " &fin folder &c"+ directory.getPath() +" &fcannot be loaded! Error: &c" + e.getMessage()

                            ));
                }
        }
        return result.<Module>toArray(new Module[result.size()]);
    }

    public Module[] loadModules() {
        return loadModules(ModuleMain.getModulesFolder());
    }

    public Module[] getModules() {
        return this.modules.<Module>toArray(new Module[this.modules.size()]);
    }
}
