package com.windstudio.discordwl.bot.Manager.Plugin.Module;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Commands.CommandManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Loader.ModuleClassLoader;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Manager.ModuleFileSystem;
import org.bukkit.command.Command;

import java.io.File;

public abstract class Module {

    private ModuleFileSystem description;
    private File dataFolder;
    private ClassLoader loader;

    public final void init(ModuleClassLoader classLoader, File dataFolder, ModuleFileSystem description) {
        this.dataFolder = dataFolder;
        this.description = description;
        this.loader = classLoader;
        this.onEnable();
    }

    public abstract void onEnable();

    public File getDataFolder() {
        return this.dataFolder;
    }

    public ModuleFileSystem getDescription() {
        return this.description;
    }

    public ClassLoader getLoader() {
        return this.loader;
    }
}
