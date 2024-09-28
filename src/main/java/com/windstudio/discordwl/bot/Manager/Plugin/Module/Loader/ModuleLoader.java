package com.windstudio.discordwl.bot.Manager.Plugin.Module.Loader;

import com.windstudio.discordwl.bot.Manager.Plugin.Module.Exception.InvalidModuleDescriptionException;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Exception.InvalidModuleException;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Module;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Manager.ModuleFileSystem;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.ModuleMain;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleLoader {
    public static Module loadModule(File file) throws InvalidModuleException {
        ModuleFileSystem description;
        ModuleClassLoader loader;
        Validate.notNull(file, "FileHelp cannot be null");
        if (!file.exists())
            throw new InvalidModuleException(new FileNotFoundException(String.valueOf(file.getPath()) + " does not exist"));
        try {
            description = getModuleDescriptionFile(file);
        } catch (InvalidModuleDescriptionException e) {
            throw new InvalidModuleException(e);
        }
        File dataFolder = new File(ModuleMain.getModulesFolder() + File.separator + description.getName());
        if (description.isFolder()) {
            if (!dataFolder.exists())
                dataFolder.mkdir();
        }
        try {
            loader = new ModuleClassLoader(ModuleLoader.class.getClassLoader(), description, file, dataFolder);
        } catch (MalformedURLException e) {
            throw new InvalidModuleException(e);
        }
        return loader.module;
    }

    public static ModuleFileSystem getModuleDescriptionFile(File file) throws InvalidModuleDescriptionException {
        Validate.notNull(file, "FileHelp cannot be null");
        JarFile jar = null;
        InputStream stream = null;
        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("module.yml");
            if (entry == null)
                throw new InvalidModuleDescriptionException(new FileNotFoundException("Module Jar does not contain module.yml"));
            stream = jar.getInputStream(entry);
            return new ModuleFileSystem(stream);
        } catch (IOException|org.yaml.snakeyaml.error.YAMLException ex) {
            throw new InvalidModuleDescriptionException(ex);
        } finally {
            if (jar != null)
                try {
                    jar.close();
                } catch (IOException iOException) {}
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException iOException) {}
        }
    }
}
