package com.rei.stats.gsampler.es

import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import java.util.concurrent.TimeUnit

class ElasticSearchIndex {
    String baseUrl
    String indexPattern
    
    ElasticSearchIndex(String baseUrl, String indexPattern) {
        this.baseUrl = baseUrl
        this.indexPattern = indexPattern
    }
    
    int executeQuery(String query, String term, long time, TimeUnit unit) {
        def now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        def index = now.format(indexPattern)
        
        def from = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        from.add(Calendar.MILLISECOND, (int)-unit.toMillis(time))
        def fromIndex = from.format(indexPattern)
        
		int hits
		
		if(term == null){
			hits = doQuery(query, baseUrl, index, from, now, time)
			if (index != fromIndex) {
				hits += doQuery(query, baseUrl, fromIndex, from, now, time)
			}
		}else{
			hits = doTermQuery(query, term, baseUrl, index, from, now, time)
			if (index != fromIndex) {
				hits += doTermQuery(query, term, baseUrl, fromIndex, from, now, time)
			}
		}
        return hits
    }
	
	private int doTermQuery(String query, String term, String base, String index, Calendar from, Calendar to, long time) {
		def http = new HTTPBuilder("$base/$index/_search")
		def hits = 0

        http.request( Method.POST, ContentType.JSON ) { req ->
            body =
                    [
                            "size" : 0,
                            "query": [
                                    "bool": [
                                            "must": [
                                                    [
                                                            "query_string": [
                                                                    "query"           : query,
                                                                    "analyze_wildcard": true
                                                            ]
                                                    ],
                                                    [
                                                            "range": [
                                                                    "@timestamp": [
                                                                            "gte"   : from.time.time,
                                                                            "lte"   : to.time.time,
                                                                            "format": "epoch_millis"
                                                                    ]
                                                            ]
                                                    ]
                                            ]
                                    ]
                            ],
                            "aggs" : [
                                    "1": [
                                            "terms": [
                                                    "field": term,
                                                    "size" : 1,
                                                    "order": [
                                                            "_count": "desc"
                                                    ]
                                            ]
                                    ]
                            ]
                    ]

            response.success = { resp, json ->
                //println resp.status
                //println "${json.took}"
                //hits = json.facets.terms.total
                hits = json.aggregations.'1'.buckets[0].doc_count

            }
        }
		return hits
	}

    private int doQuery(String query, String base, String index, Calendar from, Calendar to, long time) {
        def http = new HTTPBuilder("$base/$index/_search")
        def hits = 0
        def body = new JsonSlurper().parseText("""
        {
            "size": 0,
            "query": {
                "bool": {
                    "must": [{
                        "query_string": {
                            "query": "${query}",
                            "analyze_wildcard": true
                        }
                    },
                    {
                        "range": {
                            "@timestamp": {
                                "gte": ${from.time.time},
                                "lte": ${to.time.time},
                                "format": "epoch_millis"
                            }
                        }
                    }]
                }
            }
        }""")
        http.request( Method.GET, ContentType.JSON ) { req -> body
            response.success = { resp, json ->
                hits = json.hits.total.value
            }
        }
        return hits
    }
}
