package me.figsq.fiscript.script

import me.fullidle.fiscript.fiscript.api.ScriptPlugin

class Tpa extends ScriptPlugin{
    @Override
    String getVersion() {
        return "1.0"
    }

    @Override
    String getScriptName() {
        return "Tpa"
    }

    @Override
    void enable() {
        getLogger().info("脚本已启用!")
        getCommandOrRegisterCommand("tpa").setExecutor(TpaCmd.INSTANCE)
        getCommandOrRegisterCommand("tpayes").setExecutor(TpaYesCmd.INSTANCE)
    }
}
