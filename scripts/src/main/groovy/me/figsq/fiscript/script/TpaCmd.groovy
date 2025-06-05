package me.figsq.fiscript.script

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TpaCmd implements CommandExecutor {
    /*单例*/
    static final def INSTANCE = new TpaCmd()

    static final Map<UUID,UUID> cache = new HashMap<>()

    @Override
    boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用这个命令!")
            return false
        }
        try {
            def target = Bukkit.getPlayer(args[0])
            if (target == null) {
                sender.sendMessage("玩家 ${args[0]} 不在线!")
                return false
            }
            if (cache.get(target.getUniqueId()) == sender.getUniqueId()) {
                sender.sendMessage("你已经在请求传送了!")
                return false
            }
            cache.put(target.getUniqueId(), sender.getUniqueId())
            target.sendMessage("${sender.getName()} 请求传送, 输入 /tpaccept 接受")
            sender.sendMessage("请求已发送!")
        } catch (ArrayIndexOutOfBoundsException ignored) {
            sender.sendMessage("用法: /tpa <玩家名>")
            return false
        }
        return false
    }
}
