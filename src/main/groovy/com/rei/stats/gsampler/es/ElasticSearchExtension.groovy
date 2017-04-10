package com.rei.stats.gsampler.es

import java.util.concurrent.TimeUnit

import com.rei.stats.gsampler.Extension
import com.rei.stats.gsampler.StatsReader

class ElasticSearchExtension implements Extension {
    final String name = 'elasticsearch'

    ElasticSearchIndex index(String url, String index="'logstash-'yyyy.MM.dd") {
        return new ElasticSearchIndex(url, index)
    }
    
    StatsReader query(ElasticSearchIndex idx, String metricName, String query, String term, long time=-1, TimeUnit unit=TimeUnit.MINUTES) {
        return queries(idx, [(metricName): query], term, time, unit)
    }

    StatsReader queries(ElasticSearchIndex idx, Map<String, String> queries, String term, long time=-1, TimeUnit unit=TimeUnit.MINUTES) {
        return new ElasticSearchStatsReader(idx, queries, term, time, unit)
    }
}
