import me.fullidle.fiscript.fiscript.Main
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener implements Listener{
    static void main(String[] args) {
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), Main.plugin);
    }

    @EventHandler
    def quit(PlayerQuitEvent e){
        Bukkit.broadcastMessage(e.getPlayer().getName()+'�뿪�˷�����(����ʾ��FIScript�ű�ִ��)')
    }
}
