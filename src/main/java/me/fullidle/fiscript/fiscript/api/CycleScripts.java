package me.fullidle.fiscript.fiscript.api;

import lombok.Getter;
import lombok.SneakyThrows;
import me.fullidle.fiscript.fiscript.FIScript;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

@Getter
public abstract class CycleScripts {
    private File file;
    public FIScript getFIScript(){
        return FIScript.plugin;
    };
    public abstract String getVersion();
    public abstract String getScriptName();
    public void load(){};
    public void enable(){};
    public void disable(){};
    @SneakyThrows
    public Command getCommandOrRegisterCommand(String name) {
        PluginCommand command = getFIScript().getCommand(name);
        if (command == null) {
            Constructor<PluginCommand> pluginCmdC = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCmdC.setAccessible(true);
            command = pluginCmdC.newInstance(name,getFIScript());

            Field commandMapF = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapF.setAccessible(true);
            CommandMap map = (CommandMap) commandMapF.get(Bukkit.getPluginManager());
            map.register(name,command);
        }
        return command;
    }
    public PluginManager getPluginManager(){
        return getFIScript().getServer().getPluginManager();
    }
}
