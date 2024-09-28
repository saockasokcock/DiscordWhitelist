package com.windstudio.discordwl.bot.Manager.Plugin.Module.Exception;

import java.io.Serial;

public class InvalidModuleException extends Exception {

    @Serial
    private static final long serialVersionUID = 6775798146755612720L;

    public InvalidModuleException(Throwable cause) {
        super(cause);
    }

    public InvalidModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidModuleException(String message) {
        super(message);
    }

}
