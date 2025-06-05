package me.fullidle.fiscript.fiscript.api;

import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ScriptLogger extends Logger {
    public String prefix;

    public ScriptLogger(String scriptName) {
        super(scriptName, null);
        prefix = "[FIScript|" + scriptName + "]";

        this.setParent(Bukkit.getServer().getLogger());
        this.setLevel(Level.ALL);
    }

    public void log(LogRecord logRecord) {
        logRecord.setMessage(this.prefix + logRecord.getMessage());
        super.log(logRecord);
    }
}
