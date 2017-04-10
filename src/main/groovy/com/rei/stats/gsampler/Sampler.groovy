package com.rei.stats.gsampler

import java.util.concurrent.TimeUnit

class Sampler {
    String id
    StatsReader reader
    String namePrefix
    long interval
    TimeUnit unit
    
    @Override
    public String toString() {
        return "Sampler[${reader?.class} $interval $unit $namePrefix]"
    }
}
