package com.rei.stats.gsampler.jdbc

import com.rei.stats.gsampler.Extension

class JdbcExtension implements Extension {
    final String name = 'jdbc'
    
    void driver(className, gav) {
        new JdbcDriver(className, gav).register()
    }
    
    ConnectionFactory connectionFactory(url, user, password) {
        return new ConnectionFactory(url, user, password)
    }
    
    JdbcStatsReader jdbcReader(ConnectionFactory connFactory, List queries) {
        return new JdbcStatsReader(connFactory, queries)
    }
    
    JdbcStatsReader jdbcReader(ConnectionFactory connFactory, CharSequence scriptFileName) {
        return new JdbcStatsReader(connFactory, new File(scriptFileName))
    }
}
