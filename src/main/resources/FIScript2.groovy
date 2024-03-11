package me.fullidle.fiscript.fiscript.scriptPack
import me.fullidle.fiscript.fiscript.api.CycleScripts

class FIScript2 extends CycleScripts{
    @Override
    String getVersion() {
        return "1.0.0"
    }

    @Override
    String getScriptName() {
        return "FIScript2"
    }

    @Override
    void enable() {
        getFIScript().getLogger().info(FIScript.getName())
    }
}
