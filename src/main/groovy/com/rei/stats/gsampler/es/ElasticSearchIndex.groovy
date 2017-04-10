package com.rei.stats.gsampler.es

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
			body = [
  "facets": [
    "terms": [
      "terms": [
        "field": term,
        "size": 1,
        "order": "count",
        "exclude": []
      ],
      "facet_filter": [
        "fquery": [
          "query": [
            "filtered": [
              "query": [
                "bool": [
                  "should": [
                    [
                      "query_string": [ "query": query ]
                    ]
                  ]
                ]
              ],
              "filter": time < 0 ? [:] : [
                "bool": [
                  "must": [
                    [
                      "range": [
                        "@timestamp": [
                          "from": from.time.time,
                          "to": to.time.time
                        ]
                      ]
                    ]
                  ]
                ]
              ]
            ]
          ]
        ]
      ]
    ]
  ],
  "size": 0
]
		 
			response.success = { resp, json ->
				//println resp.status
				//println "${json.took}"
				//hits = json.facets.terms.total
				hits = json.facets.terms.terms[0].count

			}
		}
		return hits
	}
    
    private int doQuery(String query, String base, String index, Calendar from, Calendar to, long time) {
        def http = new HTTPBuilder("$base/$index/_search")
        def hits = 0
         
        http.request( Method.POST, ContentType.JSON ) { req ->
            body = [
              "facets": [
                "0": [
                  "query": [
                    "filtered": [
                      "query": [
                        "query_string": [ "query": query ]
                      ],
                      "filter": time < 0 ? [:] : [
                        "bool": [
                          "must": [
                            [
                              "range": [
                                "@timestamp": [
                                  "from": from.time.time,
                                  "to": to.time.time
                                ]
                              ]
                            ]
                          ]
                        ]
                      ]
                    ]
                  ]
                ]
              ],
              "size": 0
            ]
         
            response.success = { resp, json ->
//                println resp.status
//                println "${json.took}"
                hits = json.facets.'0'.count
            }
        }
        return hits
    }
}
