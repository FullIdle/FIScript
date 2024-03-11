package me.fullidle.fiscript.fiscript;

import groovy.lang.GroovyCodeSource;
import lombok.SneakyThrows;
import me.fullidle.fiscript.fiscript.api.CycleScripts;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FIScript extends JavaPlugin implements Listener{
    public static FIScript plugin;
    private FIScriptClassLoader loader;
    private final ArrayList<CycleScripts> cycleScripts = new ArrayList<>();
    private final ArrayList<Method> mainMethod = new ArrayList<>();

    @Override
    public void onLoad() {
        plugin = this;
        reload(null,true);
        load();
    }
    public void load(){
        //判断同名并删除
        ArrayList<String> scriptName = new ArrayList<>();
        ArrayList<CycleScripts> waitDel = new ArrayList<>();
        for (CycleScripts script : cycleScripts) {
            if (scriptName.contains(script.getScriptName())){
                RuntimeException e = new RuntimeException("Script ["+script.getScriptName()+":"+script.getVersion()+"] with the same name appears!");
                e.printStackTrace();
                waitDel.add(script);
                continue;
            }
            getLogger().info("Loading Script:["+script.getScriptName()+":"+script.getVersion()+"]");
            try {
                scriptName.add(script.getScriptName());
                script.load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cycleScripts.removeAll(waitDel);
    }

    @Override
    public void onEnable() {
        getCommand(getDescription().getName()).setExecutor(this);
        enable();
    }
    public void enable(){
        for (Method m : mainMethod) {
            try {
                m.invoke(null, (Object) new String[0]);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        for (CycleScripts script : cycleScripts) {
            getLogger().info("Enabling Script:["+script.getScriptName()+":"+script.getVersion()+"]");
            try {
                script.enable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        for (CycleScripts script : cycleScripts) {
            getLogger().info("Disabling Script:["+script.getScriptName()+":"+script.getVersion()+"]");
            try {
                script.disable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SneakyThrows
    public void reload(CommandSender sender,boolean isFirstLoad){
        //注销所有指令和监听器
        {
            unregisterAllCMD();
            HandlerList.unregisterAll((Plugin) this);
        }
        //执行一次卸载内容
        onDisable();
        //清理list
        cycleScripts.clear();
        mainMethod.clear();
        //判断是不是第一次运行这个插件
        if (!new File(getDataFolder(), "config.yml").exists()){
            saveResource("PlayerJointListener.groovy",false);
            saveResource("FIScript.groovy",false);
            saveResource("PlayerQuitListener.groovy",false);
        }
        //加载config.yml
        saveDefaultConfig();
        super.reloadConfig();
        //待用
        sender = sender == null ? Bukkit.getConsoleSender() : sender;
        //存好一个Groovy类加载器
        FIScriptClassLoader gLoader = new FIScriptClassLoader(this.getClassLoader(), CompilerConfiguration.DEFAULT);
        Map<Class<?>,File> cycClass = new HashMap<>();
        //获取插件目录内所有的.groovy文件
        for (File file : getListFile(getDataFolder())) {
            Class<?> aClass;
            try {
                aClass = gLoader.parseClass(file);
            } catch (CompilationFailedException | IOException e) {
                e.addSuppressed(new RuntimeException("Please check script file:"+file.getPath()));
                e.printStackTrace();
                continue;
            }
            //所有实现周期接口的类添加到临时
            boolean assignableFrom = CycleScripts.class.isAssignableFrom(aClass);
            if (assignableFrom) {
                cycClass.put(aClass,file);
            }
            //添加带有main方法的类
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.getName().equals("main")&&
                        method.getParameterTypes().length == 1&&
                        method.getParameterTypes()[0].equals(String[].class)&&
                        Modifier.isStatic(method.getModifiers())&&Modifier.isPublic(method.getModifiers())){
                    mainMethod.add(method);
                }
            }
        }
        //存入
        for (Map.Entry<Class<?>, File> entry : cycClass.entrySet()) {
            //需要所有类加载了在去实例化有接口的类才行
            CycleScripts cycleS = (CycleScripts) entry.getKey().getConstructor().newInstance();
            Field field = CycleScripts.class.getDeclaredField("file");
            field.setAccessible(true);
            field.set(cycleS,entry.getValue());
            cycleScripts.add(cycleS);
        }
        if (!isFirstLoad){
            if (loader != null){
                loader.clearCache();
                loader.close();
            }
            load();enable();
        }
        loader = gLoader;
    }

    //获取指定文件下的所有.groovy
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

    //指令
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reload(sender,false);
        sender.sendMessage("§aCache and overload configurations have been stripped away!");
        return false;
    }

    //注销所有指令
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