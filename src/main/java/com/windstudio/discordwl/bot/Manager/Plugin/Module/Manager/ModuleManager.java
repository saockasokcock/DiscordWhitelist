package com.windstudio.discordwl.bot.Manager.Plugin.Module.Manager;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Exception.InvalidModuleException;
import com.windstudio.discordwl.bot.Manager.Plugin.Module.Module;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public interface ModuleManager {
    Module[] loadModules(File paramFile);

    Module[] loadModules();

    Module[] getModules();

}
