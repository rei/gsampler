package com.rei.stats.gsampler

class StatValue {
    final Number value
    final Date timestamp
    
    StatValue(Number value) {
        this(value, null)
    }
    
    StatValue(Number value, Date timestamp) {
        this.value = value
        this.timestamp = timestamp ?: new Date()
    }
    
    @Override
    public String toString() {
        return "[$value $timestamp.time]";
    }
}
