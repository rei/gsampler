package com.rei.stats.gsampler.exec

import com.rei.stats.gsampler.Extension
import com.rei.stats.gsampler.StatsReader

class ExecExtension implements Extension {
    final String name = 'exec'

    StatsReader command(cmd, timeout = 10000, parser = parser()) {
        return new ExecStatsReader(cmd, timeout, parser)
    }
    
    MetricParser parser(regex = /([\w.]+)\s+(\d*\.?\d*)/, metricName = null) {
        return new MetricParser(regex, metricName)
    }
}
