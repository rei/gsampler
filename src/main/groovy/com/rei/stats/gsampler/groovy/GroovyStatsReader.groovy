package com.rei.stats.gsampler.groovy

import com.rei.stats.gsampler.StatValue
import com.rei.stats.gsampler.StatsReader

class GroovyStatsReader implements StatsReader {
    private Script script
    
    GroovyStatsReader(File scriptFile, List args) {
        this(scriptFile.text, args)
    }
    
    GroovyStatsReader(String scriptText, List args) {
        def shell =  new GroovyShell(new Binding([args: args, val: { v, ts = new Date() -> new StatValue(v, ts) }]))
        script = shell.parse(scriptText)
    }
    
    @Override
    public Map<String, StatValue> read() {
        def val = script.run()
        return val.collectEntries { k, v -> [k, v instanceof StatValue ? v : new StatValue(v)] }        
    }
}
