package com.rei.stats.gsampler.jdbc;

import static org.junit.Assert.*

import java.sql.SQLException

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class JdbcStatsReaderTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()
    
    def connFactory = new ConnectionFactory('jdbc:h2:mem:', 'sa', '')

    @Test(expected=IllegalArgumentException)
    public void canFailSyntaxCheck() {
        new JdbcStatsReader(connFactory, ['select 1 from nonexistent'])
    }
    
    @Test
    public void canReadStatsFromSingleQuery() {
        def queries = ["select 'metric.name', 10, current_timestamp()"]
        def reader = new JdbcStatsReader(connFactory, queries)
        def stats = reader.read()
        println stats
        assertEquals(1, stats.size())
        assertEquals(10, stats['metric.name'].value)
    }
    
    @Test
    public void canReadStatsFromMultipleQueries() {
        def queries = ["select 'metric.name', 10, current_timestamp()", 
                       "select 'other.metric.name', 20, current_timestamp()"]
        
        def reader = new JdbcStatsReader(connFactory, queries)
        def stats = reader.read()
        println stats
        assertEquals(2, stats.size())
        assertEquals(10, stats['metric.name'].value)
        assertEquals(20, stats['other.metric.name'].value)
    }
    
    @Test
    public void canReadMultipleStatsFromMultipleQueries() {
        def queries = ["select name, convert(value, int) from information_schema.settings where name like '%Size'",
                       "select 'other.metric.name', 20, current_timestamp()"]
        
        def reader = new JdbcStatsReader(connFactory, queries)
        def stats = reader.read()
        println stats
        assertEquals(6, stats.size())
        assertEquals(20, stats['other.metric.name'].value)
        assertEquals(1024, stats['h2.objectCacheSize'].value)
    }
    
    @Test
    public void resilientToFailedDbConnection() {
        def invocations = 0
        def mockConnFactory = [getConnection: { 
            if (invocations == 1) { throw new SQLException("connection failure") }
            return connFactory.connection
        }] as ConnectionFactory
    
        def reader = new JdbcStatsReader(mockConnFactory, ["select 'metric.name', 10"])
        try {
            reader.read()
        } catch (SQLException e) {
            println e.message
        }
        def stats = reader.read()
        println stats
        assertEquals(1, stats.size())
    }
    
    @Test
    public void canReadQueriesFromFile() {
        def sqlFile = tmp.newFile()
        sqlFile.text = '''
select name, convert(value, int) from information_schema.settings where name like '%Size';

-- selects some metric
select 'other.metric.name', 20, current_timestamp();
select 'metric.name', 10;

'''
        
        def reader = new JdbcStatsReader(connFactory, sqlFile)
        def stats = reader.read()
        println stats
        assertNotNull(stats['h2.objectCacheSize'])
        assertNotNull(stats['other.metric.name'])
        assertNotNull(stats['metric.name'])
    }
}