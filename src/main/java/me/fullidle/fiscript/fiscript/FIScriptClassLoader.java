package me.fullidle.fiscript.fiscript;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FIScriptClassLoader extends GroovyClassLoader {
    public FIScriptClassLoader(ClassLoader parentClassLoader, CompilerConfiguration aDefault){
        super(parentClassLoader,aDefault);
    }

    /*@Override
    public Class<?> parseClass(File file) throws CompilationFailedException, IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        String str = new String(bytes,StandardCharsets.UTF_8);
        return super.parseClass(str);
    }*/
}
