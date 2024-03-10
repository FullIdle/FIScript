package me.fullidle.fiscript.fiscript.evet;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.io.File;

@Getter
public class EvaluateScriptEvent extends Event implements Cancellable {
    @Getter
    public static HandlerList handlerList = new HandlerList();

    @Setter
    private boolean cancelled = false;

    private final File scriptFile;

    public EvaluateScriptEvent(File scriptFile) {
        this.scriptFile = scriptFile;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
