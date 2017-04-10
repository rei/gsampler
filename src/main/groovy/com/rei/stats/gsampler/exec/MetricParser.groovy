package com.rei.stats.gsampler.exec

import java.util.regex.Pattern

import com.rei.stats.gsampler.StatValue

class MetricParser {
    private Pattern pattern
    private String metricName
    
    MetricParser(regex, metricName) {
        pattern = Pattern.compile(regex)
        if (pattern.matcher("").groupCount() < 2 && metricName == null) {
            throw new IllegalArgumentException("if only one matching group specified, metric name must be")
        }
        this.metricName = metricName
    }
    
    public Map<String, StatValue> parse(String output) {
        return output.readLines().collect { pattern.matcher(it) }.findAll { it }.collectEntries {
            if (it.groupCount() == 1) {
                return [metricName, val(it.group(1))] 
            }
            return [it.group(1), val(it.group(2))]
        }
    }    
    
    private StatValue val(String s) { return new StatValue(new BigDecimal(s)) }
}
