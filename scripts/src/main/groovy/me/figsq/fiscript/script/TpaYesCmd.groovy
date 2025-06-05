package me.figsq.fiscript.script

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

public class TpaYesCmd implements CommandExecutor {
    /*单例*/
    static final TpaYesCmd INSTANCE = new TpaYesCmd();

    @Override
    boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令!")
            return false;
        }
        def uuid = TpaCmd.cache.get(sender.getUniqueId())
        if (uuid == null) {
            sender.sendMessage("你没有收到任何tpa请求!")
            return false;
        }
        def target = Bukkit.getPlayer(uuid);
        if (target == null) {
            sender.sendMessage("你没有收到任何tpa请求!")
            TpaCmd.cache.remove(sender.getUniqueId())
            return false;
        }
        target.teleport(sender)
        sender.sendMessage("你接受了tpa请求!")
        target.sendMessage("${sender.getName()}接受了tpa请求!")
        return false;
    }
}
