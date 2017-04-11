package com.rei.stats.gsampler.graphite

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.rei.stats.gsampler.StatValue
import com.rei.stats.gsampler.StatsWriter

class GraphiteStatsWriter implements StatsWriter {
    private static Logger logger = LoggerFactory.getLogger(GraphiteStatsWriter)
    
    private int tries = 2
    private String host
    private int port
    private Socket graphiteSocket
    private List queue = []
    private Lock lock = new ReentrantLock() 
    
    GraphiteStatsWriter(String host, int port) {
        this.host = host
        this.port = port
        connect(host, port)
    }
    
    @Override
    void writeStats(Map<String, StatValue> stats) {
        def records = stats.collect { n, v -> "${n.replace(' ', '_')} ${v.value} ${v.timestamp.time / 1000 as int}" }
        for (i in 0..(tries-1)) {
            try {
                lock.lock()
                graphiteSocket.outputStream << "${queue.join('\n')}\n${records.join('\n')}\n"
                queue.clear() // sent the data ok, clear the queue
                return
            } catch (IOException e) {
                logger.warn("exception writing stats to graphite", e)
                if (i == tries-1) { //if last try, queue records for next attempt
                    queue.addAll(records)
                }
                try {
                    connect(host, port) // try reconnecting if we couldn't write it
                } catch (IOException x) {
                    logger.warn("unable to reconnect to graphite: {}", x.message) //only log message here, too much noise 
                }
            } finally {
                lock.unlock()
            }
        }
    }

    private void connect(String host, int port) {
        if (graphiteSocket != null) {
            try {
                graphiteSocket.close()
            } catch (IOException e) {
                logger.warn("error closing graphite socket!", e)
            }
        }
        
        graphiteSocket = new Socket(host, port)
        logger.info("connected to Graphite at ${host}:${port}")
    }
}
