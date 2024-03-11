package me.fullidle.fiscript.fiscript;

import lombok.SneakyThrows;
import me.fullidle.fiscript.fiscript.evet.ReloadEvent;
import org.bukkit.command.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements Listener{
    public static File[] loadScript;
    public static File[] enableScript;
    public static File[] disableScript;
    public FIScriptShell shell = new FIScriptShell(this.getClassLoader());
    public static Main plugin;
    private boolean firstLoad = false;

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEnable() {
        getCommand(getDescription().getName()).setExecutor(this);

        {
            for (File file : enableScript) {
                try {
                    shell.evaluate(file);
                } catch (Exception e) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void reloadConfig() {
        if (!new File(getDataFolder(), "config.yml").exists()){
            saveResource("PlayerJointListener.groovy",false);
            saveResource("PlayerQuitListener.groovy",false);
        }
        saveDefaultConfig();
        super.reloadConfig();
        //判断是否是第一次执行
        if (!firstLoad){
            firstLoad = true;
            return;
        }
        //第一次load不执行这里的内容
        {
            getServer().getPluginManager().callEvent(new ReloadEvent());
            //执行load,enable,disable
            onDisable();
            //去除那些b玩意 监听器和指令
            {
                try {
                    unregisterAllCMD();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                HandlerList.unregisterAll((Plugin) this);
            }
            firstLoad = false;
            //清理缓存
            {
                shell.getClassLoader().clearCache();
                shell.resetLoadedClasses();
                shell.getContext().getVariables().clear();
            }
            onLoad();
            onEnable();
        }
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reloadConfig();
        sender.sendMessage("§aCache and overload configurations have been stripped away!");
        return false;
    }

    public void unregisterAllCMD() throws NoSuchFieldException, IllegalAccessException {
        PluginManager manager = getServer().getPluginManager();
        Field cmField= manager.getClass().getDeclaredField("commandMap");
        cmField.setAccessible(true);
        CommandMap cmap = ((CommandMap) cmField.get(manager));
        Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
        field.setAccessible(true);
        Map<String, Command> knowCommands = (Map<String, Command>) field.get(cmap);
        for (Command value : knowCommands.values()) {
            if (value instanceof PluginCommand) {
                PluginCommand command = (PluginCommand) value;
                if (command.getPlugin() == this&&command.getName().equalsIgnoreCase("fiscript")) {
                    command.unregister(cmap);
                }
            }
        }
    }
}