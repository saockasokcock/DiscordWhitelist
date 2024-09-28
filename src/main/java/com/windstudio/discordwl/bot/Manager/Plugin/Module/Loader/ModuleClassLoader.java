package com.windstudio.discordwl.bot.Manager.Plugin.Module.Loader;

import com.windstudio.discordwl.bot.Manager.Plugin.Module.Exception.InvalidModuleException;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Module;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Manager.ModuleFileSystem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class ModuleClassLoader extends URLClassLoader {

    final ModuleFileSystem fileSystem;
    private final File dataFolder;
    final File file;
    Module module;

    public ModuleClassLoader(ClassLoader parent, ModuleFileSystem fileSystem, File file, File dataFolder) throws InvalidModuleException, MalformedURLException {
        super(new URL[] { file.toURI().toURL() }, parent);
        this.fileSystem = fileSystem;
        this.dataFolder = dataFolder;
        this.file = file;
        try {
            Class<?> jarClass;
            Class<? extends Module> moduleClass;
            try {
                jarClass = Class.forName(fileSystem.getMain(), true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidModuleException("Cannot find main class '" + fileSystem.getMain() + "'", ex);
            }
            try {
                moduleClass = jarClass.asSubclass(Module.class);
            } catch (ClassCastException ex) {
                throw new InvalidModuleException("The main class '" + fileSystem.getMain() + "' does not extends 'Module'", ex);
            }
            this.module = moduleClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (IllegalAccessException ex) {
            throw new InvalidModuleException("No public constructor", ex);
        } catch (InstantiationException ex) {
            throw new InvalidModuleException("Invalid module type", ex);
        } catch (NoSuchMethodException|java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
        }
        initialize(this.module);
    }

    private synchronized void initialize(Module module) {
        Validate.notNull(module, "Initializing addon cannot be null");
        Validate.isTrue((module.getClass().getClassLoader() == this), "Cannot initialize plugin outside of this class loader");
        if (this.module.getDescription() != null)
            throw new IllegalArgumentException("Module already initialized!");
        module.init(this, this.dataFolder, this.fileSystem);
    }
}