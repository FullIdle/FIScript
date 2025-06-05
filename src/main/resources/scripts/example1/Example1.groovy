package example1

import me.fullidle.fiscript.fiscript.api.ScriptPlugin

class Example1 extends ScriptPlugin {
    @Override
    String getVersion() {
        return "1.0"
    }

    @Override
    String getScriptName() {
        return "实例1"
    }

    @Override
    void load() {
        println "${getScriptName()} load info"
    }

    @Override
    void enable() {
        println "${getScriptName()} enable info"
    }

    @Override
    void disable() {
        println "${getScriptName()} disable info"
    }
}
