package com.rei.stats.gsampler.es

import java.util.concurrent.TimeUnit

import com.rei.stats.gsampler.StatValue
import com.rei.stats.gsampler.StatsReader

class ElasticSearchStatsReader implements StatsReader {

    private ElasticSearchIndex index
    private Map<String, String> queries
    private long time
    private TimeUnit unit
	private String term
    
    ElasticSearchStatsReader(ElasticSearchIndex index, Map<String, String> queries, String term, long time, TimeUnit unit) {
        this.index = index
        this.time = time;
        this.queries = queries;
        this.unit = unit  
        this.term = term		
    }
    
    @Override
    public Map<String, StatValue> read() {
        return queries.collectEntries { n, q -> [n, new StatValue(index.executeQuery(q, term, time, unit))]}
    }
}
