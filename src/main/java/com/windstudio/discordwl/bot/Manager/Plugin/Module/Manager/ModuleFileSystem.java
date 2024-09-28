package com.windstudio.discordwl.bot.Manager.Plugin.Module.Manager;

import java.io.InputStream;
import java.util.Map;

import com.windstudio.discordwl.bot.Manager.Plugin.Module.Exception.InvalidModuleDescriptionException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class ModuleFileSystem {
    private static final ThreadLocal<Yaml> YAML = new ThreadLocal<Yaml>() {
        protected Yaml initialValue() {
            return new Yaml((BaseConstructor) new SafeConstructor(new LoaderOptions())) {

            };
        }
    };

    private String name;

    private String main;

    private String description;

    private String author;

    private String version;
    private boolean folder;

    public ModuleFileSystem(InputStream stream) throws InvalidModuleDescriptionException {
        loadMap(asMap(((Yaml) YAML.get()).load(stream)));
    }

    private void loadMap(Map<?, ?> map) throws InvalidModuleDescriptionException {
        try {
            this.name = map.get("name").toString();
            if (!this.name.matches("^[A-Za-z0-9 _.-]+$"))
                throw new InvalidModuleDescriptionException("name '" + this.name + "' contains invalid characters.");
            this.name = this.name.replace(' ', '_');
        } catch (NullPointerException ex) {
            throw new InvalidModuleDescriptionException(ex, "name is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidModuleDescriptionException(ex, "name is of wrong type");
        }
        try {
            this.version = map.get("version").toString();
        } catch (NullPointerException ex) {
            throw new InvalidModuleDescriptionException(ex, "version is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidModuleDescriptionException(ex, "version is of wrong type");
        }
        try {
            this.main = map.get("main").toString();
        } catch (NullPointerException ex) {
            throw new InvalidModuleDescriptionException(ex, "main is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidModuleDescriptionException(ex, "main is of wrong type");
        }
        try {
            this.folder = Boolean.parseBoolean(map.get("folder").toString());
        } catch (NullPointerException ex) {
            throw new InvalidModuleDescriptionException(ex, "folder is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidModuleDescriptionException(ex, "folder is of wrong type");
        }
        try {
            this.author = map.get("author").toString();
        } catch (NullPointerException ex) {
            throw new InvalidModuleDescriptionException(ex, "author is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidModuleDescriptionException(ex, "author is of wrong type");
        }
        if (map.get("description") != null) this.description = map.get("description").toString();
    }

    private Map<?, ?> asMap(Object object) throws InvalidModuleDescriptionException {
        if (object instanceof Map)
            return (Map<?, ?>) object;
        throw new InvalidModuleDescriptionException(object + " is not properly structured");
    }

    public String getName() {
        return this.name;
    }

    public String getMain() {
        return this.main;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isFolder() {
        return folder;
    }
}
