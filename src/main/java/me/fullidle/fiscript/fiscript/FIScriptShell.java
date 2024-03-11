package me.fullidle.fiscript.fiscript;

import groovy.lang.GroovyShell;
import me.fullidle.fiscript.fiscript.evet.EvaluateScriptEvent;
import org.bukkit.Bukkit;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FIScriptShell extends GroovyShell {
    public FIScriptShell(ClassLoader parentClass){
        super(parentClass);
    }

    @Override
    public Object evaluate(File file) throws CompilationFailedException, IOException {
        EvaluateScriptEvent event = new EvaluateScriptEvent(file);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;
        byte[] bytes = Files.readAllBytes(file.toPath());
        String string = new String(bytes, StandardCharsets.UTF_8);
        return super.evaluate(string);
    }
}
