GSampler
========

GSampler is a tool for sampling stats/metrics from various sources and writing them to either Graphite or the console.

The following stats readers are supported out of the box:

 * Groovy (inline script or file)
 * JDBC (inline queries or sql file)
 * Exec (execute a system command)
 * ElasticSearch (inline queries)

 The following stats writers are supported out of the box:

  * Graphite
  * Console

Configuration
-------------

The configuration file for GSampler is a groovy script located in $BASEDIR/config/gsampler.groovy (can be changed with -c option).

The following global methods are available in a config script:

 * globalPrefix(String) - all stats read are prefixed like this: "${globalPrefix}.${statName}" [default: 'sampler']
 * sampler(id, reader, namePrefix, interval, TimeUnit[default: SECONDS]) - registers a sampler with specified reader, prefix, and interval
 * writer(writer) - registers the specified writer to writer stats

*Example:*

    // top level block is an extension name
	groovy {
		// creates a groovy stat reader that executes the inline script, scripts must return a Map<String, Number|StatValue>
		def groovyTextReader = scriptText('[groovyStat: new StatValue(1, new Date())]')

		// creates a reader that executes the groovy script file and passes the arguments specified to it (bound to 'args')
		def groovyFileReader = scriptFile('/scripts/groovy/readSomeStats.groovy', ['arg1', 'arg2'])

	    sampler('text', groovyTextReader, 'groovy.stats', 1)
	    sampler('file', groovyFileReader, 'groovy.stats', 1)
	}

	jdbc {
		// registers a JDBC driver, will download driver jar using Groovy Grapes
		driver('org.h2.Driver', 'com.h2database:h2:1.1.105')

		// creates a connection factory object which can be passed to any jdbc reader
		def cf = connectionFactory('jdbc:h2:mem:', 'sa', '')

		def queries = ["select 'stat.name', count(*) from some_table"]

		// registers a sampler with the connection factory and queries from a list
		sampler('jdbc-stats', jdbcReader(cf, queries), 'sql.stats.prefix', 5, MINUTES)
	}

    exec {
        // use default metric parsing, <metric name>[whitespace]<value>
        sampler('default', command('default.sh'), 'default', 5, MINUTES)

        // set a custom parser, expects either regex with 2 matching groups or 1 AND metric name
        def p = parser(/Average = (\\d+)ms/, 'ping.average')
        sampler('exec', command('ping 127.0.0.1 -n 1', 1000, p), 'net', 1, MINUTES)
    }

    elasticsearch {
        def idx = index('http://elasticseach:9200')
        def esQueries = ['posts': 'type:apacheAccess AND verb:POST']

		//for 'term' pass null if you are not doing a terms query
        sampler('es', queries(idx, esQueries, 'term', 5, MINUTES), 'prefix', 5, MINUTES)
    }

	console {
		// registers a console writer to writer sampled stats to
	    writer(consoleWriter())
	}

	graphite {
		// registers a graphite writer (both console and graphite will write each stat sampled)
		writer(graphiteWriter('graphite-host', 2003))
	}

HTTP Interface
--------------

A very simple JSON based HTTP interface is exposed by GSampler. The http server is started on *port 2245*.

The following endpoints are exposed by GSampler:

 * GET /self-stats - returns a json map with stats about the sampler itself (total stats collected, etc)
 * GET /config - returns a json version of the parsed configuration that lists the registered samplers/writers
 * GET /errors - any exceptions thrown by a sampler
 * POST /reload-config - forces a configuration reload
