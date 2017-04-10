package com.rei.stats.gsampler.jdbc

import groovy.sql.Sql

import java.sql.SQLException

import com.rei.stats.gsampler.StatValue
import com.rei.stats.gsampler.StatsReader

class JdbcStatsReader implements StatsReader {

    private List queries;
    private ConnectionFactory connFactory;

    JdbcStatsReader(ConnectionFactory connFactory, File sqlFile) {
        this(connFactory, parseQueries(sqlFile))
    }

    JdbcStatsReader(ConnectionFactory connFactory, List queries) {
        this.connFactory = connFactory;
        this.queries = queries;
        syntaxCheck()
    }

    public Map<String, StatValue> read() {
        def sql = getSql()
        def stats
        sql.cacheConnection {
            def results = queries.collect { query -> sql.rows(query) }.flatten()
            stats = results.collectEntries { [it[0], it.size() == 2 ? new StatValue(it[1]) : new StatValue(it[1], it[2])] }
        }
        return stats
    }

    private Sql getSql() { new Sql(connFactory) }

    private void syntaxCheck() {
        def sql = getSql()
        sql.cacheConnection {
            def checkerQueries = queries.collectEntries { [it, "select * from ($it) x where 1 = 0"] }
            def errors = checkerQueries.collect { origQuery, query ->
                try {
                    sql.rows(query.toString(), 0, 1)
                    return [origQuery, '']
                } catch (SQLException e) {
                    return [origQuery, e.message]
                }
            }
            errors = errors.findAll { !it[1].empty }
            if (!errors.empty) {
                def msg = errors.collect{ "Invalid query ($it[1]): $it[0]" }.join("\n")
                        throw new IllegalArgumentException(msg)
            }
        }
    }

    private static List parseQueries(File f) {
        return f.text.split(';').collect { it.trim() }.findAll { !it.empty }
    }
}
