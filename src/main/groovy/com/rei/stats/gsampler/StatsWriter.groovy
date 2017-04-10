package com.rei.stats.gsampler

interface StatsWriter {
    void writeStats(Map<String, StatValue> stats)
}
