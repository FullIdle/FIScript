import me.fullidle.fiscript.fiscript.FIScript
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJointListener implements Listener{
    static void main(String[] args) {
        Bukkit.getPluginManager().registerEvents(new PlayerJointListener(),FIScript.plugin);
    }

    @EventHandler
    def join(PlayerJoinEvent e){
        Bukkit.broadcastMessage(e.getPlayer().getName()+'加入服务器(该提示由FIScript脚本执行)')
    }
}
