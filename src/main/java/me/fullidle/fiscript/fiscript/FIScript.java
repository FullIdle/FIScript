package me.fullidle.fiscript.fiscript;

import lombok.SneakyThrows;
import lombok.val;
import me.fullidle.fiscript.fiscript.api.ScriptPlugin;
import org.bukkit.command.*;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.Closeable;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class FIScript extends JavaPlugin {
    public static FIScript INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
        scriptsFolder = new File(getDataFolder(), "scripts");

        reloadConfig();
        loadedScriptPlugins.forEach(FIScript::safeLoad);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getCommand("fiscript").setExecutor(this);
        loadedScriptPlugins.forEach(FIScript::safeEnable);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        loadedScriptPlugins.forEach(FIScript::safeDisable);
    }

    public final ArrayList<ScriptPlugin> loadedScriptPlugins = new ArrayList<>();
    public File scriptsFolder = null;
    public FIScriptClassLoader scriptClassLoader = null;

    /**
     * 卸载脚本，重载配置，重加载脚本
     * 重加载脚本的main方法会立即指向，但load enable disable周期并不会理解执行
     * 卸载的disable会执行
     */
    @SneakyThrows
    @Override
    public void reloadConfig() {
        //卸载已加载的并清楚且注销所有指令和监听器
        loadedScriptPlugins.forEach(ScriptPlugin::disable);
        loadedScriptPlugins.clear();
        unregisterAll();
        if (scriptClassLoader != null) {
            scriptClassLoader.clearCache();
            scriptClassLoader.close();
            //请求gc
            System.gc();
        }

        //重新加载
        this.saveDefaultConfig();
        super.reloadConfig();

        //脚本文件
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdirs();
            saveResource("scripts/example1/Example1.groovy", false);
            saveResource("scripts/example2/Example2.groovy", false);
        }

        //新的脚本类加载器
        scriptClassLoader = new FIScriptClassLoader(this.getClassLoader(), CompilerConfiguration.DEFAULT);
        scriptClassLoader.addURL(scriptsFolder.toURI().toURL());

        val config = getConfig();
        val logger = this.getLogger();
        for (String path : config.getStringList("scripts"))
            try {
                val clz = scriptClassLoader.loadClass(path,true,false,true);
                try {
                    val main = clz.getDeclaredMethod("main", String[].class);
                    try {
                        main.invoke(null, (Object) new String[]{});
                    } catch (Exception e) {
                        error(logger, e);
                    }
                } catch (NoSuchMethodException ignored) {
                }
                val instance = clz.newInstance();
                if (instance instanceof ScriptPlugin)
                    loadedScriptPlugins.add(((ScriptPlugin) instance));
            } catch (Exception e) {
                error(logger, e);
            }
    }

    /**
     * 注销有关所有脚本的注册行为!
     */
    public void unregisterAll() {
        try {
            unregisterAllCMD();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to log out all commands!", e);
        }
        HandlerList.unregisterAll(this);
    }

    /**
     * 注销所有命令
     * 测试 1.12.2 和 1.21.1 均可以正常使用
     *
     * @throws NoSuchFieldException   没找到指令图集
     * @throws IllegalAccessException 指令图集获取提供参数错误
     */
    public void unregisterAllCMD() throws NoSuchFieldException, IllegalAccessException {
        PluginManager manager = getServer().getPluginManager();
        Field cmField = manager.getClass().getDeclaredField("commandMap");
        cmField.setAccessible(true);
        CommandMap cmap = ((CommandMap) cmField.get(manager));
        Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
        field.setAccessible(true);
        Map<String, Command> knowCommands = (Map<String, Command>) field.get(cmap);
        for (Command value : knowCommands.values()) {
            if (value instanceof PluginCommand) {
                PluginCommand command = (PluginCommand) value;
                if (command.getPlugin() == this && command.getName().equalsIgnoreCase("fiscript")) {
                    command.unregister(cmap);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.reloadConfig();
        val logger = this.getLogger();
        val loadFailed = new ArrayList<ScriptPlugin>();
        for (ScriptPlugin scriptPlugin : loadedScriptPlugins)
            try {
                scriptPlugin.load();
            } catch (Exception e) {
                error(logger,e);
                loadFailed.add(scriptPlugin);
                sender.sendMessage("Failed to load script: " + "[" + scriptPlugin.getScriptName() + "-" + scriptPlugin.getVersion() + "]");
            }
        for (ScriptPlugin scriptPlugin : loadedScriptPlugins)
            try {
                scriptPlugin.enable();
            } catch (Exception e) {
                error(logger,e);
                sender.sendMessage("Failed to enable script: " + "[" + scriptPlugin.getScriptName() + "-" + scriptPlugin.getVersion() + "]");
            }
        sender.sendMessage("§aReloaded successfully!");
        if (!loadFailed.isEmpty()) {
            sender.sendMessage("§cThe list of scripts that failed to load is as follows:");
            for (ScriptPlugin plugin : loadFailed)
                sender.sendMessage("§c  - " + plugin.getScriptName() + "-" + plugin.getVersion());
            loadedScriptPlugins.removeAll(loadFailed);
        }
        return true;
    }

    public static void safeExec(ScriptPlugin plugin, Consumer<ScriptPlugin> action) {
        try {
            action.accept(plugin);
        } catch (Exception e) {
            error(INSTANCE.getLogger(),e);
        }
    }

    public static void safeLoad(ScriptPlugin plugin){
        INSTANCE.getLogger().info("Loading script plugin " + plugin.getScriptName() + "-" + plugin.getVersion());
        safeExec(plugin, ScriptPlugin::load);
    }

    public static void safeEnable(ScriptPlugin plugin){
        INSTANCE.getLogger().info("Enabling script plugin " + plugin.getScriptName() + "-" + plugin.getVersion());
        safeExec(plugin, ScriptPlugin::enable);
    }

    public static void safeDisable(ScriptPlugin plugin){
        INSTANCE.getLogger().info("Disabling script plugin " + plugin.getScriptName() + "-" + plugin.getVersion());
        safeExec(plugin, ScriptPlugin::disable);
    }

    public static void error(Logger logger, Throwable throwable) {
        use(new StringWriter(), sw-> use(new PrintWriter(sw), pw -> {
            throwable.printStackTrace(pw);
            logger.severe(sw.toString());
        }));
    }

    public static <T extends Closeable> void use(T closeable, Consumer<T> consumer) {
        try {
            consumer.accept(closeable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try{
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
