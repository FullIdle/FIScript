package me.fullidle.fiscript.fiscript;

import groovy.lang.GroovyShell;
import lombok.SneakyThrows;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements Listener{
    public static File[] loadScript;
    public static File[] enableScript;
    public static File[] disableScript;
    public GroovyShell shell = new GroovyShell();
    public static Main plugin;

    @Override
    public void onLoad() {
        plugin = this;
        reloadConfig();
        {
            setScript("cycle.load","loadScript");
            setScript("cycle.enable","enableScript");
            setScript("cycle.disable","disableScript");
        }
        {
            for (File file : loadScript) {
                try {
                    shell.evaluate(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEnable() {
        {
            for (File file : enableScript) {
                try {
                    shell.evaluate(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDisable() {
        {
            for (File file : disableScript) {
                try {
                    shell.evaluate(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void reloadConfig() {
        if (!new File(getDataFolder(), "config.yml").exists()){
            saveResource("PlayerJointListener.groovy",false);
            saveResource("PlayerQuitListener.groovy",false);
        }
        saveDefaultConfig();
        super.reloadConfig();
    }

    @SneakyThrows
    public static void setScript(String path, String fieldName) {
        Field field = Main.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ArrayList<File> files = new ArrayList<>();
        for (String s : plugin.getConfig().getStringList(path)) {
            File file = s.equalsIgnoreCase("")
                    ? plugin.getDataFolder()
                    :new File(plugin.getDataFolder(),s);
            files.addAll(getListFile(file));
        }
        field.set(Main.class,files.toArray(new File[0]));
    }
    public static List<File> getListFile(File file){
        ArrayList<File> files = new ArrayList<>();
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles == null) {
                return files;
            }
            for (File subFile : listFiles) {
                files.addAll(getListFile(subFile));
            }
            return files;
        }else{
            files.add(file);
        }
        return files.stream().filter(f->f.getName().endsWith(".groovy")).collect(Collectors.toList());
    }
}