package com.rei.stats.gsampler.graphite

import com.rei.stats.gsampler.Extension

class GraphiteExtension implements Extension {
    final String name = 'graphite'
    
    GraphiteStatsWriter graphiteWriter(String host, int port) {
        return new GraphiteStatsWriter(host, port)
    }
}
