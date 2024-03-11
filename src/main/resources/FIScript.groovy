package me.fullidle.fiscript.fiscript.scriptPack
import me.fullidle.fiscript.fiscript.api.CycleScripts

class FIScript extends CycleScripts {
    @Override
    String getVersion() {
        return "1.0.0"
    }

    @Override
    String getScriptName() {
        return 'FIScript'
    }

    @Override
    void load() {
        me.fullidle.fiscript.fiscript.FIScript.plugin.getLogger().info("FIScript脚本正在加载!")
        getFIScript().getLogger().info(FIScript2.getName())
    }

    @Override
    void enable() {
        me.fullidle.fiscript.fiscript.FIScript.plugin.getLogger().info("FIScript脚本已启用!")
    }

    @Override
    void disable() {
        me.fullidle.fiscript.fiscript.FIScript.plugin.getLogger().info("FIScript脚本正在卸载!")
    }
}
