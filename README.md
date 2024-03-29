GSampler
========

GSampler is a tool for sampling stats/metrics from various sources and writing them to either Graphite or the console.

The following stats readers are supported out of the box:

 * Groovy (inline script or file)
 * JDBC (inline queries or sql file)
 * Exec (execute a system command)
 * ElasticSearch (inline queries, current support is for ES 7.x only)

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
 * decrypt('<encrypted text>') - decrypts the given string

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
		// registers a JDBC driver, will download driver jar using Aether
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

		//for 'term', pass null if you are not doing a terms query
        sampler('es', queries(idx, esQueries, 'term', 5, MINUTES), 'prefix', 5, MINUTES)
    }

	console {
		// registers a console writer to write sampled stats to
	    writer(consoleWriter())
	}

	graphite {
		// registers a graphite writer (both console and graphite will write each stat sampled)
		writer(graphiteWriter('graphite-host', 2003))
	}

Docker
------

The simplest way to run GSampler is with Docker. 

   docker run -v /your/data/dir:/sampler/data -p 2245:2245 reicoop/gsampler

Encryption Support
------------------

Passwords and any other secret data can be encrypted in the configuration file. 

  1. To encrypt a value, `POST /encrypt` with plaintext value as the request body
  2. Pass the response body to the decrypt function in the config file: `decrypt('HspkoY4K8RBtSIMBJR864ACUgDooH16uqbi8r1WjqaDHG9iUs+czog==')`

NOTE: The key.dat file in $BASEDIR must be used for this config in the future in able to be able to decrypt the values.


HTTP Interface
--------------

A very simple JSON based HTTP interface is exposed by GSampler. The http server is started on *port 2245*.

The following endpoints are exposed by GSampler:

 * GET /self-stats - returns a json map with stats about the sampler itself (total stats collected, etc)
 * GET /config - returns a json version of the parsed configuration that lists the registered samplers/writers
 * GET /errors - any exceptions thrown by a sampler
 * POST /reload-config - forces a configuration reload
 * POST /encrypt - accepts a request body to encrypt, returns the encrypted value that can be used in `decrypt(...)` method

Getting App Running in Dev Environment
---------------------------------------
Last Developed with:
* Java: 8.0.265-amzn
* Groovy 2.4.15

If you are going to use an existing config, you need the proper 'key.dat' file
to decrypt anything encrypted in the config. This goes in $BASEDIR. If you don't have one, a new one is automatically created.

Add the environment variable 'export TEST=true'. This will make the sampler run everything in the config one time without waiting for scheduled times.

The easiest way to setup the config, is to point to it with -c or create a symlink 'config' under $BASEDIR that points to it.
