import me.fullidle.fiscript.fiscript.Main
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJointListener implements Listener{
    static void main(String[] args) {
        Bukkit.getPluginManager().registerEvents(new PlayerJointListener(),Main.plugin);
    }

    @EventHandler
    def join(PlayerJoinEvent e){
        Bukkit.broadcastMessage(e.getPlayer().getName()+'���������(����ʾ��FIScript�ű�ִ��)')
    }
}
