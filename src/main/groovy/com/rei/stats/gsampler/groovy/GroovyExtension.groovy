package com.rei.stats.gsampler.groovy

import com.rei.stats.gsampler.Extension
import com.rei.stats.gsampler.StatsReader

class GroovyExtension implements Extension {
    final String name = 'groovy'

    StatsReader scriptFile(fileName, args = []) {
        return new GroovyStatsReader(new File(fileName), args)
    }
    
    StatsReader scriptText(scriptText, args = []) {
        return new GroovyStatsReader(scriptText, args)
    }
}
