package me.fullidle.fiscript.fiscript;

import groovy.lang.GroovyShell;
import me.fullidle.fiscript.fiscript.evet.EvaluateScriptEvent;
import org.bukkit.Bukkit;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.IOException;

public class FIScriptShell extends GroovyShell {
    @Override
    public Object evaluate(File file) throws CompilationFailedException, IOException {
        EvaluateScriptEvent event = new EvaluateScriptEvent(file);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;
        return super.evaluate(file);
    }
}
