package com.windstudio.discordwl.bot.Manager.Plugin.Module.Exception;

import java.io.Serial;

public class InvalidModuleDescriptionException extends Exception {

    @Serial
    private static final long serialVersionUID = 5242669537380431591L;

    public InvalidModuleDescriptionException(Throwable cause, String message) {
        super(message, cause);
    }

    public InvalidModuleDescriptionException(Throwable cause) {
        super("Invalid module.yml", cause);
    }

    public InvalidModuleDescriptionException(String message) {
        super(message);
    }

    public InvalidModuleDescriptionException() {
        super("Invalid module.yml");
    }
}

