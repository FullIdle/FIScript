package test

import me.fullidle.fiscript.fiscript.api.ScriptPlugin

class Test extends ScriptPlugin {
    @Override
    String getVersion() {
        return "1.0"
    }

    @Override
    String getScriptName() {
        return "Test"
    }

    @Override
    void enable() {

    }

    @Override
    void disable() {
    }
}
