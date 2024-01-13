package com.github.fullidle.bioprompt

import me.fullidle.ficore.ficore.common.api.util.FileUtil
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class Main implements Listener{
    public static Map<UUID, String> record = new HashMap<>()
    public static def pluginName = 'BoredPlugin'
    public static Map<World,Boolean> worldSet = new HashMap<>()
    public static def scriptPlugin = me.fullidle.fiscript.fiscript.Main.plugin
    public static def config = FileUtil.getInstance(new File(scriptPlugin.getDataFolder()
            ,Main.package.name.replace(".", File.separator)
            + File.separator + 'config.yml'),false)

    static void main(String[] args) {
        if (Bukkit.pluginManager.getPlugin('FICore') == null){
            scriptPlugin.getLogger().info(pluginName+':ȱ��FICore����!')
            scriptPlugin.getLogger().info(pluginName+':�ű�����ʧ��!')
            return
        }
        for (def s : config.getConfiguration().getStringList("worlds")) {
            def split = s.split(":")
            def wn = split[0].replace(":", "")
            def b = Boolean.parseBoolean(split[1].replace(":", ""))
            def world = Bukkit.getServer().getWorld(wn)
            worldSet.put(world,b)
        }
        Bukkit.getServer().getPluginManager().registerEvents(new Main(),scriptPlugin)

        scriptPlugin.getLogger().info('��a'+pluginName+'�ű��������7(��Bukkit�������pl�ڲ�û�취����)')
    }
    /*�¼�����*/
    @EventHandler
    void onMove(PlayerMoveEvent e){
        if (e.getFrom().distance(e.getTo()) <= 0) {
            return
        }
        def world = e.getTo().getWorld()
        if (!worldSet.containsKey(world)){
            return
        }
        def b = worldSet.get(world)
        /*<������������ʾ>*/
        def player = e.getPlayer()
        def uniqueId = player.getUniqueId()
        def biomeName = player.getLocation().getBlock().getBiome().name()
        def oldBiome = record.get(uniqueId)
        if (oldBiome == null){
            record.put(uniqueId,biomeName)
            title(player,biomeName,b)
            return
        }
        if (oldBiome == biomeName){
            return
        }
        record.replace(uniqueId,biomeName)
        title(player,biomeName,b)
    }
    static void title(Player player,String biome,boolean b){
        def name = config.getConfiguration().getString("translateName." + biome)
        if (name != null){
            biome = name
        }
        def msg = config.getConfiguration().getString("format").replace('&','��')
        msg = msg.replace("{biome}",biome)
        if (b)player.sendTitle(msg,"",7, config.getConfiguration().getInt("time")*20,7)
        else player.sendTitle("",msg,7, config.getConfiguration().getInt("time")*20,7)
    }
}
