package com.rei.stats.gsampler

import groovy.json.JsonBuilder

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong

class SamplerStats {
    final AtomicLong samplesTaken = new AtomicLong(0)
    final AtomicLong failedSamples = new AtomicLong(0)
    final AtomicLong lastConfigReload = new AtomicLong(0)
    final AtomicLong totalStats = new AtomicLong(0)
    final AtomicLong poolSize = new AtomicLong(0)
    final ConcurrentMap<String, Object> lastSampled = new ConcurrentHashMap<>()
    final ConcurrentMap<String, Object> lastRan = new ConcurrentHashMap<>()
    
    @Override
    public String toString() {        
        return new JsonBuilder(this).toString() 
    }
}
